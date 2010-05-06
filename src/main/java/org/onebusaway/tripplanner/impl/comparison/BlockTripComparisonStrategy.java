/**
 * 
 */
package org.onebusaway.tripplanner.impl.comparison;

import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.TripStateStats;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.services.ETripComparison;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;

import java.util.HashSet;
import java.util.Set;

public class BlockTripComparisonStrategy implements TripComparisonStrategy {

  private TripPlannerConstraints _constraints;

  public BlockTripComparisonStrategy(TripPlannerConstraints constraints) {
    _constraints = constraints;
  }

  public ETripComparison compare(TripStateStats statsA, TripStateStats statsB) {

    Set<String> tripIdsA = getTripIds(statsA);
    Set<String> tripIdsB = getTripIds(statsB);

    boolean haveCommonTripIds = false;
    boolean haveAllCommonTripIds = tripIdsA.equals(tripIdsB);

    for (String tripId : tripIdsB) {
      if (tripIdsA.contains(tripId))
        haveCommonTripIds = true;
    }

    if (haveAllCommonTripIds) {
      return statsA.getScore() == statsB.getScore() ? ETripComparison.KEEP_BOTH
          : (statsA.getScore() < statsB.getScore() ? ETripComparison.KEEP_A : ETripComparison.KEEP_B);
    } else if (haveCommonTripIds) {
      if (_constraints.hasMaxTripDurationRatio()) {
        double scoreA = statsA.getScore();
        double scoreB = statsB.getScore();
        if (scoreA < scoreB) {
          return scoreB > scoreA * _constraints.getMaxTripDurationRatio() ? ETripComparison.KEEP_A
              : ETripComparison.KEEP_BOTH;
        } else {
          return scoreA > scoreB * _constraints.getMaxTripDurationRatio() ? ETripComparison.KEEP_B
              : ETripComparison.KEEP_BOTH;
        }
      }
    }

    return ETripComparison.NOT_COMPARABLE;
  }

  private Set<String> getTripIds(TripStateStats stats) {

    Set<String> tripIds = new HashSet<String>();

    while (stats != null) {

      TripState state = stats.getState();

      if (state instanceof VehicleDepartureState) {
        VehicleDepartureState vds = (VehicleDepartureState) state;
        StopTimeInstanceProxy sti = vds.getStopTimeInstance();
        StopTimeProxy st = sti.getStopTime();
        String tripId = st.getTripId();
        tripIds.add(tripId);
      }
      stats = stats.getParent();
    }
    return tripIds;
  }
}