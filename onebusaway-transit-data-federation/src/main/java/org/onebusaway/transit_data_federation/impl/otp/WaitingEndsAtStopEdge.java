package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class WaitingEndsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  public WaitingEndsAtStopEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vertex getToVertex() {
    return new WalkFromStopVertex(_context, _stop);

  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {
    return new TraverseResult(0, s0, this);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    TraverseResult result = new TraverseResult(0, s0, this);
    //result.setVertex(new WaitingAtStopVertex(_context, _stop, s0.getTime()));
    return result;
  }

  @Override
  public double getDistance() {
    return 0;
  }

  @Override
  public String toString() {
    return "WaitingEndsAtStopEdge(stop=" + _stop.getId() + ")";
  }
}
