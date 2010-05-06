package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripStats;

public class DefaultTravelTimeScoringStrategy implements TripStatsScoringStrategy {

  public double getTripScore(TripStats trip) {
    return trip.getTripDuration();
  }
}
