/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.services.IntervalFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * TimeInterval methods for application of APIs.  Date operations
 * are expensive so minimize/cache their usage.
 */
public class WebIntervalFactory implements IntervalFactory {

  private Map<ServiceDate, AgencyServiceInterval> _beanByServiceDate = new HashMap<>();
  private Map<String, Integer> _overrides = new HashMap<>();

  private int _binWidthInMillis = 15 * 60 * 1000; // 15 minutes

  public void setBinWidthInMillis(int widthInMillis) {
    _binWidthInMillis = widthInMillis;
  }

  public void setOverrides(Map<String, Integer> overrides) {
    _overrides = overrides;
  }

  @Override
  public AgencyServiceInterval constructForDate(Date date) {
    ServiceDate serviceDate = new ServiceDate(date);
    AgencyServiceInterval agencyInterval = _beanByServiceDate.get(serviceDate);
    if (agencyInterval == null) {
      agencyInterval = new AgencyServiceInterval(binDate(date.getTime()), _overrides);
      _beanByServiceDate.put(serviceDate, agencyInterval);
    }
    return agencyInterval;
  }

  @Override
  public AgencyServiceInterval constructDefault() {
    return constructForDate(new Date());
  }

  private long binDate(long time) {
    return new Double(Math.floor(new Double(time).doubleValue()/ _binWidthInMillis) * _binWidthInMillis).longValue();
  }

}
