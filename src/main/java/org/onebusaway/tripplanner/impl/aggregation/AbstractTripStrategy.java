/**
 * 
 */
package org.onebusaway.tripplanner.impl.aggregation;

import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripAggregationStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTripStrategy implements TripAggregationStrategy {

  private TripPlannerConstants _constants;

  public AbstractTripStrategy(TripPlannerConstants constants) {
    _constants = constants;
  }

  protected Set<String> getTripIds(TripPlan trip) {
    Set<String> tripIds = new HashSet<String>();
    for (TripState state : trip.getStates()) {

      if (state instanceof VehicleDepartureState) {
        VehicleDepartureState vds = (VehicleDepartureState) state;
        StopTimeInstanceProxy sti = vds.getStopTimeInstance();
        StopTimeProxy st = sti.getStopTime();
        String tripId = st.getTripId();
        tripIds.add(tripId);
      }

    }
    return tripIds;
  }

  protected Map<String, Long> getTripIdsAndVehicleTime(TripPlan trip) {
    Map<String, Long> results = new HashMap<String, Long>();

    TripState prev = null;
    for (TripState state : trip.getStates()) {
      if (prev != null) {
        if (prev instanceof VehicleDepartureState) {

          VehicleDepartureState vds = (VehicleDepartureState) prev;
          StopTimeInstanceProxy sti = vds.getStopTimeInstance();
          StopTimeProxy st = sti.getStopTime();
          String tripId = st.getTripId();
          long duration = state.getCurrentTime() - prev.getCurrentTime();
          addTime(results, tripId, duration);
        } else if (prev instanceof BlockTransferState) {
          BlockTransferState bt = (BlockTransferState) prev;
          String tripId = bt.getNextTripId();
          long duration = state.getCurrentTime() - prev.getCurrentTime();
          addTime(results, tripId, duration);
        }
      }
      prev = state;
    }
    return results;

  }

  private void addTime(Map<String, Long> results, String tripId, long time) {
    Long existingTime = results.get(tripId);
    if (existingTime == null)
      existingTime = 0L;
    results.put(tripId, existingTime + time);
  }

  protected String getTripKeyForTrip(TripPlan trip) {
    StringBuilder b = new StringBuilder();
    for (TripState state : trip.getStates()) {
      if (state instanceof VehicleDepartureState) {
        VehicleDepartureState vds = (VehicleDepartureState) state;
        StopTimeInstanceProxy sti = vds.getStopTimeInstance();
        StopTimeProxy st = sti.getStopTime();
        if (b.length() > 0)
          b.append(',');
        b.append(st.getTripId());
      }
    }

    return b.toString();
  }

  protected double scoreTrip(TripPlan trip) {

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