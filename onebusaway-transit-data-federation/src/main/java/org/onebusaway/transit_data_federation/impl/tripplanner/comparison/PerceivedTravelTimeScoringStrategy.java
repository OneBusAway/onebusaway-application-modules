package org.onebusaway.transit_data_federation.impl.tripplanner.comparison;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStats;

public class PerceivedTravelTimeScoringStrategy implements TripStatsScoringStrategy {

  private TripPlannerConstants _constants;

  public PerceivedTravelTimeScoringStrategy(TripPlannerConstants constants) {
    _constants = constants;
  }

  public double getTripScore(TripStats trip) {

    double score = trip.getVehicleTime();

    double walkingDistance = trip.getTotalWalkingDistance();
    double walkingTime = walkingDistance / _constants.getWalkingVelocity();
    score += walkingTime * _constants.getWalkTimePenaltyRatio();

    score += trip.getInitialWaitingTime() * _constants.getInitialWaitTimePenaltyRatio();

    score += trip.getTransferWaitingTime() * _constants.getTransferWaitTimePenaltyRatio();

    if (trip.getVehicleCount() > 1) {
      int transfers = trip.getVehicleCount() - 1;
      if (!_constants.isTransferPenaltyAdditive())
        transfers = 1;
      score += transfers * _constants.getTransferPenalty();
    }

    return score;
  }

}
