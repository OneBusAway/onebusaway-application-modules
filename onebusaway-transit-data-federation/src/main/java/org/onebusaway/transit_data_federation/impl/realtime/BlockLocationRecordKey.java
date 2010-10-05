package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

class BlockLocationRecordKey {
  private final BlockInstance blockInstance;
  private final AgencyAndId vehicleId;

  public BlockLocationRecordKey(BlockInstance blockInstance,
      AgencyAndId vehicleId) {
    if (blockInstance == null)
      throw new IllegalArgumentException("blockInstance is null");
    if (vehicleId == null)
      throw new IllegalArgumentException("vehicleId is null");
    this.blockInstance = blockInstance;
    this.vehicleId = vehicleId;
  }

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockInstance.hashCode();
    result = prime * result + vehicleId.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockLocationRecordKey other = (BlockLocationRecordKey) obj;
    if (!blockInstance.equals(other.blockInstance))
      return false;
    if (!vehicleId.equals(other.vehicleId))
      return false;
    return true;
  }

}