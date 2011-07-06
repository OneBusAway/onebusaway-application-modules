package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.SearchLocal;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPDepartureVertex extends AbstractTPPathStateVertex implements
    SearchLocal {

  public TPDepartureVertex(GraphContext context, TPState pathState) {
    super(context, pathState, true);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    // Return to the street network
    WaitingBeginsAtStopEdge edge = new WaitingBeginsAtStopEdge(_context,
        getStop(), true);
    edge.setToVertex(this);
    return Arrays.asList((Edge) edge);
  }

  @Override
  public Collection<Edge> getOutgoing() {
    TPDepartureEdge edge = new TPDepartureEdge(_context, _pathState);
    edge.setFromVertex(this);
    return Arrays.asList((Edge) edge);
  }

  /****
   * {@link SearchLocal} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getSearchLocalValue() {
    return (T) _pathState.getNode().getDepartureLocalValue();
  }

  @Override
  public <T> void setSearchLocalValue(T value) {
    _pathState.getNode().setDepartureLocalValue(value);
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
    TPDepartureVertex v = (TPDepartureVertex) obj;
    TransferNode a = _pathState.getNode();
    TransferNode b = v._pathState.getNode();
    return a.equals(b);
  }

  @Override
  public String toString() {
    return "TPDepartureVertex(" + _pathState + ")";
  }
}
