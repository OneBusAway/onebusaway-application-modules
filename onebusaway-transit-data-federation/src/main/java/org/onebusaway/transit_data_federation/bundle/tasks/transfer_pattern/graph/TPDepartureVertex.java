package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public final class TPDepartureVertex extends AbstractVertex implements HasEdges,
    HasStopTransitVertex {

  private final StopEntry _stop;

  public TPDepartureVertex(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public StopEntry getStop() {
    return _stop;
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "stop_departure_" + getStopId();
  }

  @Override
  public AgencyAndId getStopId() {
    return _stop.getId();
  }

  @Override
  public double getX() {
    return _stop.getStopLon();
  }

  @Override
  public double getY() {
    return _stop.getStopLat();
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
    List<Edge> edges = new ArrayList<Edge>(1);
    // Return to the street network
    edges.add(new WaitingBeginsAtStopEdge(_context, _stop, true));
    return edges;
  }

  @Override
  public int getDegreeOut() {
    return getOutgoing().size();
  }

  @Override
  public Collection<Edge> getOutgoing() {
    List<Edge> edges = new ArrayList<Edge>(1);
    // Let's board a bus!
    edges.add(new TPDepartureEdge(_context, _stop));
    return edges;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "DepartureVertex(stop=" + _stop.getId() + ")";
  }

  @Override
  public int hashCode() {
    return _stop.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TPDepartureVertex other = (TPDepartureVertex) obj;
    return _stop.equals(other._stop);
  }
}
