package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripStateStats;

public interface TripStateStatsScoringStrategy {
  public double getTripStateStatsScore(TripStateStats stats);
}
