package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;

import java.util.ArrayList;
import java.util.List;

public class MultiTripInclusionStrategy implements TripStateInclusionStrategy {

  private List<TripStateInclusionStrategy> _strategies = new ArrayList<TripStateInclusionStrategy>();

  public void addInclusionStrategy(TripStateInclusionStrategy strategy) {
    _strategies.add(strategy);
  }

  public boolean isStateIncluded(TripState state) {

    for (TripStateInclusionStrategy strategy : _strategies) {
      if (!strategy.isStateIncluded(state))
        return false;
    }

    return true;
  }
}
