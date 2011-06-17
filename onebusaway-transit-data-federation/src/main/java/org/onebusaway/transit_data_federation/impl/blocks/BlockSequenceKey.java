package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

class BlockSequenceKey {

  private final ServiceIdActivation _serviceIds;

  private final List<AgencyAndId> _stopIds;


  public BlockSequenceKey(ServiceIdActivation serviceIds, List<AgencyAndId> stopIds) {
    _serviceIds = serviceIds;
    _stopIds = stopIds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _serviceIds.hashCode();
    result = prime * result + _stopIds.hashCode();
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
    BlockSequenceKey other = (BlockSequenceKey) obj;
    return _serviceIds.equals(other._serviceIds)
        && _stopIds.equals(other._stopIds);
  }
}