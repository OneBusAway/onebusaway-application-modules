package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;

public class MaxTimeTripStateInclusionStrategy implements
    TripStateInclusionStrategy {

  private long _maxTime;

  public MaxTimeTripStateInclusionStrategy(long maxTime) {
    _maxTime = maxTime;
  }

  public boolean isStateIncluded(TripState state) {
    return state.getCurrentTime() <= _maxTime;
  }
}
