package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.MinHeap;

import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripState;

import java.util.HashSet;
import java.util.Set;

public class OneBusAwayTripStateStrategy implements TripStrategy {

  private MinHeap<TripState> _queue = new MinHeap<TripState>();

  private MultiTripInclusionStrategy _inclusionStrategy;

  private Set<TripState> _allNodes = new HashSet<TripState>();

  private Set<TripState> _leafNodes = new HashSet<TripState>();

  public OneBusAwayTripStateStrategy(long maxTime) {
    _inclusionStrategy = new MultiTripInclusionStrategy();
    _inclusionStrategy.addInclusionStrategy(new MaxTimeTripStateInclusionStrategy(
        maxTime));
    _inclusionStrategy.addInclusionStrategy(new MaxTransferCountTripStateInclusionStrategy(
        1));
  }

  public void addState(TripContext context, TripState state) {
    if (_inclusionStrategy.isStateIncluded(state)) {
      _queue.add(state);
      _allNodes.add(state);
      _leafNodes.add(state);
      if (state.getPreviousState() != null)
        _leafNodes.remove(state.getPreviousState());
    }
  }

  public TripState getNextState() {
    if (_queue.isEmpty())
      return null;
    return _queue.removeMin();
  }
}
