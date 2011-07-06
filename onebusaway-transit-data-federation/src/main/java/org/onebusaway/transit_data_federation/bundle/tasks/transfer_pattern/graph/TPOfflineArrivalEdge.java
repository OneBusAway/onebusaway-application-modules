package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineArrivalEdge extends AbstractEdge {

  private final StopTimeInstance _instance;

  public TPOfflineArrivalEdge(GraphContext context, StopTimeInstance instance) {
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
    Vertex fromVertex = new TPOfflineBlockArrivalVertex(_context, _instance);
    Vertex toVertex = new TPOfflineArrivalVertex(_context, _instance.getStop());
    return narrative(s0, fromVertex, toVertex);
  }

}
