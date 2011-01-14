package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

class BlockLayoverSequenceKey {

  private final ServiceIdActivation _serviceIds;

  private final AgencyAndId _firstStopId;

  public BlockLayoverSequenceKey(ServiceIdActivation serviceIds,
      AgencyAndId firstStopId) {
    _serviceIds = serviceIds;
    _firstStopId = firstStopId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _serviceIds.hashCode();
    result = prime * result + _firstStopId.hashCode();
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
    BlockLayoverSequenceKey other = (BlockLayoverSequenceKey) obj;
    return _serviceIds.equals(other._serviceIds)
        && _firstStopId.equals(other._firstStopId);
  }
}