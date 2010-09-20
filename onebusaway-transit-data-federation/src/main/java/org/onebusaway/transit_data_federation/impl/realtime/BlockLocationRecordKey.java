package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;

class BlockLocationRecordKey {
  private final AgencyAndId blockId;
  private final long serviceDate;
  private final AgencyAndId vehicleId;

  public BlockLocationRecordKey(AgencyAndId blockId, long serviceDate, AgencyAndId vehicleId) {
    if (blockId == null)
      throw new IllegalArgumentException("blockId is null");
    if (vehicleId == null)
      throw new IllegalArgumentException("vehicleId is null");
    this.blockId = blockId;
    this.serviceDate = serviceDate;
    this.vehicleId = vehicleId;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + blockId.hashCode();
    result = prime * result + (int) (serviceDate ^ (serviceDate >>> 32));
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
    if (!blockId.equals(other.blockId))
      return false;
    if (serviceDate != other.serviceDate)
      return false;
    if (!vehicleId.equals(other.vehicleId))
      return false;
    return true;
  }

}