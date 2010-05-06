package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.MinHeapMap;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PointToPointStrategy {

  private Set<TripState> _closed = new HashSet<TripState>();

  private MinHeapMap<Double, TripState> _queue = new MinHeapMap<Double, TripState>();

  private long _startTime;

  private long _firstTripEndTime = -1;

  private Point _endPoint;

  private double _maxVelocity;

  private Trips _trips = new Trips();

  private Map<State, Long> _tripStartTimes = new HashMap<State, Long>();

  private Map<State, Long> _tripWalkingTimes = new HashMap<State, Long>();

  private Map<State, State> _previousStates = new HashMap<State, State>();

  private Map<TripState, Set<RouteKey>> _routeKeys = new HashMap<TripState, Set<RouteKey>>();

  private Map<Class<?>, Set<TripPlannerStateTransition>> _handlers = new FactoryMap<Class<?>, Set<TripPlannerStateTransition>>(
      new HashSet<TripPlannerStateTransition>());

  private Set<TripState> _endStates = new HashSet<TripState>();

  private Map<Stop, Long> _firstVisitWaitingAtStop = new HashMap<Stop, Long>();

  private Map<Stop, Long> _firstVisitWalkToAnotherStop = new HashMap<Stop, Long>();

  public PointToPointStrategy(long startTime, Point startPoint, Point endPoint,
      double maxVelocity) {

    _startTime = startTime;
    _endPoint = endPoint;
    _maxVelocity = maxVelocity;

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
        Stop stop = was.getStop();
        Long time = _firstVisitWaitingAtStop.get(stop);
        if (time != null && time < was.getCurrentTime()) {
          System.out.println("  already been=" + state);
          return;
        }
        if (time == null || time > was.getCurrentTime())
          _firstVisitWaitingAtStop.put(stop, was.getCurrentTime());
      }

      if (state instanceof EndState) {
        if (_firstTripEndTime <= 0) {
          System.out.println("  XXX END XXX");
          _firstTripEndTime = state.getCurrentTime();
        }
        _endStates.add(state);
      }

      if (_firstTripEndTime > 0 && _firstTripEndTime + 20 * 60 * 1000 <= time)
        break;

      System.out.println("^==========================^");
      System.out.println(state);

      Set<TripPlannerStateTransition> handlers = _handlers.get(state.getClass());
      Set<TripState> transitions = new HashSet<TripState>();

      for (TripPlannerStateTransition transitionHandler : handlers)
        transitionHandler.getTransitions(context, state, transitions);

      for (TripState next : transitions)
        handleTransition(context, state, next);
      System.out.println("v==========================v");
    }

    // Ok now what

    for (TripState state : _endStates) {
      Set<RouteKey> routeKeys = getRouteKeysForState(state);
      for (RouteKey key : routeKeys) {
        State s = new State(key, state);
        List<TripState> trip = new ArrayList<TripState>();
        getTrip(s, trip);
        _trips.addTrip(key, trip);
      }
    }

    return _trips;
  }

  private void handleTransition(TripContext context, TripState previousState,
      TripState state) {

    if (state instanceof WaitingAtStopState
        && !(previousState instanceof WaitingAtStopState)) {

      WaitingAtStopState was = (WaitingAtStopState) state;
      Stop stop = was.getStop();
      Long time = _firstVisitWaitingAtStop.get(stop);
      if (time != null && time < was.getCurrentTime()) {
        System.out.println("  already been=" + state);
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
        System.out.println("  already been=" + state);
        return;
      }
      if (time == null || time > was.getCurrentTime())
        _firstVisitWalkToAnotherStop.put(stop, was.getCurrentTime());
    }

    // Have we been here before?
    if (_closed.contains(state)) {

      System.out.println("  === REVISIT: IN ===");

      Set<RouteKey> currentKeys = getRouteKeysForState(state);
      Set<RouteKey> previousKeys = getRouteKeysForState(previousState);
      Map<RouteKey, RouteKey> newKeys = extendRouteKeys(previousKeys, state);

      for (Map.Entry<RouteKey, RouteKey> entry : newKeys.entrySet()) {
        RouteKey previousKey = entry.getKey();
        RouteKey nextKey = entry.getValue();
        State prev = new State(previousKey, previousState);
        State next = new State(nextKey, state);
        if (currentKeys.contains(nextKey)) {
          State currentPreviousState = getPreviousState(next);
          if (compareTo(context, prev, currentPreviousState, next) < 0)
            storeTransition(prev, next);
        } else {
          storeTransition(prev, next);
        }
      }

      System.out.println("  === REVISIT: OUT ===");

    } else {

      System.out.println("  === NEW: IN ===");

      _closed.add(state);

      Map<RouteKey, RouteKey> routeTransitions = extendRouteKeys(
          getRouteKeysForState(previousState), state);

      for (Map.Entry<RouteKey, RouteKey> entry : routeTransitions.entrySet()) {
        RouteKey previousKey = entry.getKey();
        RouteKey nextKey = entry.getValue();
        State prev = new State(previousKey, previousState);
        State next = new State(nextKey, state);
        storeTransition(prev, next);
      }

      addNext(state);

      System.out.println("  === NEW: OUT ===");
    }
  }

  private void storeTransition(State previous, State next) {

    TripState previousState = previous.getTripState();
    if (previousState instanceof VehicleContinuationState) {
      State p = getPreviousState(previous);
      if (p == null)
        throw new IllegalStateException("previous state should not be null");
      storeTransition(p, next);
      return;
    } else if (previousState instanceof WaitingAtStopState) {
      State p = getPreviousState(previous);
      if (p == null)
        throw new IllegalStateException("previous state should not be null");
      if (p.getTripState() instanceof WaitingAtStopState) {
        storeTransition(p, next);
        return;
      }
    }

    System.out.println("  " + previous + " => " + next);

    setPreviousState(previous, next);
  }

  private void addNext(TripState nextState) {

    double timePassed = nextState.getCurrentTime() - _startTime;
    Point location = nextState.getLocation();
    double estimatedDistanceRemaining = location.distance(_endPoint);
    double estimatedTimeRemaining = estimatedDistanceRemaining / _maxVelocity;

    double score = timePassed + estimatedTimeRemaining;
    _queue.add(score, nextState);
  }

  private int compareTo(TripContext context, State previousStateA,
      State previousStateB, State nextState) {

    long startTimeA = getTripStartTime(context, previousStateA);
    long startTimeB = getTripStartTime(context, previousStateB);

    if (startTimeA != startTimeB)
      return startTimeA > startTimeB ? -1 : 1;

    long walkingTimeA = getTripWalkingTime(previousStateA);
    long walkingTimeB = getTripWalkingTime(previousStateB);

    return walkingTimeA == walkingTimeB ? 0 : (walkingTimeA < walkingTimeB ? -1
        : 1);
  }

  private Map<RouteKey, RouteKey> extendRouteKeys(Set<RouteKey> keys,
      TripState state) {

    Map<RouteKey, RouteKey> extended = new HashMap<RouteKey, RouteKey>();

    if (!(state instanceof VehicleState)) {
      for (RouteKey key : keys)
        extended.put(key, key);
      return extended;
    }

    VehicleState vs = (VehicleState) state;
    StopTimeInstance sti = vs.getStopTimeInstance();
    StopTime st = sti.getStopTime();
    Trip trip = st.getTrip();
    Route route = trip.getRoute();

    for (RouteKey key : keys)
      extended.put(key, key.extend(route.getId()));

    return extended;
  }

  private long getTripStartTime(TripContext context, State state) {

    Long startTime = _tripStartTimes.get(state);

    if (startTime == null) {

      State prev = getPreviousState(state);
      if (prev != null) {
        State prev2 = getPreviousState(prev);
        if (prev2 != null) {
          TripState s = state.getTripState();
          TripState p = prev.getTripState();
          TripState p2 = prev2.getTripState();
          if (s instanceof VehicleDepartureState
              && p instanceof WaitingAtStopState && p2 instanceof StartState) {
            long waitTime = s.getCurrentTime() - p.getCurrentTime();
            startTime = p2.getCurrentTime() + waitTime;
            _tripStartTimes.put(state, startTime);
            return startTime;
          }
        }
      }

      if (prev == null)
        startTime = state.getTripState().getCurrentTime();
      else
        startTime = getTripStartTime(context, prev);

      _tripStartTimes.put(state, startTime);
    }

    return startTime;
  }

  private long getTripWalkingTime(State state) {

    Long walkingTime = _tripWalkingTimes.get(state);

    if (walkingTime == null) {
      State prev = getPreviousState(state);
      if (prev == null) {
        walkingTime = 0L;
      } else {
        long time = getTripWalkingTime(prev);
        TripState s = state.getTripState();
        TripState p = prev.getTripState();
        if (p instanceof StartState || p instanceof WalkToAnotherStopState)
          time += (s.getCurrentTime()) + p.getCurrentTime();
        walkingTime = time;
      }

      _tripWalkingTimes.put(state, walkingTime);
    }

    return walkingTime;
  }

  private void getTrip(State state, List<TripState> trip) {
    State prev = getPreviousState(state);
    if (prev != null)
      getTrip(prev, trip);
    trip.add(state.getTripState());
  }

  private State getPreviousState(State state) {
    return _previousStates.get(state);
  }

  private void setPreviousState(State previous, State next) {
    _previousStates.put(next, previous);
    Set<RouteKey> keys = _routeKeys.get(next.getTripState());
    if (keys == null) {
      keys = new HashSet<RouteKey>();
      _routeKeys.put(next.getTripState(), keys);
    }
    keys.add(next.getRouteKey());
  }

  private Set<RouteKey> getRouteKeysForState(TripState state) {
    Set<RouteKey> keys = _routeKeys.get(state);
    if (keys == null) {
      keys = new HashSet<RouteKey>();
      keys.add(new RouteKey());
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

  private static class State {

    private RouteKey _routeKey;
    private TripState _state;

    public State(RouteKey routeKey, TripState tripState) {
      _routeKey = routeKey;
      _state = tripState;
    }

    public RouteKey getRouteKey() {
      return _routeKey;
    }

    public TripState getTripState() {
      return _state;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof State))
        return false;
      State s = (State) obj;
      return _routeKey.equals(s._routeKey) && _state.equals(s._state);
    }

    @Override
    public int hashCode() {
      return _routeKey.hashCode() + _state.hashCode();
    }

    @Override
    public String toString() {
      return _routeKey + " " + _state;
    }
  }
}
