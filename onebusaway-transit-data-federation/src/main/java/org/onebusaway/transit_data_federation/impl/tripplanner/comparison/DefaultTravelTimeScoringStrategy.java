package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripStats;

public class DefaultTravelTimeScoringStrategy implements TripStatsScoringStrategy {

  public double getTripScore(TripStats trip) {
    return trip.getTripDuration();
  }
}
