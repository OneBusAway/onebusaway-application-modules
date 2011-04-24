package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class HubVertex extends AbstractStopVertex implements HasEdges {

  private final boolean _isTransfer;

  public HubVertex(GraphContext context, StopEntry stop, boolean isTransfer) {
    super(context, stop);
    _isTransfer = isTransfer;
  }

  public boolean isTransfer() {
    return _isTransfer;
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new HubEdge(_context, _stop, _isTransfer));
  }

  @Override
  public String toString() {
    return _stop.getId().toString();
  }
}
