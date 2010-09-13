package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

class BlockSequenceKey {
  
  private final List<TripSequenceKey> _trips;

  public BlockSequenceKey(List<TripSequenceKey> trips) {
    _trips = trips;
  }

  @Override
  public int hashCode() {
    return _trips.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    BlockSequenceKey other = (BlockSequenceKey) obj;
    return _trips.equals(other._trips);
  }
}