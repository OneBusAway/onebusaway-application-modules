package org.onebusaway.tripplanner.impl.state;

import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripEntry;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleContinuationState;
import org.onebusaway.tripplanner.model.VehicleState;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.List;
import java.util.Set;

public class VehicleDepartureAndContinuationStateHandler implements
    TripPlannerStateTransition {

  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions) {

    VehicleState vs = (VehicleState) state;
    StopTimeInstance sti = vs.getStopTimeInstance();
    StopTime st = sti.getStopTime();
    Trip trip = st.getTrip();

    TripPlannerGraph graph = context.getGraph();

    TripEntry entry = graph.getTripEntryByTripId(trip.getId());
    List<StopTime> stopTimes = entry.getStopTimes();
    int nextIndex = st.getStopSequence() + 1;

    if (nextIndex > stopTimes.size())
      throw new IllegalStateException("not good");

    if (nextIndex == stopTimes.size()) {

      Trip nextTrip = entry.getNextTrip();
      if (nextTrip != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            nextTrip, vs.getLocation()));

    } else {
      StopTime nextStopTime = stopTimes.get(nextIndex);
      StopTimeInstance nextSti = new StopTimeInstance(nextStopTime,
          sti.getDate());

      // We can either get off at the next stop
      transitions.add(new VehicleArrivalState(nextSti));

      // Or we can continue on
      transitions.add(new VehicleContinuationState(nextSti));
    }
  }

}
