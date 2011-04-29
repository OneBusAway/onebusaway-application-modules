package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPDepartureVertex extends AbstractTPPathStateVertex {

  public TPDepartureVertex(GraphContext context, TPState pathState) {
    super(context, pathState, true);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    // Return to the street network
    Edge edge = new WaitingBeginsAtStopEdge(_context, getStop(), true);
    return Arrays.asList(edge);
  }

  @Override
  public Collection<Edge> getOutgoing() {
    Edge edge = new TPDepartureEdge(_context, _pathState);
    return Arrays.asList(edge);
  }

  @Override
  public String toString() {
    return "TPDepartureVertex(" + _pathState + ")";
  }
  
  
  @Override
  public int hashCode() {
    return _pathState.getNode().hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if( this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TPDepartureVertex v = (TPDepartureVertex) obj;
    TransferNode a = _pathState.getNode();
    TransferNode b = v._pathState.getNode();
    return a.equals(b);
  }  
}
