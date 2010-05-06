package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.services.NoPathException;

public interface TripStateScoringStrategy {

  public abstract double getMinScoreForTripState(TripState state) throws NoPathException;

}