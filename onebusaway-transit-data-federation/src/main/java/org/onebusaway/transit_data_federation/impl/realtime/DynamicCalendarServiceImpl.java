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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Component
/**
 * Calendaring for dynamic trips.
 */
public class DynamicCalendarServiceImpl implements DynamicCalendarService {

  private static Logger _log = LoggerFactory.getLogger(DynamicCalendarServiceImpl.class);
  @Override
  public boolean hasServiceId(LocalizedServiceId localizedServiceId) {
    return localizedServiceId.getId().getId().startsWith("DYN-");
  }

  @Override
  public Collection<Date> getServiceDatesWithinRange(LocalizedServiceId localizedServiceId, ServiceInterval interval, Date from, Date to) {
    // a trivial implementation -- the actual date is embedded in the service id instead of looking it up
    // example format: MTASBWY_DYN-2023-05-09
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date today = null;
    try {
      today = sdf.parse(localizedServiceId.getId().getId().replaceAll("DYN-", ""));
    } catch (ParseException e) {
      _log.error("unexpected dynamic serviceId {", localizedServiceId.getId().toString());
      return new ArrayList<>();
    }

    Date yesterday = new Date(today.toInstant().toEpochMilli() - 24 * 60 * 60 * 1000);
    Date tomorrow = new Date(today.toInstant().toEpochMilli() + 24 * 60 * 60 * 1000);
    Date[] dates = {yesterday, today, tomorrow};
    return Arrays.asList(dates);
  }
}
