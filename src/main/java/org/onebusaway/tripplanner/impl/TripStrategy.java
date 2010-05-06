package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripState;

public interface TripStrategy {

  public void addState(TripContext context, TripState state);

  public TripState getNextState();
}
