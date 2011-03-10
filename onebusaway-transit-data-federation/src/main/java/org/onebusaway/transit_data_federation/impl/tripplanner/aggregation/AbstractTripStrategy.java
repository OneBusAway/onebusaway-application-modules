/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.aggregation;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.TripAggregationStrategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTripStrategy implements TripAggregationStrategy {

  private TripPlannerConstants _constants;

  public AbstractTripStrategy(TripPlannerConstants constants) {
    _constants = constants;
  }

  protected Set<AgencyAndId> getTripIds(TripPlan trip) {
    Set<AgencyAndId> tripIds = new HashSet<AgencyAndId>();
    for (TripState state : trip.getStates()) {

      if (state instanceof VehicleDepartureState) {
        VehicleDepartureState vds = (VehicleDepartureState) state;
        StopTimeInstance sti = vds.getStopTimeInstance();
        AgencyAndId tripId = sti.getTrip().getTrip().getId();
        tripIds.add(tripId);
      }

    }
    return tripIds;
  }

  protected Map<AgencyAndId, Long> getTripIdsAndVehicleTime(TripPlan trip) {
    
    Map<AgencyAndId, Long> results = new HashMap<AgencyAndId, Long>();

    TripState prev = null;
    for (TripState state : trip.getStates()) {
      if (prev != null) {
        if (prev instanceof VehicleDepartureState) {

          VehicleDepartureState vds = (VehicleDepartureState) prev;
          StopTimeInstance sti = vds.getStopTimeInstance();
          AgencyAndId tripId = sti.getTrip().getTrip().getId();
          long duration = state.getCurrentTime() - prev.getCurrentTime();
          addTime(results, tripId, duration);
        } else if (prev instanceof BlockTransferState) {
          BlockTransferState bt = (BlockTransferState) prev;
          AgencyAndId tripId = bt.getNextTrip().getTrip().getId();
          long duration = state.getCurrentTime() - prev.getCurrentTime();
          addTime(results, tripId, duration);
        }
      }
      prev = state;
    }
    return results;

  }

  private void addTime(Map<AgencyAndId, Long> results, AgencyAndId tripId, long time) {
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
        StopTimeInstance sti = vds.getStopTimeInstance();
        if (b.length() > 0)
          b.append(',');
        b.append(sti.getTrip().getTrip().getId());
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