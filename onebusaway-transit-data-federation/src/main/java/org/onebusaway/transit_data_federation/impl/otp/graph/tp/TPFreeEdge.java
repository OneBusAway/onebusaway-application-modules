package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TPFreeEdge extends AbstractEdge {

  private final Vertex _from;
  private final Vertex _to;

  public TPFreeEdge(GraphContext context, Vertex from, Vertex to) {
    super(context);
    _from = from;
    _to = to;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {
    return new TraverseResult(0, s0, createNarrative());
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    return new TraverseResult(0, s0, createNarrative());
  }

  private EdgeNarrative createNarrative() {
    return new EdgeNarrativeImpl(_from, _to);
  }
}
