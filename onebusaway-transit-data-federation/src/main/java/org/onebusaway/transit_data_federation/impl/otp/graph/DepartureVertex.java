package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public final class DepartureVertex extends AbstractVertex implements
    HasEdges {

  private static DateFormat _format = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private final StopEntry _stop;

  private final long _time;

  public DepartureVertex(GraphContext context, StopEntry stop, long time) {
    super(context);
    _stop = stop;
    _time = time;
  }
  
  public StopEntry getStop() {
    return _stop;
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "stop_departure_" + getStopId() + "_wait_" + _time;
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
    edges.add(new DepartureEdge(_context, _stop));
    return edges;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "DepartureVertex(stop=" + _stop.getId() + " time="
        + _format.format(new Date(_time)) + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_stop == null) ? 0 : _stop.hashCode());
    result = prime * result + (int) (_time ^ (_time >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DepartureVertex other = (DepartureVertex) obj;
    return _stop.equals(other._stop) && _time == other._time;
  }
}
