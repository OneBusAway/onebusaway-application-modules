package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlansImpl;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;
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
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexResult;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;

public class CombinedStateHandler {

  private WalkPlansImpl _walkPlans;

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  private WalkPlannerService _walkPlanner;

  private StopTimeIndexContext _indexContext;

  private CoordinatePoint _endPoint;

  private WalkPlan _walkFromStartToEndpointPlan;

  private Map<StopEntry, WalkPlan> _walkFromStopsToEndpointPlans;

  public CombinedStateHandler(TripContext context) {
    _walkPlans = context.getWalkPlans();
    _constants = context.getConstants();
    _graph = context.getGraph();
    _walkPlanner = context.getWalkPlannerService();
    _indexContext = context;
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
    List<StopEntry> stopEntries = _graph.getStopsByLocation(bounds);

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
    StopEntriesWithValues transferMap = entry.getTransfers();

    for (int i = 0; i < transferMap.size(); i++) {

      StopEntry nearbyEntry = transferMap.getStopEntry(i);
      if (nearbyEntry.equals(entry))
        continue;

      int transferDistance = transferMap.getValue(i);
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
    StopEntriesWithValues transferMap = stopEntry.getTransfers();

    for (int i = 0; i < transferMap.size(); i++) {

      StopEntry nearbyEntry = transferMap.getStopEntry(i);
      if (nearbyEntry.equals(stopEntry))
        continue;

      int transferDistance = transferMap.getValue(i);
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
    StopTimeIndex stopTimeIndex = stopEntry.getStopTimes();

    StopTimeIndexResult result = StopTimeSearchOperations.getNextStopTimeDeparture(
        stopTimeIndex, _indexContext, state.getCurrentTime(), null);
    List<StopTimeInstanceProxy> departures = result.getStopTimeInstances();

    if (departures.isEmpty()) {
      System.err.println("unlikely");
      return;
    }

    long nextTime = -1;
    for (StopTimeInstanceProxy sti : departures) {
      VehicleDepartureState next = new VehicleDepartureState(sti);
      if (nextTime == -1 || next.getCurrentTime() > nextTime)
        nextTime = next.getCurrentTime();
      transitions.add(next);
    }

    transitions.add(new WaitingAtStopState(nextTime + 1, state.getStop()));
  }

  public void getReverseTransitions(WaitingAtStopState state,
      Set<TripState> transitions) {

    StopEntry stopEntry = state.getStop();
    StopTimeIndex stopTimeIndex = stopEntry.getStopTimes();

    StopTimeIndexResult result = StopTimeSearchOperations.getPreviousStopTimeArrival(
        stopTimeIndex, _indexContext, state.getCurrentTime(), null);

    List<StopTimeInstanceProxy> arrivals = result.getStopTimeInstances();

    if (arrivals.isEmpty()) {
      System.err.println("unlikely");
      return;
    }

    long prevTime = -1;
    for (StopTimeInstanceProxy sti : arrivals) {
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

    StopTimeInstanceProxy sti = state.getStopTimeInstance();

    TripEntry entry = sti.getTrip();
    List<StopTimeEntry> stopTimes = entry.getStopTimes();
    int nextIndex = sti.getSequence() + 1;

    if (nextIndex > stopTimes.size())
      throw new IllegalStateException("not good");

    if (nextIndex == stopTimes.size()) {

      TripEntry nextTrip = entry.getNextTrip();

      if (nextTrip != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(), entry,
            nextTrip, sti.getServiceDate()));
      }

    } else {
      StopTimeEntry nextStopTime = stopTimes.get(nextIndex);

      StopTimeInstanceProxy nextSti = new StopTimeInstanceProxy(nextStopTime,
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

  private boolean hasReasonToGetOffAtThisStop(StopTimeInstanceProxy nextSti) {

    StopEntry stopEntry = nextSti.getStopEntry();

    if (_walkFromStopsToEndpointPlans != null
        && _walkFromStopsToEndpointPlans.containsKey(stopEntry))
      return true;

    if (stopEntry.getNextStopsWithMinTimes().size() > 1)
      return true;

    if (!stopEntry.getTransfers().isEmpty())
      return true;

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

    StopTimeInstanceProxy sti = state.getStopTimeInstance();
    TripEntry entry = sti.getTrip();

    List<StopTimeEntry> stopTimes = entry.getStopTimes();
    int prevIndex = sti.getSequence() - 1;

    if (prevIndex < 0) {

      TripEntry prevTrip = entry.getPrevTrip();

      if (prevTrip != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            prevTrip, entry, sti.getServiceDate()));
      }
    } else {
      StopTimeEntry prevStopTime = stopTimes.get(prevIndex);

      StopTimeInstanceProxy nextSti = new StopTimeInstanceProxy(prevStopTime,
          sti.getServiceDate());

      // We can either get on at the previous stop
      transitions.add(new VehicleDepartureState(nextSti));

      // Or we can continue from the previous stop
      transitions.add(new VehicleContinuationState(nextSti));
    }
  }

  public void getVehicleArrivalForwardTransitions(VehicleArrivalState state,
      Set<TripState> transitions) {

    StopTimeInstanceProxy sti = state.getStopTimeInstance();

    // We can wait here
    transitions.add(new WaitingAtStopState(state.getCurrentTime()
        + _constants.getMinTransferTime(), sti.getStopEntry()));

    // Or we can walk to another stop
    transitions.add(new WalkFromStopState(state.getCurrentTime() + 1,
        sti.getStopEntry()));
  }

  /*****************************************************************************
   * Block Transfer
   ****************************************************************************/

  public void getBlockTransferForwardTransitions(BlockTransferState state,
      Set<TripState> transitions) {

    TripEntry entry = state.getNextTrip();
    List<StopTimeEntry> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      TripEntry nextTrip = entry.getNextTrip();
      if (nextTrip != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(), entry,
            nextTrip, state.getServiceDate()));
    } else {
      StopTimeEntry first = stopTimes.get(0);
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(first,
          state.getServiceDate());
      transitions.add(new VehicleContinuationState(sti));
      transitions.add(new VehicleArrivalState(sti));
    }
  }

  public void getBlockTransferReverseTransitions(BlockTransferState state,
      Set<TripState> transitions) {

    TripEntry entry = state.getPrevTrip();
    List<StopTimeEntry> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      TripEntry prevTrip = entry.getPrevTrip();
      if (prevTrip != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            prevTrip, entry, state.getServiceDate()));
    } else {
      StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(last,
          state.getServiceDate());
      transitions.add(new VehicleContinuationState(sti));
      transitions.add(new VehicleDepartureState(sti));
    }
  }

  /*****************************************************************************
   * End
   ****************************************************************************/

  public void getEndReverseTransitions(EndState state,
      Set<TripState> transitions) {

    double d = _constants.getMaxTransferDistance();
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        state.getLocation(), d);
    List<StopEntry> stopEntries = _graph.getStopsByLocation(bounds);

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
