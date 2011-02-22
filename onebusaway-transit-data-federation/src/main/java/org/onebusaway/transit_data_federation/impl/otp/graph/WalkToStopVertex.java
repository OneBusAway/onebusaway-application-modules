package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;

public final class WalkToStopVertex extends AbstractStopVertex {

  public WalkToStopVertex(GraphContext context, StopEntry stop) {
    super(context, stop);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return getVertexLabelForStop(_stop);
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "WalkToStopVertex(stop=" + _stop.getId() + ")";
  }

  public static String getVertexLabelForStop(StopEntry stopEntry) {
    return "walk_to_stop_" + stopEntry.getId();
  }
}
