package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class WaitingBeginsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  private final boolean _isReverseEdge;

  public WaitingBeginsAtStopEdge(GraphContext context, StopEntry stop,
      boolean isReverseEdge) {
    super(context);
    _stop = stop;
    _isReverseEdge = isReverseEdge;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Only allow transition to a transit stop if transit is enabled
     */
    if (!SupportLibrary.isTransitEnabled(options))
      return null;

    /**
     * If we've already boarded a transit vehicle, we only allow additional
     * boardings from a direct transfer. Note that we only apply this rule when
     * doing forward traversal of the graph. In a backwards traversal, this edge
     * traversal will be called in the optimization step where the number of
     * boardings is greater than zero. However, we still want the traversal to
     * proceed.
     * 
     */
    if (!_isReverseEdge && s0.numBoardings > 0)
      return null;

    State s1 = s0.clone();

    EdgeNarrativeImpl narrative = createNarrative(s1.getTime());

    return new TraverseResult(0, s1, narrative);
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
    DepartureVertex toVertex = new DepartureVertex(_context, _stop, time);

    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
