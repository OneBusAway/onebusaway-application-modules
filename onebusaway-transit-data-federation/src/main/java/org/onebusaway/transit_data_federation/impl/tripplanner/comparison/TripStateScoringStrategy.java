package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;

public interface TripStateScoringStrategy {

  public abstract double getMinScoreForTripState(TripState state) throws NoPathException;

}