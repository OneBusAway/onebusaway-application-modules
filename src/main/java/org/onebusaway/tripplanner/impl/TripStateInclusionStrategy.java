package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;

public interface TripStateInclusionStrategy {
  public boolean isStateIncluded(TripState state);
}
