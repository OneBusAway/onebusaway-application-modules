/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.realtime.DynamicCalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
/**
 * Calendaring for dynamic trips.
 */
public class DynamicCalendarServiceImpl implements DynamicCalendarService {

  private static Logger _log = LoggerFactory.getLogger(DynamicCalendarServiceImpl.class);
  private DynamicHelper helper = new DynamicHelper();
  @Override
  public boolean hasServiceId(LocalizedServiceId localizedServiceId) {
    return helper.isServiceIdDynamic(localizedServiceId.getId().getId());
  }

  @Override
  public Collection<Date> getServiceDatesWithinRange(LocalizedServiceId localizedServiceId, ServiceInterval interval, Date from, Date to) {
    // a trivial implementation -- the actual date is embedded in the service id instead of looking it up
    // example format: MTASBWY_DYN-2023-05-09
    Date today = getDateFromServiceId(localizedServiceId);

    // service day can have overlap on both sides
    long startOfDay = today.getTime() - (1 * 60 * 60 * 1000); // allow 23:00 to be active
    long endOfDay = today.getTime() + (24 * 60 * 60 * 1000)  + (3 * 60 * 60 * 1000)- 1; // allow up to 02:59:59 to be active
    if (from.getTime() <= endOfDay && startOfDay <= to.getTime()) {
      Date[] dates = {today};
      return Arrays.asList(dates);
    }
    return Collections.emptyList();

  }
  @Override
  public boolean isLocalizedServiceIdActiveOnDate(LocalizedServiceId lsid, Date serviceDate) {
    Date lsidDate = getDateFromServiceId(lsid);
    if (lsidDate.equals(serviceDate)) {
      return true;
    }
    return false;
  }

  private Date getDateFromServiceId(LocalizedServiceId localizedServiceId) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date serviceDate = null;
    try {
      serviceDate = sdf.parse(localizedServiceId.getId().getId().replaceAll("DYN-", ""));
    } catch (ParseException e) {
      _log.error("unexpected dynamic serviceId {", localizedServiceId.getId().toString());
      return null;
    }
    return serviceDate;
  }
}
