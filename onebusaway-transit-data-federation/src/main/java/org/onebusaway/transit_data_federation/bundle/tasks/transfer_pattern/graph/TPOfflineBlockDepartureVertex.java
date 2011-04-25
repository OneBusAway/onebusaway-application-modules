package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineBlockDepartureVertex extends AbstractTPOfflineBlockVertex {

  public TPOfflineBlockDepartureVertex(GraphContext context,
      StopTimeInstance instance) {
    super(context, instance);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "block_departure: " + _instance.toString();
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getOutgoing() {
    StopTimeInstance next = _instance.getNextStopTimeInstance();
    if (next == null)
      return Collections.emptyList();
    return Arrays.asList((Edge) new TPOfflineBlockHopEdge(_context, _instance,
        next));
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "block_departure: " + _instance.toString();
  }
}
