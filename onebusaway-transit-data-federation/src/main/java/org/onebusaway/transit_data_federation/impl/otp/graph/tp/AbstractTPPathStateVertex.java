package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;

abstract class AbstractTPPathStateVertex extends AbstractVertexWithEdges
    implements HasStopTransitVertex, HasPathStateVertex {

  protected final TPState _pathState;

  protected final boolean _isDeparture;

  public AbstractTPPathStateVertex(GraphContext context, TPState pathState,
      boolean isDeparture) {
    super(context);
    _pathState = pathState;
    _isDeparture = isDeparture;
  }

  public TPState getPathState() {
    return _pathState;
  }

  public boolean isDeparture() {
    return _isDeparture;
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public double getX() {
    return getStop().getStopLon();
  }

  @Override
  public double getY() {
    return getStop().getStopLat();
  }

  /****
   * {@link HasStopTransitVertex} Interface
   ****/

  @Override
  public StopEntry getStop() {
    Pair<StopEntry> pair = _pathState.getStops();
    boolean pickFirstStop = _isDeparture ^ _pathState.isReverse();
    return pickFirstStop ? pair.getFirst() : pair.getSecond();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isDeparture ? 1231 : 1237);
    result = prime * result + _pathState.hashCode();
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
    AbstractTPPathStateVertex other = (AbstractTPPathStateVertex) obj;
    if (_isDeparture != other._isDeparture)
      return false;
    if (!_pathState.equals(other._pathState))
      return false;
    return true;
  }

  /****
   * {@link Object} Interface
   ****/

}
