package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

public class VehicleLocationCacheElement {

  private final long measuredLastUpdateTime = System.currentTimeMillis();

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

  public ScheduledBlockLocation getScheduledBlockLocation() {
    return _scheduledBlockLocation;
  }

  public ScheduleDeviationSamples getScheduleDeviations() {
    return _scheduleDeviations;
  }
}