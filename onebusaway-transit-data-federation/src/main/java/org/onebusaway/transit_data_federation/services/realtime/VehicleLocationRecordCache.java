package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

public interface VehicleLocationRecordCache {

  public VehicleLocationCacheRecord getRecordForVehicleId(AgencyAndId vehicleId);

  public List<VehicleLocationCacheRecord> getRecordsForBlockInstance(
      BlockInstance blockInstance);

  public VehicleLocationCacheRecord addRecord(BlockInstance blockInstance, VehicleLocationRecord record, ScheduledBlockLocation scheduledBlockLocation, ScheduleDeviationSamples samples);

  public void clearRecordsForVehicleId(AgencyAndId vehicleId);
}