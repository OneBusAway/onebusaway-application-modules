package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

public class VehicleLocationCacheRecord {

  private final BlockInstance _blockInstance;

  private final long measuredLastUpdateTime = System.currentTimeMillis();;

  private final VehicleLocationRecord _record;

  public VehicleLocationCacheRecord(BlockInstance blockInstance,
      VehicleLocationRecord record) {
    _blockInstance = blockInstance;
    _record = record;
  }

  public BlockInstance getBlockInstance() {
    return _blockInstance;
  }

  public long getMeasuredLastUpdateTime() {
    return measuredLastUpdateTime;
  }

  public VehicleLocationRecord getRecord() {
    return _record;
  }
}