package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripStats;

public interface TripStatsScoringStrategy {
  public double getTripScore(TripStats stats);
}
