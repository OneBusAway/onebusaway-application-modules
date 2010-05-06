package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;

import java.util.Set;

public interface TripPlannerStateTransition {
  
  public void getForwardTransitions(TripState state, Set<TripState> transitions);

  public void getReverseTransitions(TripState state, Set<TripState> transitions);
}
