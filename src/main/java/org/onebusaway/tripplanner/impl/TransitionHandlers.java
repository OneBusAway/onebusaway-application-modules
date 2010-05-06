package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.tripplanner.impl.state.BlockTransferStateHandler;
import org.onebusaway.tripplanner.impl.state.StartStateHandler;
import org.onebusaway.tripplanner.impl.state.VehicleArrivalStateHandler;
import org.onebusaway.tripplanner.impl.state.VehicleDepartureAndContinuationStateHandler;
import org.onebusaway.tripplanner.impl.state.WaitingAtStopStateHandler;
import org.onebusaway.tripplanner.impl.state.WalkToAnotherStopStateHandler;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransitionHandlers {

  private Map<Class<?>, Set<TripPlannerStateTransition>> _handlers = new FactoryMap<Class<?>, Set<TripPlannerStateTransition>>(
      new HashSet<TripPlannerStateTransition>());

  public TransitionHandlers() {
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
  }

  public <T> void addStateHandler(Class<? extends TripState> stateClass,
      TripPlannerStateTransition handler) {
    _handlers.get(stateClass).add(handler);
  }

  public Set<TripState> getTransitions(TripContext context, TripState state) {
    Set<TripPlannerStateTransition> handlers = _handlers.get(state.getClass());
    Set<TripState> transitions = new HashSet<TripState>();

    for (TripPlannerStateTransition transitionHandler : handlers)
      transitionHandler.getTransitions(context, state, transitions);
    return transitions;
  }
}
