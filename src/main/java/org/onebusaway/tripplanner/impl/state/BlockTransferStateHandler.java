package org.onebusaway.tripplanner.impl.state;

import edu.washington.cs.rse.collections.stats.Min;

import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripEntry;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.where.model.ServiceDate;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.List;
import java.util.Set;

public class BlockTransferStateHandler implements TripPlannerStateTransition {

  public void getTransitions(TripContext context, TripState state,
      Set<TripState> transitions) {

    BlockTransferState bt = (BlockTransferState) state;

    TripPlannerGraph graph = context.getGraph();
    Trip trip = bt.getNextTrip();

    TripEntry entry = graph.getTripEntryByTripId(trip.getId());
    List<StopTime> stopTimes = entry.getStopTimes();

    if (stopTimes.isEmpty()) {
      Trip nextTrip = entry.getNextTrip();
      if (nextTrip != null)
        transitions.add(new BlockTransferState(state.getCurrentTime(),
            nextTrip, bt.getLocation()));
    } else {

      StopTime first = stopTimes.get(0);

      String serviceId = trip.getServiceId();
      Set<ServiceDate> serviceDates = context.getServiceDates(state, serviceId);

      Min<StopTimeInstance> m = new Min<StopTimeInstance>();

      for (ServiceDate sd : serviceDates) {
        StopTimeInstance sti = new StopTimeInstance(first, sd.getServiceDate());
        long t = sti.getArrivalTime().getTime() - state.getCurrentTime();
        if (t >= 0)
          m.add(t, sti);
      }

      StopTimeInstance sti = m.getMinElement();
      transitions.add(new VehicleArrivalState(sti));
    }
  }
}
