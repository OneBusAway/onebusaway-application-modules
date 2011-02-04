package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class WaitingBeginsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  public WaitingBeginsAtStopEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Only allow transition to a transit stop if transit is enabled
     */
    if (!SupportLibrary.isTransitEnabled(options))
      return null;

    State s1 = s0.clone();

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());

    TraverseResult result = new TraverseResult(0, s1, narrative);

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    State s1 = s0.clone();

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());
    return new TraverseResult(0, s1, narrative);
  }

  @Override
  public String toString() {
    return "WaitingBeginsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {

    WalkToStopVertex fromVertex = new WalkToStopVertex(_context, _stop);
    BoardVertex toVertex = new BoardVertex(_context, _stop, time);

    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
