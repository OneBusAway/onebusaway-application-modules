package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.TripContext;

import java.util.Set;

public interface TripPlannerStateTransition {
  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions);
}
