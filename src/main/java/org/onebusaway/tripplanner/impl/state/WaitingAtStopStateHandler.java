package org.onebusaway.tripplanner.impl.state;

import edu.washington.cs.rse.collections.stats.Min;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.tripplanner.impl.TripPlannerStateTransition;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.where.model.ServiceDate;
import org.onebusaway.where.model.StopTimeInstance;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WaitingAtStopStateHandler implements TripPlannerStateTransition {

  private StopTimeBinarySearch _search = new StopTimeBinarySearch();

  public void getTransitions(TripContext context, TripState state, Set<TripState> transitions) {

    WaitingAtStopState atStop = (WaitingAtStopState) state;
    Stop stop = atStop.getStop();

    TripPlannerGraph graph = context.getGraph();
    Set<ServiceDate> serviceDates = context.getServiceDates(state);

    StopEntry stopEntry = graph.getStopEntryByStopId(stop.getId());
    Map<String, List<StopTime>> stopTimesByServiceId = stopEntry.getStopTimes();

    Min<StopTimeInstance> nextDepartures = new Min<StopTimeInstance>();

    for (ServiceDate serviceDate : serviceDates) {
      String serviceId = serviceDate.getServiceId();
      Date date = serviceDate.getServiceDate();
      List<StopTime> stopTimes = stopTimesByServiceId.get(serviceId);

      if (stopTimes == null)
        continue;

      List<StopTimeInstance> next = _search.getNextStopTime(date.getTime(),
          stopTimes, state.getCurrentTime());

      for (StopTimeInstance sti : next)
        nextDepartures.add(sti.getDepartureTime().getTime(), sti);
    }

    for (StopTimeInstance sti : nextDepartures.getMinElements())
      transitions.add(new VehicleDepartureState(sti));

    long nextTime = (long) (nextDepartures.getMinValue() + 1000);
    transitions.add(new WaitingAtStopState(nextTime, stop));
  }
}
