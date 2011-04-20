package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPPathVertex extends AbstractVertexWithEdges implements
    HasStopTransitVertex {

  private TPState _pathState;

  public TPPathVertex(GraphContext context, TPState pathState) {
    super(context);
    _pathState = pathState;
  }

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
    List<Pair<StopEntry>> path = _pathState.getPath();
    Pair<StopEntry> pair = path.get(_pathState.getPathIndex());
    return pair.getFirst();
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getOutgoing() {
    if (_pathState.hasCurrentStopPair()) {
      Edge edge = new TPStopPairEdge(_context, _pathState);
      return Arrays.asList(edge);
    } else {
      Edge edge = new TPWalkFromStopToDestEdge(_context, _pathState);
      return Arrays.asList(edge);
    }
  }
}
