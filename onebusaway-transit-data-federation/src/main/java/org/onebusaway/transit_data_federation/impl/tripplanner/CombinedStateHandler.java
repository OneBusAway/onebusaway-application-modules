package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlansImpl;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleArrivalState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleContinuationState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleState;
import org.onebusaway.transit_data_federation.model.tripplanner.WaitingAtStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkFromStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombinedStateHandler {

  private static Logger _log = LoggerFactory.getLogger(CombinedStateHandler.class);

  private WalkPlansImpl _walkPlans;

  private TripPlannerConstants _constants;

  private TransitGraphDao _transitGraphDao;

  private WalkPlannerService _walkPlanner;

  private CoordinatePoint _endPoint;

  private WalkPlan _walkFromStartToEndpointPlan;

  private Map<StopEntry, WalkPlan> _walkFromStopsToEndpointPlans;

  private StopTimeService _stopTimeService;

  private StopTransferService _stopTransferService;

  private int _stopTimeSearchWindow = 30;

  public CombinedStateHandler(TripContext context) {
    _walkPlans = context.getWalkPlans();
    _constants = context.getConstants();
    _transitGraphDao = context.getTransitGraphDao();
    _walkPlanner = context.getWalkPlannerService();
    _stopTimeService = context.getStopTimeService();
    _stopTransferService = context.getStopTransferService();
  }

  public void setEndPointWalkPlans(CoordinatePoint endPoint,
      WalkPlan walkFromStart, Map<StopEntry, WalkPlan> walkFromStops) {
    _endPoint = endPoint;
    _walkFromStartToEndpointPlan = walkFromStart;
    _walkFromStopsToEndpointPlans = walkFromStops;
  }

  /*****************************************************************************
   * {@link TripPlannerStateTransition} Interface
   ****************************************************************************/

  public void getForwardTransitions(TripState state, Set<TripState> transitions) {
    if (state instanceof WaitingAtStopState)
      getWaitingAtStopForwardTransitions((WaitingAtStopState) state,
          transitions);
    else if (state instanceof VehicleDepartureState
        || state instanceof VehicleContinuationState)
      getVehicleDepartureOrContinuationForwardTransitions((VehicleState) state,
          transitions);
    else if (state instanceof VehicleArrivalState)
      getVehicleArrivalForwardTransitions((VehicleArrivalState) state,
          transitions);
    else if (state instanceof WalkToStopState)
      getWalkToStopForwardTransitions((WalkToStopState) state, transitions);
    else if (state instanceof WalkFromStopState)
      getWalkFromStopForwardTransitions((WalkFromStopState) state, transitions);
    else if (state instanceof BlockTransferState)
      getBlockTransferForwardTransitions((BlockTransferState) state,
          transitions);
    else if (state instanceof StartState)
      getStartForwardTransitions((StartState) state, transitions);
  }

  public void getReverseTransitions(TripState state, Set<TripState> transitions) {

  }

  /*****************************************************************************
   * Start
   ****************************************************************************/

  public void getStartForwardTransitions(StartState state,
      Set<TripState> transitions) {

    double d = _constants.getMaxTransferDistance();
    CoordinateBounds bounds = DistanceLibrary.bounds(state.getLocation(), d);
    List<StopEntry> stopEntries = _transitGraphDao.getStopsByLocation(bounds);

    for (StopEntry stop : stopEntries) {

      try {
        WalkPlan plan = _walkPlanner.getWalkPlan(state.getLocation(),
            stop.getStopLocation());
        double walkingDistance = plan.getDistance();
        double walkingTime = walkingDistance / _constants.getWalkingVelocity();
        long t = (long) (state.getCurrentTime() + walkingTime);
        WalkToStopState walkToStop = new WalkToStopState(t, stop);
        transitions.add(walkToStop);
        _walkPlans.putWalkPlan(state, walkToStop, plan);
      } catch (NoPathException ex) {

      }
    }

    if (_walkFromStartToEndpointPlan != null) {
      double walkingDistance = _walkFromStartToEndpointPlan.getDistance();
      double walkingTime = walkingDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);
      EndState endState = new EndState(t, _endPoint);
      transitions.add(endState);
      _walkPlans.putWalkPlan(state, endState, _walkFromStartToEndpointPlan);
    }
  }

  /*****************************************************************************
   * Walk From Stop
   ****************************************************************************/

  public void getWalkFromStopForwardTransitions(WalkFromStopState state,
      Set<TripState> transitions) {

    StopEntry entry = state.getStop();

    // StopEntriesWithValues transferMap = entry.getTransfers();
    List<StopTransfer> transfers = _stopTransferService.getTransfersFromStop(entry);

    for (StopTransfer transfer : transfers) {
      StopEntry nearbyEntry = transfer.getStop();
      if (nearbyEntry.equals(entry))
        continue;

      double transferDistance = transfer.getDistance();
      double walkingTime = transferDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);

      WalkToStopState walkToStop = new WalkToStopState(t, nearbyEntry);
      transitions.add(walkToStop);
      _walkPlans.putWalkDistance(state, walkToStop, transferDistance);
    }

    if (_walkFromStopsToEndpointPlans != null
        && _walkFromStopsToEndpointPlans.containsKey(entry)) {
      WalkPlan walkPlan = _walkFromStopsToEndpointPlans.get(entry);
      double walkingDistance = walkPlan.getDistance();
      double walkingTime = walkingDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);
      EndState endState = new EndState(t, _endPoint);
      transitions.add(endState);
      _walkPlans.putWalkPlan(state, endState, walkPlan);
    }
  }

  public void getWalkFromStopReverseTransitions(WalkFromStopState state,
      Set<TripState> transitions) {

    // TODO : change this
    transitions.add(new WaitingAtStopState(state.getCurrentTime(),
        state.getStop()));
  }

  /*****************************************************************************
   * Walk To Stop
   ****************************************************************************/

  public void getWalkToStopForwardTransitions(WalkToStopState state,
      Set<TripState> transitions) {
    long t = state.getCurrentTime() + _constants.getMinTransferTime();
    transitions.add(new WaitingAtStopState(t, state.getStop()));
  }

  public void getWalkToStopReverseTransitions(WalkToStopState state,
      Set<TripState> transitions) {

    StopEntry stopEntry = state.getStop();
    List<StopTransfer> transfers = _stopTransferService.getTransfersFromStop(stopEntry);

    for (StopTransfer transfer : transfers) {

      StopEntry nearbyEntry = transfer.getStop();
      if (nearbyEntry.equals(stopEntry))
        continue;

      double transferDistance = transfer.getDistance();
      double walkingTime = transferDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() - walkingTime);

      WalkFromStopState walkFromStop = new WalkFromStopState(t, nearbyEntry);
      transitions.add(walkFromStop);
      _walkPlans.putWalkDistance(walkFromStop, state, transferDistance);
    }
  }

  /*****************************************************************************
   * Waiting At Stop State
   ****************************************************************************/

  public void getWaitingAtStopForwardTransitions(WaitingAtStopState state,
      Set<TripState> transitions) {

    StopEntry stopEntry = state.getStop();

    /**
     * Look for departures in the next X minutes
     */
    Date from = new Date(state.getCurrentTime());
    Date to = new Date(SupportLibrary.getNextTimeWindow(_stopTimeSearchWindow,
        state.getCurrentTime()));

    List<StopTimeInstance> instances = _stopTimeService.getStopTimeInstancesInTimeRange(
        stopEntry, from, to);

    for (StopTimeInstance instance : instances) {
      
      long departureTime = instance.getDepartureTime();

      // Prune anything that doesn't have a departure in the proper range, since
      // the stopTimeService method will also return instances that arrive in
      // the target interval as well
      if (departureTime < from.getTime() || to.getTime() <= departureTime)
        continue;

      // If this is the last stop time in the block, don't continue
      if (!SupportLibrary.hasNextStopTime(instance))
        continue;

      VehicleDepartureState next = new VehicleDepartureState(instance);
      transitions.add(next);
    }

    transitions.add(new WaitingAtStopState(to.getTime(), state.getStop()));
  }

  public void getReverseTransitions(WaitingAtStopState state,
      Set<TripState> transitions) {

    StopEntry stopEntry = state.getStop();

    // _stopTimeService.getPreviousStopTimeArrival(stopEntry,
    // state.getCurrentTime());
    List<StopTimeInstance> arrivals = Collections.emptyList();

    if (arrivals.isEmpty()) {
      System.err.println("unlikely");
      return;
    }

    long prevTime = -1;
    for (StopTimeInstance sti : arrivals) {
      VehicleArrivalState prev = new VehicleArrivalState(sti);
      if (prevTime == -1 || prev.getCurrentTime() < prevTime)
        prevTime = prev.getCurrentTime();
      transitions.add(prev);
    }

    transitions.add(new WaitingAtStopState(prevTime - 1, state.getStop()));
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void getVehicleDepartureOrContinuationForwardTransitions(
      VehicleState state, Set<TripState> transitions) {

    StopTimeInstance sti = state.getStopTimeInstance();

    BlockTripEntry blockTrip = sti.getTrip();
    List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();

    int nextIndex = sti.getSequence() - blockTrip.getAccumulatedStopTimeIndex()
        + 1;

    if (nextIndex > stopTimes.size())
      throw new IllegalStateException("not good");

    if (nextIndex == stopTimes.size()) {

      BlockTripEntry nextTrip = blockTrip.getNextTrip();

      if (nextTrip != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            blockTrip, nextTrip, sti.getServiceDate()));
      }

    } else {
      BlockStopTimeEntry nextStopTime = stopTimes.get(nextIndex);

      StopTimeInstance nextSti = new StopTimeInstance(nextStopTime,
          sti.getServiceDate());

      // We can continue on
      transitions.add(new VehicleContinuationState(nextSti));

      /**
       * Or we can get off... but only if there is a reason to, such as:
       * 
       * 1) You can walk from this stop to the end point
       * 
       * 2) You can transfer at this stop to another route whose next stop is
       * different than the current route
       * 
       * 3) You can walk from this stop to another stop that services a route
       * that this stop doesn't
       */

      if (hasReasonToGetOffAtThisStop(nextSti))
        transitions.add(new VehicleArrivalState(nextSti));
    }
  }

  private boolean hasReasonToGetOffAtThisStop(StopTimeInstance nextSti) {

    StopEntry stopEntry = nextSti.getStop();

    if (_walkFromStopsToEndpointPlans != null
        && _walkFromStopsToEndpointPlans.containsKey(stopEntry))
      return true;

    if (stopEntry.getNextStopsWithMinTimes().size() > 1)
      return true;

    /*
     * if (!stopEntry.getTransfers().isEmpty()) return true;
     */

    return false;
  }

  public void getVehicleDepartureReverseTransitions(
      VehicleDepartureState state, Set<TripState> transitions) {

    long t = state.getCurrentTime() - _constants.getMinTransferTime();

    // We can either remain at the current stop
    transitions.add(new WaitingAtStopState(t, state.getStop()));

    // Or Walk to another stop
    transitions.add(new WalkToStopState(t, state.getStop()));
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void getVehicleContinuationAndArrivalReverseTransitions(
      VehicleState state, Set<TripState> transitions) {

    StopTimeInstance sti = state.getStopTimeInstance();
    BlockTripEntry blockTrip = sti.getTrip();

    List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
    int prevIndex = sti.getSequence() - blockTrip.getAccumulatedStopTimeIndex()
        - 1;

    if (prevIndex < 0) {

      BlockTripEntry prevTrip = blockTrip.getPreviousTrip();

      if (prevTrip != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            prevTrip, blockTrip, sti.getServiceDate()));
      }
    } else {
      BlockStopTimeEntry prevStopTime = stopTimes.get(prevIndex);

      StopTimeInstance nextSti = new StopTimeInstance(prevStopTime,
          sti.getServiceDate());

      // We can either get on at the previous stop
      transitions.add(new VehicleDepartureState(nextSti));

      // Or we can continue from the previous stop
      transitions.add(new VehicleContinuationState(nextSti));
    }
  }

  public void getVehicleArrivalForwardTransitions(VehicleArrivalState state,
      Set<TripState> transitions) {

    StopTimeInstance sti = state.getStopTimeInstance();

    // We can wait here
    transitions.add(new WaitingAtStopState(state.getCurrentTime()
        + _constants.getMinTransferTime(), sti.getStop()));

    // Or we can walk to another stop
    transitions.add(new WalkFromStopState(state.getCurrentTime() + 1,
        sti.getStop()));
  }

  /*****************************************************************************
   * Block Transfer
   ****************************************************************************/

  public void getBlockTransferForwardTransitions(BlockTransferState state,
      Set<TripState> transitions) {

    BlockTripEntry entry = state.getNextTrip();
    List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();
    BlockStopTimeEntry first = stopTimes.get(0);
    StopTimeInstance sti = new StopTimeInstance(first, state.getServiceDate());
    transitions.add(new VehicleContinuationState(sti));
    transitions.add(new VehicleArrivalState(sti));
  }

  public void getBlockTransferReverseTransitions(BlockTransferState state,
      Set<TripState> transitions) {

    BlockTripEntry entry = state.getPrevTrip();
    List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();

    BlockStopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
    StopTimeInstance sti = new StopTimeInstance(last, state.getServiceDate());
    transitions.add(new VehicleContinuationState(sti));
    transitions.add(new VehicleDepartureState(sti));
  }

  /*****************************************************************************
   * End
   ****************************************************************************/

  public void getEndReverseTransitions(EndState state,
      Set<TripState> transitions) {

    double d = _constants.getMaxTransferDistance();
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        state.getLocation(), d);
    List<StopEntry> stopEntries = _transitGraphDao.getStopsByLocation(bounds);

    for (StopEntry stop : stopEntries) {

      try {
        WalkPlan plan = _walkPlanner.getWalkPlan(state.getLocation(),
            stop.getStopLocation());
        double walkingDistance = plan.getDistance();
        double walkingTime = walkingDistance / _constants.getWalkingVelocity();
        long t = (long) (state.getCurrentTime() - walkingTime);
        WalkFromStopState walkFromStop = new WalkFromStopState(t, stop);
        transitions.add(walkFromStop);
        _walkPlans.putWalkPlan(walkFromStop, state, plan);
      } catch (NoPathException ex) {

      }
    }

  }

}
