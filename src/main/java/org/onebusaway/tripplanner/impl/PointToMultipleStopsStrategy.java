package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.MinHeapMap;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PointToMultipleStopsStrategy {

  private static TransitionHandlers _handlers = new TransitionHandlers();

  private Set<TripState> _closed = new HashSet<TripState>();

  private MinHeapMap<Double, TripState> _queue = new MinHeapMap<Double, TripState>();

  private Map<TripState, Set<TripState>> _previousStates = new FactoryMap<TripState, Set<TripState>>(
      new HashSet<TripState>());

  private Map<Stop, Long> _firstVisitWaitingAtStop = new HashMap<Stop, Long>();

  private Map<Stop, Long> _firstVisitWalkToAnotherStop = new HashMap<Stop, Long>();

  private Map<Stop, Long> _arrivalTimes = new HashMap<Stop, Long>();

  private long _startTime;

  private long _endTime;

  public PointToMultipleStopsStrategy(long startTime, long endTime,
      Point startPoint) {

    _startTime = startTime;
    _endTime = endTime;

    _queue.add(0.0, new StartState(startTime, startPoint));

    if (_queue.isEmpty())
      throw new IllegalStateException();
  }

  public Map<Stop, Long> explore(TripContext context) {

    while (true) {

      if (_queue.isEmpty())
        break;

      Entry<Double, TripState> entry = _queue.removeMin();

      TripState state = entry.getValue();

      if (state.getCurrentTime() > _endTime)
        break;

      if (state instanceof VehicleArrivalState) {
        VehicleArrivalState vas = (VehicleArrivalState) state;
        StopTimeInstance sti = vas.getStopTimeInstance();
        StopTime st = sti.getStopTime();
        Stop stop = st.getStop();
        Long t = _arrivalTimes.get(stop);
        if (t == null) {
          t = state.getCurrentTime();
          _arrivalTimes.put(stop, t);
        }
      }

      Set<TripState> transitions = _handlers.getTransitions(context, state);

      for (TripState next : transitions)
        handleTransition(context, state, next);
    }

    return _arrivalTimes;
  }

  private void handleTransition(TripContext context, TripState previousState,
      TripState state) {

    if (state instanceof WaitingAtStopState
        && !(previousState instanceof WaitingAtStopState)) {

      WaitingAtStopState was = (WaitingAtStopState) state;
      Stop stop = was.getStop();
      Long time = _firstVisitWaitingAtStop.get(stop);
      if (time != null && time < was.getCurrentTime())
        return;
      if (time == null || time > was.getCurrentTime())
        _firstVisitWaitingAtStop.put(stop, was.getCurrentTime());
    }

    if (state instanceof WalkToAnotherStopState) {
      WalkToAnotherStopState was = (WalkToAnotherStopState) state;
      Stop stop = was.getStop();
      Long time = _firstVisitWalkToAnotherStop.get(stop);
      if (time != null && time < was.getCurrentTime())
        return;
      if (time == null || time > was.getCurrentTime())
        _firstVisitWalkToAnotherStop.put(stop, was.getCurrentTime());
    }

    _previousStates.get(state).add(previousState);

    if (_closed.contains(state))
      return;

    _closed.add(state);

    addNext(state);
  }

  private void addNext(TripState nextState) {
    double timePassed = nextState.getCurrentTime() - _startTime;
    _queue.add(timePassed, nextState);
  }
}