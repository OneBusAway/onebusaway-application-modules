package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.StopIdsWithValues;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.VehicleState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeIndex;
import org.onebusaway.tripplanner.services.StopTimeIndexContext;
import org.onebusaway.tripplanner.services.StopTimeIndexResult;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;
import org.onebusaway.tripplanner.services.WalkPlannerService;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CombinedStateHandler implements TripPlannerStateTransition {

  private WalkPlansImpl _walkPlans;

  private TripPlannerConstants _constants;

  private TripPlannerGraph _graph;

  private WalkPlannerService _walkPlanner;

  private StopTimeIndexContext _indexContext;

  private Point _endPoint;

  private WalkPlan _walkFromStartToEndpointPlan;

  private Map<String, WalkPlan> _walkFromStopsToEndpointPlans;

  public CombinedStateHandler(TripContext context) {
    _walkPlans = context.getWalkPlans();
    _constants = context.getConstants();
    _graph = context.getGraph();
    _walkPlanner = context.getWalkPlannerService();
    _indexContext = context;
  }

  public void setEndPointWalkPlans(Point endPoint, WalkPlan walkFromStart, Map<String, WalkPlan> walkFromStops) {
    _endPoint = endPoint;
    _walkFromStartToEndpointPlan = walkFromStart;
    _walkFromStopsToEndpointPlans = walkFromStops;
  }

  /*****************************************************************************
   * {@link TripPlannerStateTransition} Interface
   ****************************************************************************/

  public void getForwardTransitions(TripState state, Set<TripState> transitions) {
    if (state instanceof WaitingAtStopState)
      getWaitingAtStopForwardTransitions((WaitingAtStopState) state, transitions);
    else if (state instanceof VehicleDepartureState || state instanceof VehicleContinuationState)
      getVehicleDepartureOrContinuationForwardTransitions((VehicleState) state, transitions);
    else if (state instanceof VehicleArrivalState)
      getVehicleArrivalForwardTransitions((VehicleArrivalState) state, transitions);
    else if (state instanceof WalkToStopState)
      getWalkToStopForwardTransitions((WalkToStopState) state, transitions);
    else if (state instanceof WalkFromStopState)
      getWalkFromStopForwardTransitions((WalkFromStopState) state, transitions);
    else if (state instanceof BlockTransferState)
      getBlockTransferForwardTransitions((BlockTransferState) state, transitions);
    else if (state instanceof StartState)
      getStartForwardTransitions((StartState) state, transitions);
  }

  public void getReverseTransitions(TripState state, Set<TripState> transitions) {

  }

  /*****************************************************************************
   * Start
   ****************************************************************************/

  public void getStartForwardTransitions(StartState state, Set<TripState> transitions) {

    Point location = state.getLocation();
    Geometry boundary = location.buffer(5280 * 1 / 2).getBoundary();
    List<String> stopIds = _graph.getStopsByLocation(boundary);

    for (String stopId : stopIds) {

      try {
        StopEntry entry = _graph.getStopEntryByStopId(stopId);
        StopProxy stop = entry.getProxy();
        WalkPlan plan = _walkPlanner.getWalkPlan(location, stop.getStopLocation());
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

  public void getWalkFromStopForwardTransitions(WalkFromStopState state, Set<TripState> transitions) {

    String stopId = state.getStopId();
    StopEntry entry = _graph.getStopEntryByStopId(stopId);
    StopIdsWithValues transferMap = entry.getTransfers();

    for (int i = 0; i < transferMap.size(); i++) {

      String id = transferMap.getStopId(i);
      if (id.equals(stopId))
        continue;

      int transferDistance = transferMap.getValue(i);
      double walkingTime = transferDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);

      StopEntry nearbyEntry = _graph.getStopEntryByStopId(id);

      WalkToStopState walkToStop = new WalkToStopState(t, nearbyEntry.getProxy());
      transitions.add(walkToStop);
      _walkPlans.putWalkDistance(state, walkToStop, transferDistance);
    }

    if (_walkFromStopsToEndpointPlans != null && _walkFromStopsToEndpointPlans.containsKey(stopId)) {
      WalkPlan walkPlan = _walkFromStopsToEndpointPlans.get(stopId);
      double walkingDistance = walkPlan.getDistance();
      double walkingTime = walkingDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() + walkingTime);
      EndState endState = new EndState(t, _endPoint);
      transitions.add(endState);
      _walkPlans.putWalkPlan(state, endState, walkPlan);
    }
  }

  public void getWalkFromStopReverseTransitions(WalkFromStopState state, Set<TripState> transitions) {

    // TODO : change this
    transitions.add(new WaitingAtStopState(state.getCurrentTime(), state.getStop()));
  }

  /*****************************************************************************
   * Walk To Stop
   ****************************************************************************/

  public void getWalkToStopForwardTransitions(WalkToStopState state, Set<TripState> transitions) {
    long t = state.getCurrentTime() + _constants.getMinTransferTime();
    transitions.add(new WaitingAtStopState(t, state.getStop()));
  }

  public void getWalkToStopReverseTransitions(WalkToStopState state, Set<TripState> transitions) {

    String stopId = state.getStopId();
    StopEntry entry = _graph.getStopEntryByStopId(stopId);
    StopIdsWithValues transferMap = entry.getTransfers();

    for (int i = 0; i < transferMap.size(); i++) {

      String id = transferMap.getStopId(i);
      if (id.equals(stopId))
        continue;

      int transferDistance = transferMap.getValue(i);
      double walkingTime = transferDistance / _constants.getWalkingVelocity();
      long t = (long) (state.getCurrentTime() - walkingTime);

      StopEntry nearbyEntry = _graph.getStopEntryByStopId(id);

      WalkFromStopState walkFromStop = new WalkFromStopState(t, nearbyEntry.getProxy());
      transitions.add(walkFromStop);
      _walkPlans.putWalkDistance(walkFromStop, state, transferDistance);
    }
  }

  /*****************************************************************************
   * Waiting At Stop State
   ****************************************************************************/

  public void getWaitingAtStopForwardTransitions(WaitingAtStopState state, Set<TripState> transitions) {

    String stopId = state.getStopId();
    StopEntry stopEntry = _graph.getStopEntryByStopId(stopId);
    StopTimeIndex stopTimeIndex = stopEntry.getStopTimes();

    StopTimeIndexResult result = stopTimeIndex.getNextStopTimeDeparture(_indexContext, state.getCurrentTime(), null);
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

  public void getReverseTransitions(WaitingAtStopState state, Set<TripState> transitions) {

    String stopId = state.getStopId();

    StopEntry stopEntry = _graph.getStopEntryByStopId(stopId);
    StopTimeIndex stopTimeIndex = stopEntry.getStopTimes();

    StopTimeIndexResult result = stopTimeIndex.getPreviousStopTimeArrival(_indexContext, state.getCurrentTime(), null);

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

  public void getVehicleDepartureOrContinuationForwardTransitions(VehicleState state, Set<TripState> transitions) {

    StopTimeInstanceProxy sti = state.getStopTimeInstance();

    TripEntry entry = _graph.getTripEntryByTripId(sti.getTripId());
    List<StopTimeProxy> stopTimes = entry.getStopTimes();
    int nextIndex = sti.getSequence() + 1;

    if (nextIndex > stopTimes.size())
      throw new IllegalStateException("not good");

    if (nextIndex == stopTimes.size()) {

      String nextTripId = entry.getNextTripId();

      if (nextTripId != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(), state.getLocation(), sti.getTripId(),
            nextTripId, sti.getServiceDate()));
      }

    } else {
      StopTimeProxy nextStopTime = stopTimes.get(nextIndex);

      StopTimeInstanceProxy nextSti = new StopTimeInstanceProxy(nextStopTime, sti.getServiceDate());

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

    StopProxy stop = nextSti.getStop();
    StopEntry stopEntry = _graph.getStopEntryByStopId(stop.getStopId());

    if (_walkFromStopsToEndpointPlans != null && _walkFromStopsToEndpointPlans.containsKey(stop.getStopId()))
      return true;

    if (stopEntry.getNextStopsWithMinTimes().size() > 1)
      return true;

    if (!stopEntry.getTransfers().isEmpty())
      return true;

    return false;
  }

  public void getVehicleDepartureReverseTransitions(VehicleDepartureState state, Set<TripState> transitions) {

    long t = state.getCurrentTime() - _constants.getMinTransferTime();

    // We can either remain at the current stop
    transitions.add(new WaitingAtStopState(t, state.getStop()));

    // Or Walk to another stop
    transitions.add(new WalkToStopState(t, state.getStop()));
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void getVehicleContinuationAndArrivalReverseTransitions(VehicleState state, Set<TripState> transitions) {

    StopTimeInstanceProxy sti = state.getStopTimeInstance();
    TripEntry entry = _graph.getTripEntryByTripId(sti.getTripId());

    List<StopTimeProxy> stopTimes = entry.getStopTimes();
    int prevIndex = sti.getSequence() - 1;

    if (prevIndex < 0) {

      String prevTripId = entry.getPrevTripId();

      if (prevTripId != null) {
        transitions.add(new BlockTransferState(state.getCurrentTime(), state.getLocation(), prevTripId,
            sti.getTripId(), sti.getServiceDate()));
      }
    } else {
      StopTimeProxy prevStopTime = stopTimes.get(prevIndex);

      StopTimeInstanceProxy nextSti = new StopTimeInstanceProxy(prevStopTime, sti.getServiceDate());

      // We can either get on at the previous stop
      transitions.add(new VehicleDepartureState(nextSti));

      // Or we can continue from the previous stop
      transitions.add(new VehicleContinuationState(nextSti));
    }
  }

  public void getVehicleArrivalForwardTransitions(VehicleArrivalState state, Set<TripState> transitions) {

    StopTimeInstanceProxy sti = state.getStopTimeInstance();

    // We can wait here
    transitions.add(new WaitingAtStopState(state.getCurrentTime() + _constants.getMinTransferTime(), sti.getStop()));

    // Or we can walk to another stop
    transitions.add(new WalkFromStopState(state.getCurrentTime() + 1, sti.getStop()));
  }

  /*****************************************************************************
   * Block Transfer
   ****************************************************************************/

  public void getBlockTransferForwardTransitions(BlockTransferState state, Set<TripState> transitions) {

    String tripId = state.getNextTripId();
    TripEntry entry = _graph.getTripEntryByTripId(tripId);
    List<StopTimeProxy> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      String nextTripId = entry.getNextTripId();
      if (nextTripId != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(), state.getLocation(), tripId, nextTripId,
            state.getServiceDate()));
    } else {
      StopTimeProxy first = stopTimes.get(0);
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(first, state.getServiceDate());
      transitions.add(new VehicleContinuationState(sti));
      transitions.add(new VehicleArrivalState(sti));
    }
  }

  public void getBlockTransferReverseTransitions(BlockTransferState state, Set<TripState> transitions) {

    String tripId = state.getPrevTripId();
    TripEntry entry = _graph.getTripEntryByTripId(tripId);
    List<StopTimeProxy> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      String prevTripId = entry.getNextTripId();
      if (prevTripId != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(), state.getLocation(), prevTripId, tripId,
            state.getServiceDate()));
    } else {
      StopTimeProxy last = stopTimes.get(stopTimes.size() - 1);
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(last, state.getServiceDate());
      transitions.add(new VehicleContinuationState(sti));
      transitions.add(new VehicleDepartureState(sti));
    }
  }

  /*****************************************************************************
   * End
   ****************************************************************************/

  public void getEndReverseTransitions(EndState state, Set<TripState> transitions) {

    Point location = state.getLocation();
    Geometry boundary = location.buffer(5280 * 1 / 2).getBoundary();
    List<String> stopIds = _graph.getStopsByLocation(boundary);

    for (String stopId : stopIds) {

      try {
        StopEntry entry = _graph.getStopEntryByStopId(stopId);
        StopProxy stop = entry.getProxy();
        WalkPlan plan = _walkPlanner.getWalkPlan(location, stop.getStopLocation());
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
