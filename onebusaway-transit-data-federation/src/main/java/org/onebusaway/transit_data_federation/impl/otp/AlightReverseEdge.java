package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class AlightReverseEdge extends AbstractEdge {

  private final StopEntry _stop;

  public AlightReverseEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * We alight from our current vehicle to the stop. However, we don't
     * actually know which vehicle. Hopefully this method will only ever be
     * called in the GraphPath.optimize(), where the traverseBack() method has
     * previously been called.
     */
    Vertex fromVertex = null;
    Vertex toVertex = new AlightVertex(_context, _stop, s0.getTime());
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    StopTimeService stopTimeService = _context.getStopTimeService();
    long time = s0.getTime();

    /**
     * Look for arrivals in the previous X minutes
     */
    Date timeFrom = new Date(SupportLibrary.getPreviousTimeWindow(_context,
        time));
    Date timeTo = new Date(time);

    List<StopTimeInstance> instances = stopTimeService.getStopTimeInstancesInTimeRange(
        _stop, timeFrom, timeTo);

    for (StopTimeInstance instance : instances) {

      long arrivalTime = instance.getArrivalTime();

      // Prune anything that doesn't have an arrival time in the proper range,
      // since the stopTimeService method will also return instances that depart
      // in the target interval as well
      if (arrivalTime < timeFrom.getTime() || timeTo.getTime() <= arrivalTime)
        continue;

      int dwellTime = (int) ((time - arrivalTime) / 1000);
      State s1 = s0.clone();
      s1.time = arrivalTime;
      s1.numBoardings++;
      s1.everBoarded = true;
      

      Vertex fromVertex = new BlockArrivalVertex(_context, instance);
      Vertex toVertex = new AlightVertex(_context, _stop, s0.getTime());
      EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

      TraverseResult r = new TraverseResult(-dwellTime, s1, narrative);
      result = r.addToExistingResultChain(result);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    int dwellTime = (int) ((timeTo.getTime() - timeFrom.getTime()) / 1000);
    State s1 = new State(timeFrom.getTime());

    Vertex fromVertex = new BoardVertex(_context, _stop, timeFrom.getTime());
    Vertex toVertex = new AlightVertex(_context, _stop, s0.getTime());
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex,toVertex);

    TraverseResult r = new TraverseResult(dwellTime, s1, narrative);
    result = r.addToExistingResultChain(result);

    return result;
  }
}
