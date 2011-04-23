package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingEndsAtStopEdge;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPArrivalVertex extends AbstractTPPathStateVertex {

  public TPArrivalVertex(GraphContext context, TPState pathState) {
    super(context, pathState, false);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    Edge edge = new TPArrivalReverseEdge(_context, _pathState);
    return Arrays.asList(edge);
  }

  @Override
  public Collection<Edge> getOutgoing() {
    // We stop waiting and move back to the street
    Edge edge = new WaitingEndsAtStopEdge(_context, getStop(), false);
    return Arrays.asList(edge);
  }

  @Override
  public String toString() {
    return "TPArrivalVertex(" + _pathState + ")";
  }
}
