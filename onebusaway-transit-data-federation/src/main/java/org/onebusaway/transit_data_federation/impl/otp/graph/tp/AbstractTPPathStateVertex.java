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
    return _isDeparture ? pair.getFirst() : pair.getSecond();
  }
}
