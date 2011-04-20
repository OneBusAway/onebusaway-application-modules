package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.opentripplanner.routing.core.Vertex;

public class TPDestinationVertex extends AbstractVertexWithEdges {

  private final TPQueryData _queryData;

  public TPDestinationVertex(GraphContext context, TPQueryData queryData) {
    super(context);
    if (queryData == null)
      throw new IllegalArgumentException();
    _queryData = queryData;
  }

  @Override
  public double getX() {
    Vertex v = _queryData.getDestVertex();
    return v.getX();
  }

  @Override
  public double getY() {
    Vertex v = _queryData.getDestVertex();
    return v.getY();
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
    TPDestinationVertex other = (TPDestinationVertex) obj;
    return this._queryData.equals(other._queryData);
  }
}
