package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripStats;

public interface TripStatsScoringStrategy {
  public double getTripScore(TripStats stats);
}
