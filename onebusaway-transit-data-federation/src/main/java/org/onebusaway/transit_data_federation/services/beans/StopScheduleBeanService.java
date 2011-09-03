/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.services.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;

/**
 * Service methods for querying the full schedule at a particular stop,
 * including the days the stop has service and the schedule on each of those
 * service dates.
 * 
 * @author bdferris
 * @see StopRouteScheduleBean
 * @see StopCalendarDaysBean
 */
public interface StopScheduleBeanService {

  /**
   * Retrieve the full schedule for a particular stop on a particular service
   * date
   * 
   * @param stopId see {@link Stop#getId()}
   * @param date the date of service
   * @return the schedule for each route at the specified stop on the specified
   *         service date
   */
  public List<StopRouteScheduleBean> getScheduledArrivalsForStopAndDate(
      AgencyAndId stopId, ServiceDate date);

  /**
   * Retrieve the service dates that a stop has service
   * 
   * @param stopId see {@link Stop#getId()}
   * @return the set of calendar days for which we have schedule data for the
   *         specified stop
   */
  public StopCalendarDaysBean getCalendarForStop(AgencyAndId stopId);
}
