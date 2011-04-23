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

public class TPBlockDepartureVertex extends AbstractTPPathStateVertex {

  private final ArrivalAndDepartureInstance _departure;

  private final ArrivalAndDepartureInstance _arrival;

  public TPBlockDepartureVertex(GraphContext context, TPState pathState,
      ArrivalAndDepartureInstance departure, ArrivalAndDepartureInstance arrival) {
    super(context, pathState, true);
    _departure = departure;
    _arrival = arrival;
  }

  public ArrivalAndDepartureInstance getDeparture() {
    return _departure;
  }

  public ArrivalAndDepartureInstance getArrival() {
    return _arrival;
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    if (_pathState.hasTransfers()) {
      List<Edge> edges = new ArrayList<Edge>();
      for (TPState prevState : _pathState.getTransferStates()) {
        Edge edge = new TPTransferEdge(_context, prevState, _pathState,
            _departure, _arrival, false);
        edges.add(edge);
      }
      return edges;
    } else {
      TPDepartureVertex from = new TPDepartureVertex(_context, _pathState);
      Edge edge = new FreeEdge(from, this);
      return Arrays.asList(edge);
    }
  }

  @Override
  public Collection<Edge> getOutgoing() {
    Edge edge = new TPBlockHopEdge(_context, _pathState, _departure, _arrival);
    return Arrays.asList(edge);
  }

  @Override
  public String toString() {
    return "TPBlockDepartureVertex(" + _pathState.toString() + ")";
  }
}
