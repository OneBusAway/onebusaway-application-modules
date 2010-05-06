package org.onebusaway.tripplanner.impl.state;

import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkToAnotherStopState;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.Set;

public class VehicleArrivalStateHandler implements TripPlannerStateTransition {

  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions) {

    VehicleArrivalState as = (VehicleArrivalState) state;
    StopTimeInstance sti = as.getStopTimeInstance();
    StopTime st = sti.getStopTime();

    // We can wait here
    TripPlannerConstants constants = context.getConstants();
    transitions.add(new WaitingAtStopState(as.getCurrentTime() + 1
        + constants.getMinTransferTime(), st.getStop()));

    // Or we can walk to another stop
    transitions.add(new WalkToAnotherStopState(as.getCurrentTime() + 1,
        st.getStop()));
  }
}
