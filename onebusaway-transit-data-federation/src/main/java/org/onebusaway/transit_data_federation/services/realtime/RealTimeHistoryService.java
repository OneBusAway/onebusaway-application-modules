package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

public interface RealTimeHistoryService {
  public ScheduleDeviationHistogram getScheduleDeviationHistogramForArrivalAndDepartureInstance(
      ArrivalAndDepartureInstance instance, int stepSizeInSeconds);

  public ScheduleDeviationSamples sampleScheduleDeviationsForVehicle(
      BlockInstance instance, VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation);
}
