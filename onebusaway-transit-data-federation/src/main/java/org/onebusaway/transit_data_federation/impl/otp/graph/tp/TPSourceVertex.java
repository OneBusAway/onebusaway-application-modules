package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.GraphPath;

public class TPSourceVertex extends AbstractVertexWithEdges {

  private final TPQueryData _queryData;

  public TPSourceVertex(GraphContext context, TPQueryData queryData) {
    super(context);
    if (queryData == null)
      throw new IllegalArgumentException();
    _queryData = queryData;
  }

  @Override
  public double getX() {
    Vertex v = _queryData.getSourceVertex();
    return v.getX();
  }

  @Override
  public double getY() {
    Vertex v = _queryData.getSourceVertex();
    return v.getY();
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getOutgoing() {
    Map<StopEntry, GraphPath> sourceStops = _queryData.getSourceStops();

    List<Edge> edges = new ArrayList<Edge>(sourceStops.size());
    for (Map.Entry<StopEntry, GraphPath> entry : sourceStops.entrySet()) {
      StopEntry stop = entry.getKey();
      GraphPath path = entry.getValue();
      Edge edge = new TPWalkFromSourceToStopEdge(_context, _queryData, stop,
          path);
      edges.add(edge);
    }
    return edges;
  }

  @Override
  public int hashCode() {
    return _queryData.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TPSourceVertex other = (TPSourceVertex) obj;
    return this._queryData.equals(other._queryData);
  }
}
