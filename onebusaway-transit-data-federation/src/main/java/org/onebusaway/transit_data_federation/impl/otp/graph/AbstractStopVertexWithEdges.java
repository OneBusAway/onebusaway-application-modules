package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.Collection;
import java.util.Collections;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public abstract class AbstractStopVertexWithEdges extends AbstractStopVertex
    implements HasEdges {

  public AbstractStopVertexWithEdges(GraphContext context, StopEntry stop) {
    super(context, stop);
  }

  @Override
  public int getDegreeIn() {
    return getIncoming().size();
  }

  @Override
  public int getDegreeOut() {
    return getOutgoing().size();
  }

  @Override
  public Collection<Edge> getIncoming() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Collections.emptyList();
  }
}
