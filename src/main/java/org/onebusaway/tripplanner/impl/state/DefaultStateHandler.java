package org.onebusaway.tripplanner.impl.state;

import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultStateHandler implements TripPlannerStateTransition {

  private Map<Class<?>, TripPlannerStateTransition> _handlers = new HashMap<Class<?>, TripPlannerStateTransition>();

  public DefaultStateHandler() {

    _handlers.put(VehicleArrivalState.class, new VehicleArrivalStateHandler());
    _handlers.put(VehicleContinuationState.class,
        new VehicleDepartureAndContinuationStateHandler());
    _handlers.put(VehicleDepartureState.class,
        new VehicleDepartureAndContinuationStateHandler());
    _handlers.put(StartState.class, new StartStateHandler());
    _handlers.put(WaitingAtStopState.class, new WaitingAtStopStateHandler());
    _handlers.put(WalkToAnotherStopState.class,
        new WalkToAnotherStopStateHandler());
  }

  public void getTransitions(TripContext context, TripState state, Set<TripState> transitions) {
    Class<? extends TripState> c = state.getClass();
    TripPlannerStateTransition handler = _handlers.get(c);
    if (handler == null)
      throw new IllegalStateException("no state handler for type " + c);
    handler.getTransitions(context, state, transitions);
  }
}
