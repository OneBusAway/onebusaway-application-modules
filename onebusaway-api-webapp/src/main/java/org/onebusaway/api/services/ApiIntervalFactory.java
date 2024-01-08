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
package org.onebusaway.api.services;

import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.services.IntervalFactory;
import org.onebusaway.util.SystemTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate AgencyServiceIntervals per API system configuration
 */
public class ApiIntervalFactory implements IntervalFactory {

  private int _binWidthInMillis = 15 * 60 * 1000; // 15 minutes

  public void setBinWidthInMillis(int widthInMillis) {
    _binWidthInMillis = widthInMillis;
  }
  private Map<String, Integer> _overridesByAgencyId = new HashMap<>();
  public void setOverrides(Map<String, Integer> overridesByAgencyId) {
    _overridesByAgencyId = overridesByAgencyId;
  }

  public AgencyServiceInterval constructDefault() {
    return new AgencyServiceInterval(binDate(SystemTime.currentTimeMillis()), _overridesByAgencyId);
  }

  public AgencyServiceInterval constructForDate(Date date) {
    return new AgencyServiceInterval(binDate(date.getTime()), _overridesByAgencyId);
  }

  // reduce the precision of the data so it may be cacheable
  private long binDate(long time) {
    return new Double(Math.floor(new Double(time).doubleValue()/ _binWidthInMillis) * _binWidthInMillis).longValue();
  }


}
