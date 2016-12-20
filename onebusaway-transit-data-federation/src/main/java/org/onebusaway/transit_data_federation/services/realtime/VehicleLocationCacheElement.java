/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.util.SystemTime;

public class VehicleLocationCacheElement {

  private final long measuredLastUpdateTime = SystemTime.currentTimeMillis();

  private final VehicleLocationRecord _record;

  private final ScheduledBlockLocation _scheduledBlockLocation;

  private final ScheduleDeviationSamples _scheduleDeviations;

  public VehicleLocationCacheElement(VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation,
      ScheduleDeviationSamples scheduleDeviations) {
    _record = record;
    _scheduledBlockLocation = scheduledBlockLocation;
    _scheduleDeviations = scheduleDeviations;
  }

  public long getMeasuredLastUpdateTime() {
    return measuredLastUpdateTime;
  }

  public VehicleLocationRecord getRecord() {
    return _record;
  }

  /**
   * The effective scheduled block location of the transit vehicle at the time
   * of the vehicle location record.
   * 
   * @return the effective location, or null if unknown.
   */
  public ScheduledBlockLocation getScheduledBlockLocation() {
    return _scheduledBlockLocation;
  }

  public ScheduleDeviationSamples getScheduleDeviations() {
    return _scheduleDeviations;
  }
}