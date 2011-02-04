package org.onebusaway.transit_data_federation.impl.otp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class BlockDepartureVertex extends AbstractBlockVertex implements
    HasEdges {

  public BlockDepartureVertex(GraphContext context,
      ArrivalAndDepartureInstance instance) {
    super(context, instance);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "block_departure: " + _instance.toString();
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public int getDegreeIn() {
    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    return bst.getBlockSequence() > 0 ? 1 : 0;
  }

  @Override
  public Collection<Edge> getIncoming() {

    List<Edge> edges = new ArrayList<Edge>();

    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    if (bst.getBlockSequence() > 0) {
      edges.add(new BlockDwellEdge(_context, _instance));
    }

    return edges;
  }

  @Override
  public int getDegreeOut() {
    return 1;
  }

  @Override
  public Collection<Edge> getOutgoing() {
    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();
    ArrivalAndDepartureInstance nextStop = service.getNextStopArrivalAndDeparture(_instance);
    return Arrays.asList((Edge) new BlockHopEdge(_context, _instance, nextStop));
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "block_departure: " + _instance.toString();
  }
}
