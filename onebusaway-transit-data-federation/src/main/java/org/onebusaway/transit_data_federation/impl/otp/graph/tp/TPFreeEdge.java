package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
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
  public State traverse(State s0) {
    return s0.edit(this, createNarrative(s0)).makeState();
  }

  private EdgeNarrative createNarrative(State s0) {
    return narrative(s0, _from, _to);
  }
}
