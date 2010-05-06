package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.MinHeapMap;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.text.TimeLengthFormat;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.impl.state.BlockTransferStateHandler;
import org.onebusaway.tripplanner.impl.state.StartStateHandler;
import org.onebusaway.tripplanner.impl.state.VehicleArrivalStateHandler;
import org.onebusaway.tripplanner.impl.state.VehicleDepartureAndContinuationStateHandler;
import org.onebusaway.tripplanner.impl.state.WaitingAtStopStateHandler;
import org.onebusaway.tripplanner.impl.state.WalkToAnotherStopStateHandler;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.Trips;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.VehicleState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PointToPointStrategy2 {

  private static TimeLengthFormat _format = new TimeLengthFormat("mm:ss");

  private Map<Class<?>, Set<TripPlannerStateTransition>> _handlers = new FactoryMap<Class<?>, Set<TripPlannerStateTransition>>(
      new HashSet<TripPlannerStateTransition>());

  private Set<TripState> _closed = new HashSet<TripState>();

  private MinHeapMap<Double, TripState> _queue = new MinHeapMap<Double, TripState>();

  private long _startTime;

  private long _firstTripDuration = -1;

  private Point _endPoint;

  private Trips _trips = new Trips();
  
  private TripPlannerConstants _constants;

  private Map<TripState, Set<TripState>> _previousStates = new FactoryMap<TripState, Set<TripState>>(
      new HashSet<TripState>());

  private Map<Stop, Long> _firstVisitWaitingAtStop = new HashMap<Stop, Long>();

  private Map<Stop, Long> _firstVisitWalkToAnotherStop = new HashMap<Stop, Long>();

  private Set<TripState> _endStates = new HashSet<TripState>();

  private Map<TripState, Set<RouteKey>> _routeKeys = new HashMap<TripState, Set<RouteKey>>();

  private Set<Stop> _stops = new HashSet<Stop>();

  private Map<TripState, Integer> _transferCounts = new HashMap<TripState, Integer>();

  public PointToPointStrategy2(long startTime, Point startPoint,
      Point endPoint) {

    _startTime = startTime;
    _endPoint = endPoint;

    addStateHandler(StartState.class, new StartStateHandler());
    addStateHandler(WaitingAtStopState.class, new WaitingAtStopStateHandler());
    addStateHandler(VehicleDepartureState.class,
        new VehicleDepartureAndContinuationStateHandler());
    addStateHandler(VehicleContinuationState.class,
        new VehicleDepartureAndContinuationStateHandler());
    addStateHandler(BlockTransferState.class, new BlockTransferStateHandler());
    addStateHandler(VehicleArrivalState.class, new VehicleArrivalStateHandler());
    addStateHandler(WalkToAnotherStopState.class,
        new WalkToAnotherStopStateHandler());
    addStateHandler(VehicleArrivalState.class, new EndPointTransitionHandler());

    _queue.add(0.0, new StartState(startTime, startPoint));

    if (_queue.isEmpty())
      throw new IllegalStateException();
  }

  public <T> void addStateHandler(Class<? extends TripState> stateClass,
      TripPlannerStateTransition handler) {
    _handlers.get(stateClass).add(handler);
  }

  public Trips explore(TripContext context) {

    while (true) {

      if (_queue.isEmpty())
        break;

      Entry<Double, TripState> entry = _queue.removeMin();
      double time = entry.getKey();
      TripState state = entry.getValue();

      if (state instanceof WaitingAtStopState) {
        WaitingAtStopState was = (WaitingAtStopState) state;
        _stops.add(was.getStop());
      }

      //System.out.println("^==========================^");
      //System.out.println(state + " " + _format.format(time));
      //System.out.println("transfers=" + getMinTransferCount(state));
      if (state instanceof EndState) {
        if (_firstTripDuration <= 0) {
          //System.out.println("  XXX END XXX");
          _firstTripDuration = state.getCurrentTime() - _startTime;
        }
        _endStates.add(state);
      }

      if (_firstTripDuration > 0 && _firstTripDuration + 20 * 60 * 1000 <= time)
        break;

      Set<TripPlannerStateTransition> handlers = _handlers.get(state.getClass());
      Set<TripState> transitions = new HashSet<TripState>();

      for (TripPlannerStateTransition transitionHandler : handlers)
        transitionHandler.getTransitions(context, state, transitions);

      for (TripState next : transitions)
        handleTransition(context, state, next);

      //System.out.println("v==========================v");
    }

    // Ok now what?

    /*
     * for (TripState state : _endStates) { getRouteKeys for (TripState prev :
     * _previousStates.get(state)) {
     * 
     * }
     * 
     * }
     */

    // for (Stop stop : _stops) {
    // System.out.println(stop.getLat() + " " + stop.getLon());
    // }
    return _trips;
  }

  private int getMinTransferCount(TripState state) {
    Integer count = _transferCounts.get(state);
    if (count == null) {
      Set<TripState> prevs = _previousStates.get(state);
      if (prevs.isEmpty()) {
        count = 0;
      } else {
        Min<TripState> m = new Min<TripState>();
        for (TripState prev : prevs)
          m.add(getMinTransferCount(prev), prev);
        count = (int) m.getMinValue();
      }
      if (state instanceof VehicleDepartureState)
        count++;
      _transferCounts.put(state, count);
    }
    return count;
  }

  private void handleTransition(TripContext context, TripState previousState,
      TripState state) {

    if (state instanceof WaitingAtStopState
        && !(previousState instanceof WaitingAtStopState)) {

      WaitingAtStopState was = (WaitingAtStopState) state;
      Stop stop = was.getStop();
      Long time = _firstVisitWaitingAtStop.get(stop);
      if (time != null && time < was.getCurrentTime()) {
        // System.out.println("  already been=" + state);
        return;
      }
      if (time == null || time > was.getCurrentTime())
        _firstVisitWaitingAtStop.put(stop, was.getCurrentTime());
    }

    if (state instanceof WalkToAnotherStopState) {
      WalkToAnotherStopState was = (WalkToAnotherStopState) state;
      Stop stop = was.getStop();
      Long time = _firstVisitWalkToAnotherStop.get(stop);
      if (time != null && time < was.getCurrentTime()) {
        // System.out.println("  already been=" + state);
        return;
      }
      if (time == null || time > was.getCurrentTime())
        _firstVisitWalkToAnotherStop.put(stop, was.getCurrentTime());
    }

    _previousStates.get(state).add(previousState);
    _transferCounts.remove(state);

    if (_closed.contains(state))
      return;

    _closed.add(state);

    addNext(state);
  }

  private void addNext(TripState nextState) {

    double timePassed = nextState.getCurrentTime() - _startTime;
    Point location = nextState.getLocation();
    double estimatedDistanceRemaining = location.distance(_endPoint);
    double estimatedTimeRemaining = estimatedDistanceRemaining / _constants.getWalkingVelocity();

    double score = timePassed + estimatedTimeRemaining;
    _queue.add(score, nextState);
  }

  private Set<RouteKey> getRouteKeys(TripState state) {

    Set<RouteKey> keys = _routeKeys.get(state);

    if (keys == null) {

      keys = new HashSet<RouteKey>();
      Set<RouteKey> priorKeys = new HashSet<RouteKey>();

      Set<TripState> previousStates = _previousStates.get(state);

      if (previousStates.isEmpty()) {
        priorKeys.add(new RouteKey());
      } else {

        for (TripState prev : previousStates)
          priorKeys.addAll(getRouteKeys(prev));

      }

      if (state instanceof VehicleState) {

        VehicleState vs = (VehicleState) state;
        StopTimeInstance sti = vs.getStopTimeInstance();
        StopTime st = sti.getStopTime();
        Trip trip = st.getTrip();
        Route route = trip.getRoute();

        for (RouteKey key : keys)
          keys.add(key.extend(route.getId()));

      } else {
        keys.addAll(priorKeys);
      }

      _routeKeys.put(state, keys);
    }

    return keys;
  }

  private class EndPointTransitionHandler implements TripPlannerStateTransition {

    public void getTransitions(TripContext context, TripState state,
        Set<TripState> transitions) {

      Point location = state.getLocation();
      double estimatedDistanceRemaining = location.distance(_endPoint);

      if (estimatedDistanceRemaining < 5280 / 2) {
        TripPlannerConstants constants = context.getConstants();
        long walkTime = (long) (estimatedDistanceRemaining / constants.getWalkingVelocity());
        EndState es = new EndState(state.getCurrentTime() + walkTime, _endPoint);
        transitions.add(es);
      }
    }
  }
}
