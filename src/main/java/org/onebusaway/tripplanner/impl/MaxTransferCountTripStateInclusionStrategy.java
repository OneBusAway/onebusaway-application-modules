package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;

public class MaxTransferCountTripStateInclusionStrategy implements
    TripStateInclusionStrategy {

  private int _maxTransferCount;

  public MaxTransferCountTripStateInclusionStrategy(int maxTransferCount) {
    _maxTransferCount = maxTransferCount;
  }

  public boolean isStateIncluded(TripState state) {
    int count = getTransferCount(state);
    return count <= _maxTransferCount;
  }

  private int getTransferCount(TripState state) {
    if (state == null)
      return 0;
    return (state instanceof VehicleDepartureState ? 1 : 0)
        + getTransferCount(state.getPreviousState());
  }
}
