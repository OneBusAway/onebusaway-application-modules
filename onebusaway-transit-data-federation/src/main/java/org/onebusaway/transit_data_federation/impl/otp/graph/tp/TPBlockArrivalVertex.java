package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.edgetype.FreeEdge;

public class TPBlockArrivalVertex extends AbstractTPPathStateVertex {

  private final ArrivalAndDepartureInstance _departure;

  private final ArrivalAndDepartureInstance _arrival;

  public TPBlockArrivalVertex(GraphContext context, TPState pathState,
      ArrivalAndDepartureInstance departure, ArrivalAndDepartureInstance arrival) {
    super(context, pathState, false);
    _departure = departure;
    _arrival = arrival;
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    Edge edge = new TPBlockHopEdge(_context, _pathState, _departure, _arrival);
    return Arrays.asList(edge);
  }

  @Override
  public Collection<Edge> getOutgoing() {
    if (_pathState.hasTransfers()) {
      List<Edge> edges = new ArrayList<Edge>();
      List<TPState> transferStates = _pathState.getTransferStates();
      for (TPState nextState : transferStates) {
        Edge edge = new TPTransferEdge(_context, _pathState, nextState,
            _departure, _arrival, false);
        edges.add(edge);
      }
      return edges;
    } else {
      TPArrivalVertex to = new TPArrivalVertex(_context, _pathState);
      Edge edge = new FreeEdge(this, to);
      return Arrays.asList(edge);
    }
  }

  @Override
  public String toString() {
    return "TPBlockArrivalVertex(" + _pathState.toString() + ")";
  }

}
