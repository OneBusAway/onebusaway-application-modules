package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class WaitingEndsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  private final boolean _isReverseEdge;

  public WaitingEndsAtStopEdge(GraphContext context, StopEntry stop,
      boolean isReverseEdge) {
    super(context);
    _stop = stop;
    _isReverseEdge = isReverseEdge;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());

    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Only allow transition to a transit stop if transit is enabled
     */
    if (!SupportLibrary.isTransitEnabled(options))
      return null;

    /**
     * If we've already boarded a transit vehicle, we only allow additional
     * boardings from a direct transfer. Note that we only apply this rule when
     * doing reverse traversal of the graph. In a forward traversal, this edge
     * traversal will be called in the optimization step where the number of
     * boardings is greater than zero. However, we still want the traversal to
     * proceed.
     */
    StateData data = s0.getData();
    if (_isReverseEdge && data.getNumBoardings() > 0)
      return null;

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());

    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public String toString() {
    return "WaitingEndsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  private EdgeNarrativeImpl createNarrative(long time) {

    ArrivalVertex fromVertex = new ArrivalVertex(_context, _stop, time);
    WalkFromStopVertex toVertex = new WalkFromStopVertex(_context, _stop);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
