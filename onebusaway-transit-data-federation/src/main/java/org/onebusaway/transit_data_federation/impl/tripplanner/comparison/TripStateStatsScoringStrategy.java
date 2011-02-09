package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;

public interface TripStateStatsScoringStrategy {
  public double getTripStateStatsScore(TripStateStats stats);
}
