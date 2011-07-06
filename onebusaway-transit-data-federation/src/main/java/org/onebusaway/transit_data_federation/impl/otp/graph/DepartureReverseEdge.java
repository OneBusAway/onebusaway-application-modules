package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.Vertex;

public class DepartureReverseEdge extends AbstractEdge {

  private final ArrivalAndDepartureInstance _instance;

  public DepartureReverseEdge(GraphContext context,
      ArrivalAndDepartureInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public State traverse(State s0) {
    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrative createNarrative(State s0) {
    Vertex fromVertex = new DepartureVertex(_context, _instance.getStop(),
        s0.getTime());
    Vertex toVertex = new BlockDepartureVertex(_context, _instance);
    return narrative(s0, fromVertex, toVertex);
  }

}
