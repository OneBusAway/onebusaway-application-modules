package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

public interface VehicleLocationRecordCache {

  public VehicleLocationCacheElements getRecordForVehicleId(AgencyAndId vehicleId);

  public List<VehicleLocationCacheElements> getRecordsForBlockInstance(
      BlockInstance blockInstance);

  public VehicleLocationCacheElements addRecord(BlockInstance blockInstance, VehicleLocationRecord record, ScheduledBlockLocation scheduledBlockLocation, ScheduleDeviationSamples samples);

  public void clearRecordsForVehicleId(AgencyAndId vehicleId);
}