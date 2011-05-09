package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.SearchLocal;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingEndsAtStopEdge;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPArrivalVertex extends AbstractTPPathStateVertex implements
    SearchLocal {

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

  /****
   * {@link SearchLocal} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getSearchLocalValue() {
    return (T) _pathState.getNode().getArrivalLocalValue();
  }

  @Override
  public <T> void setSearchLocalValue(T value) {
    _pathState.getNode().setArrivalLocalValue(value);
  }

  /****
   * {@link Object} Interface
   ****/
  
  @Override
  public int hashCode() {
    return _pathState.getNode().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TPArrivalVertex v = (TPArrivalVertex) obj;
    TransferNode a = _pathState.getNode();
    TransferNode b = v._pathState.getNode();
    return a.equals(b);
  }


  @Override
  public String toString() {
    return "TPArrivalVertex(" + _pathState + ")";
  }
}
