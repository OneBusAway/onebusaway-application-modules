package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

class BlockStopTimeKey {

  private final ServiceIdActivation _serviceIds;

  private final AgencyAndId _stopId;

  public BlockStopTimeKey(ServiceIdActivation serviceIds, AgencyAndId stopId) {
    _serviceIds = serviceIds;
    _stopId = stopId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _serviceIds.hashCode();
    result = prime * result + _stopId.hashCode();
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
    BlockStopTimeKey other = (BlockStopTimeKey) obj;
    return _serviceIds.equals(other._serviceIds)
        && _stopId.equals(other._stopId);
  }
}