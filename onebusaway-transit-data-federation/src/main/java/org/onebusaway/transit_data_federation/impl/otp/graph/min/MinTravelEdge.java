package org.onebusaway.transit_data_federation.impl.otp.graph.min;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class MinTravelEdge extends AbstractEdge {

  private final Vertex _fromVertex;
  private final Vertex _toVertex;
  private final int _travelTime;

  public MinTravelEdge(GraphContext context, Vertex fromVertex,
      Vertex toVertex, int travelTime) {
    super(context);

    _fromVertex = fromVertex;
    _toVertex = toVertex;
    _travelTime = travelTime;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    State s1 = s0.incrementTimeInSeconds(_travelTime);
    EdgeNarrative narrative = getEdgeNarrative();
    return new TraverseResult(_travelTime, s1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    State s1 = s0.incrementTimeInSeconds(-_travelTime);
    EdgeNarrative narrative = getEdgeNarrative();
    return new TraverseResult(_travelTime, s1, narrative);
  }

  private EdgeNarrative getEdgeNarrative() {
    return new EdgeNarrativeImpl(_fromVertex, _toVertex);
  }
}
