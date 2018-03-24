/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.bundle.task;

import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Collection;

/**
 * To help with pick changes (as opposed to just updates), modify the start date of the gtfs 
 * to allow inference of overnight routes.
 * 
 * This task must be inserted before the calendar_service task to be effective.
 * 
 */
public class ModifyStartDateTask implements Runnable {

  private static final int ROLLBACK_INTERVAL = -15;

  private Logger _log = LoggerFactory.getLogger(ModifyStartDateTask.class);

  private GtfsMutableRelationalDao _gtfsMutableRelationalDao;
  protected BundleRequestResponse requestResponse;
  
  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }

  @Autowired
  public void setGtfsMutableRelationalDao(
      GtfsMutableRelationalDao gtfsMutableRelationalDao) {
    _gtfsMutableRelationalDao = gtfsMutableRelationalDao;
  }

  public void run() {
    if (!requestResponse.getRequest().getPredate()) {
      _log.info("ModifyStartDateTask Exiting, predate flag not set");
      return;
    }
    
    _log.info("ModifyStartDateTask Running:  predate flag is set");
    Collection<ServiceCalendar> calendars = _gtfsMutableRelationalDao.getAllCalendars();
    _log.info("found " + calendars.size() + " calendar entries");
    for (ServiceCalendar sc : calendars) {
      if (isApplicable(sc)) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sc.getStartDate().getAsDate());
        cal.add(Calendar.DAY_OF_YEAR, ROLLBACK_INTERVAL); // go back 15 days recomputing month if necessary
        _log.info("changed (" + sc.getServiceId() + ") calendar start date from "
            + sc.getStartDate().getAsDate() + " to " + cal.getTime());
        sc.setStartDate(new ServiceDate(cal));
      }
    }
  }

  /**
   * Pick changes happen on Saturday night; extend Saturday only serviceIds back more than two weeks 
   * so that service will be considered.  Do not extends service exceptions that consider only a single 
   * Saturday
   * @param ServiceCalencar an entry in calendars.txt
   * @return true if the calendar entry is applicable
   */
  private boolean isApplicable(ServiceCalendar sc) {
    if (sc.getSaturday() == 1 
        && sc.getMonday() == 0 
        && sc.getTuesday() == 0
        && sc.getWednesday() == 0 
        && sc.getThursday() == 0
        && sc.getFriday() == 0 
        && sc.getSunday() == 0) {
      if (!sc.getStartDate().equals(sc.getEndDate())) {
        return true;
      }
    }
    return false;
  }
}
