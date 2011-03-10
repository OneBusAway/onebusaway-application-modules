/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.services.tripplanner.ETripComparison;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

import java.util.HashSet;
import java.util.Set;

public class BlockTripComparisonStrategy implements TripComparisonStrategy {

  private TripPlannerConstraints _constraints;

  public BlockTripComparisonStrategy(TripPlannerConstraints constraints) {
    _constraints = constraints;
  }

  public ETripComparison compare(TripStateStats statsA, TripStateStats statsB) {

    Set<AgencyAndId> tripIdsA = getTripIds(statsA);
    Set<AgencyAndId> tripIdsB = getTripIds(statsB);

    boolean haveCommonTripIds = false;
    boolean haveAllCommonTripIds = tripIdsA.equals(tripIdsB);

    for (AgencyAndId tripId : tripIdsB) {
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

  private Set<AgencyAndId> getTripIds(TripStateStats stats) {

    Set<AgencyAndId> tripIds = new HashSet<AgencyAndId>();

    while (stats != null) {

      TripState state = stats.getState();

      if (state instanceof VehicleDepartureState) {
        VehicleDepartureState vds = (VehicleDepartureState) state;
        StopTimeInstance sti = vds.getStopTimeInstance();
        AgencyAndId tripId = sti.getTrip().getTrip().getId();
        tripIds.add(tripId);
      }
      stats = stats.getParent();
    }
    return tripIds;
  }
}