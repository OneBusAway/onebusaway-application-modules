package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.edgetype.FreeEdge;

public class TPTransferVertex extends AbstractTPBlockVertex implements HasEdges {

  public TPTransferVertex(GraphContext context, StopTimeInstance instance) {
    super(context, instance);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public int getDegreeIn() {
    return getIncoming().size();
  }

  @Override
  public Collection<Edge> getIncoming() {
    return Collections.emptyList();
  }

  @Override
  public int getDegreeOut() {
    return getOutgoing().size();
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new FreeEdge(this, new TPBlockDepartureVertex(
        _context, _instance)));
  }
}
