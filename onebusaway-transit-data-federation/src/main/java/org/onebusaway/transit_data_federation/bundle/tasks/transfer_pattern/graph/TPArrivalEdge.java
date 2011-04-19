package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TPArrivalEdge extends AbstractEdge {

  private final StopTimeInstance _instance;

  public TPArrivalEdge(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
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

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());
    return new TraverseResult(0, s0, narrative);
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {
    Vertex fromVertex = new TPBlockArrivalVertex(_context, _instance);
    Vertex toVertex = new TPArrivalVertex(_context, _instance.getStop());
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }

}
