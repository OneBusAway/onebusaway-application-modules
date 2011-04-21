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

  public TPState getPathState() {
    return _pathState;
  }
  
  /****
   * {@link HasStopTransitVertex} Interface
   ****/

  @Override
  public StopEntry getStop() {
    List<Pair<StopEntry>> path = _pathState.getPath();
    int index = _pathState.getPathIndex();
    if (index >= path.size()) {
      Pair<StopEntry> pair = path.get(path.size() - 1);
      return pair.getSecond();
    } else {
      Pair<StopEntry> pair = path.get(index);
      return pair.getFirst();
    }
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getOutgoing() {
    Edge edge = new TPStopPairEdge(_context, _pathState);
    return Arrays.asList(edge);
  }

  @Override
  public String toString() {
    return _pathState.toString();
  }
}
