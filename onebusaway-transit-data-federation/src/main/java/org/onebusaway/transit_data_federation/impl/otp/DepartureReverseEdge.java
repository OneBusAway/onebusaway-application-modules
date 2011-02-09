package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class DepartureReverseEdge extends AbstractEdge {

  private final ArrivalAndDepartureInstance _instance;

  public DepartureReverseEdge(GraphContext context,
      ArrivalAndDepartureInstance instance) {
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
    Vertex fromVertex = new DepartureVertex(_context, _instance.getStop(), time);
    Vertex toVertex = new BlockDepartureVertex(_context, _instance);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }

}
