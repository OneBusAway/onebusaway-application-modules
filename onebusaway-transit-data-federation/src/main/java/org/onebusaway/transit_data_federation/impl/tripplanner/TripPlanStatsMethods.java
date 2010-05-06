package org.onebusaway.transit_data_federation.impl.tripplanner;

import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlansImpl;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStateStats;
import org.onebusaway.transit_data_federation.model.tripplanner.TripStats;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleArrivalState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleContinuationState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.model.tripplanner.WaitingAtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkFromStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlanSource;

public class TripPlanStatsMethods {

  private TripContext _context;

  public TripPlanStatsMethods(TripContext context) {
    _context = context;
  }

  public TripStateStats extendTripStateStats(TripStateStats stats, TripState to, WalkPlanSource walkPlans, int index) {

    TripStateStats extended = new TripStateStats(stats, to, stats.getTripStartTime(),index);

    TripState from = stats.getState();
    long timeDelta = to.getCurrentTime() - from.getCurrentTime();
    int vehicleCount = stats.getVehicleCount();

    if (from instanceof VehicleDepartureState || from instanceof VehicleContinuationState
        || from instanceof BlockTransferState)
      extended.setVehicleTime(extended.getVehicleTime() + timeDelta);

    if (from instanceof WalkFromStopState || from instanceof StartState) {
      double walkDistance = walkPlans.getWalkDistance(from, to);
      if (walkDistance > extended.getMaxSingleWalkDistance())
        extended.setMaxSingleWalkDistance(walkDistance);
      extended.setTotalWalkingDistance(extended.getTotalWalkingDistance() + walkDistance);
    }

    if (from instanceof WaitingAtStopState || to instanceof WaitingAtStopState) {
      if (vehicleCount == 0)
        extended.setInitialWaitingTime(extended.getInitialWaitingTime() + timeDelta);
      else
        extended.setTransferWaitingTime(extended.getTransferWaitingTime() + timeDelta);
    }

    if (to instanceof VehicleDepartureState)
      extended.setVehicleCount(vehicleCount + 1);

    return extended;
  }

  public TripStats updateTransitionStats(TripState from, TripState to, TripStats fromStats, TripStats toStats,
      boolean newStats) {

    int vehicleCount = fromStats.getVehicleCount();

    long initialWaitingTime = fromStats.getInitialWaitingTime();
    long transferWaitingTime = fromStats.getTransferWaitingTime();
    long vehicleTime = fromStats.getVehicleTime();

    double maxSingleWalkDistance = fromStats.getMaxSingleWalkDistance();
    double totalWalkingDistance = fromStats.getTotalWalkingDistance();

    if (from instanceof StartState) {

      assert (to instanceof WalkToStopState);
      //firstWalkDuration = to.getCurrentTime() - from.getCurrentTime();

    } else if (from instanceof WalkToStopState && vehicleCount == 0) {

      assert (to instanceof WaitingAtStopState);
      initialWaitingTime += to.getCurrentTime() - from.getCurrentTime();

    } else if (from instanceof WaitingAtStopState) {

      if (vehicleCount == 0)
        initialWaitingTime += to.getCurrentTime() - from.getCurrentTime();
      else
        transferWaitingTime += to.getCurrentTime() - from.getCurrentTime();

      if (to instanceof VehicleDepartureState) {

        // If this is our first departure
        if (vehicleCount == 0) {
          //TripPlannerConstants constants = _context.getConstants();
          //tripStartTime = to.getCurrentTime() - constants.getMinTransferTime() - firstWalkDuration;
        }

        // Update the vehicle count either way
        vehicleCount++;
      }
    } else if (to instanceof VehicleDepartureState) {

      assert (from instanceof WaitingAtStopState);

      // If this is our first departure
      if (vehicleCount == 0) {
        //TripPlannerConstants constants = _context.getConstants();
        //tripStartTime = to.getCurrentTime() - constants.getMinTransferTime() - firstWalkDuration;
      }

      vehicleCount++;
    } else if (from instanceof VehicleArrivalState) {
      if (to instanceof WaitingAtStopState)
        transferWaitingTime += to.getCurrentTime() - from.getCurrentTime();
    }

    // Handle a walk
    if (from instanceof StartState || from instanceof WalkFromStopState) {
      WalkPlansImpl walkPlans = _context.getWalkPlans();
      double d = walkPlans.getWalkDistance(from, to);
      totalWalkingDistance += d;
      maxSingleWalkDistance = Math.max(maxSingleWalkDistance, d);
    }

    // Handle vehicle time
    if (from instanceof VehicleDepartureState || from instanceof VehicleContinuationState
        || from instanceof BlockTransferState)
      vehicleTime += to.getCurrentTime() - from.getCurrentTime();

    if (newStats || maxSingleWalkDistance < toStats.getMaxSingleWalkDistance())
      toStats.setMaxSingleWalkDistance(maxSingleWalkDistance);

    if (newStats || totalWalkingDistance < toStats.getTotalWalkingDistance())
      toStats.setTotalWalkingDistance(totalWalkingDistance);

    if (newStats || initialWaitingTime < toStats.getInitialWaitingTime())
      toStats.setInitialWaitingTime(initialWaitingTime);

    if (newStats || transferWaitingTime < toStats.getTransferWaitingTime())
      toStats.setTransferWaitingTime(transferWaitingTime);

    if (newStats || vehicleTime < toStats.getVehicleTime())
      toStats.setVehicleTime(vehicleTime);

    if (newStats || vehicleCount < toStats.getVehicleCount())
      toStats.setVehicleCount(vehicleCount);

    return toStats;
  }

  public void updateTripPlanStatistics(TripPlan trip) {

    long tripStartTime = Long.MAX_VALUE;
    long tripEndTime = Long.MIN_VALUE;

    long initialWaitingTime = 0;
    long transferWaitingTime = 0;
    long vehicleTime = 0;

    int vehicleCount = 0;

    double maxSingleWalkDistance = 0;
    double totalWalkDistance = 0;

    TripState prev = null;

    for (TripState state : trip.getStates()) {

      tripStartTime = Math.min(tripStartTime, state.getCurrentTime());
      tripEndTime = Math.max(tripEndTime, state.getCurrentTime());

      if (prev != null) {

        if (prev instanceof VehicleDepartureState || prev instanceof VehicleContinuationState
            || prev instanceof BlockTransferState)
          vehicleTime += state.getCurrentTime() - prev.getCurrentTime();

        WalkPlanSource walkPlans = trip.getWalkPlans();

        if (prev instanceof WalkFromStopState || prev instanceof StartState) {
          double walkDistance = walkPlans.getWalkDistance(prev, state);
          maxSingleWalkDistance = Math.max(maxSingleWalkDistance, walkDistance);
          totalWalkDistance += walkDistance;
        }

        if (prev instanceof WaitingAtStopState || state instanceof WaitingAtStopState) {
          long waitingTime = state.getCurrentTime() - prev.getCurrentTime();
          if (vehicleCount == 0)
            initialWaitingTime += waitingTime;
          else
            transferWaitingTime += waitingTime;
        }
      }

      if (state instanceof VehicleDepartureState)
        vehicleCount++;

      prev = state;
    }

    trip.setInitialWaitingTime(initialWaitingTime);
    trip.setTransferWaitingTime(transferWaitingTime);
    trip.setVehicleTime(vehicleTime);

    trip.setMaxSingleWalkDistance(maxSingleWalkDistance);
    trip.setTotalWalkingDistance(totalWalkDistance);

    trip.setVehicleCount(vehicleCount);
  }
}
