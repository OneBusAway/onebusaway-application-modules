package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;

class TripSequenceKey {

  private final LocalizedServiceId _serviceId;

  private final List<AgencyAndId> _stopIds;

  public TripSequenceKey(LocalizedServiceId serviceId, List<AgencyAndId> stopIds) {
    _serviceId = serviceId;
    _stopIds = stopIds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _serviceId.hashCode();
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
    TripSequenceKey other = (TripSequenceKey) obj;
    return _serviceId.equals(other._serviceId)
        && _stopIds.equals(other._stopIds);
  }
}