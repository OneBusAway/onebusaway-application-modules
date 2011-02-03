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

public class BoardEdge extends AbstractEdge {

  private final StopEntry _stop;

  public BoardEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    StopTimeService stopTimeService = _context.getStopTimeService();
    long time = s0.getTime();

    /**
     * Look for departures in the next X minutes
     */
    Date from = new Date(time);
    Date to = new Date(SupportLibrary.getNextTimeWindow(_context, time));

    List<StopTimeInstance> instances = stopTimeService.getStopTimeInstancesInTimeRange(
        _stop, from, to);

    for (StopTimeInstance instance : instances) {

      long departureTime = instance.getDepartureTime();

      // Prune anything that doesn't have a departure in the proper range, since
      // the stopTimeService method will also return instances that arrive in
      // the target interval as well
      if (departureTime < from.getTime() || to.getTime() <= departureTime)
        continue;

      // If this is the last stop time in the block, don't continue
      if (!SupportLibrary.hasNextStopTime(instance))
        continue;

      BoardVertex fromVertex = new BoardVertex(_context, _stop, time);
      BlockDepartureVertex toVertex = new BlockDepartureVertex(_context,
          instance);
      EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

      State s1 = s0.clone();
      s1.time = departureTime;
      s1.numBoardings++;
      s1.everBoarded = true;

      int dwellTime = (int) ((departureTime - time) / 1000);
      double w = computeWeightForWait(options, dwellTime, s0);

      TraverseResult r = new TraverseResult(w, s1, narrative);
      result = r.addToExistingResultChain(result);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    
    State s1 = new State(to.getTime());

    BoardVertex fromVertex = new BoardVertex(_context, _stop, s0.getTime());
    BoardVertex toVertex = new BoardVertex(_context, _stop, to.getTime());
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);
    
    int dwellTime = (int) ((to.getTime() - from.getTime()) / 1000);
    double w = computeWeightForWait(options, dwellTime, s0);

    TraverseResult r = new TraverseResult(w, s1, narrative);
    result = r.addToExistingResultChain(result);

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    BoardVertex fromVertex = new BoardVertex(_context, _stop, s0.getTime());
    Vertex toVertex = null;
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(0, s0, narrative);
  }

  /****
   * Private Methods
   ****/

  private double computeWeightForWait(TraverseOptions options, int dwellTime,
      State s0) {

    double w = dwellTime * options.waitReluctance;

    /**
     * If this is the initial boarding, we penalize the wait time differently
     */
    if (s0.numBoardings == 0)
      w *= options.waitAtBeginningFactor;

    return w;
  }
}
