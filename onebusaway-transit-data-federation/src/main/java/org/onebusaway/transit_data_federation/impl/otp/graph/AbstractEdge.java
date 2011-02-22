package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Vertex;

public abstract class AbstractEdge implements Edge {

  protected final GraphContext _context;

  public AbstractEdge(GraphContext context) {
    _context = context;
  }

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }
}
