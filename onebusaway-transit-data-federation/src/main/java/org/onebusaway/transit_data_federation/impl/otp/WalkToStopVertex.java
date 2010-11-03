package org.onebusaway.transit_data_federation.impl.otp;

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
    return "walk_to_stop_" + getStopId();
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "WalkToStopVertex(stop=" + _stop.getId() + ")";
  }
}
