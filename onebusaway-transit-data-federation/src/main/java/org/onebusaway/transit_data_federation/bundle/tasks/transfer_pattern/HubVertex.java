package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class HubVertex extends AbstractStopVertex implements HasEdges {

  public HubVertex(GraphContext context, StopEntry stop) {
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
    return Arrays.asList((Edge) new HubEdge(_context, _stop));
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new HubEdge(_context, _stop));
  }

  @Override
  public String toString() {
    return _stop.getId().toString();
  }
}
