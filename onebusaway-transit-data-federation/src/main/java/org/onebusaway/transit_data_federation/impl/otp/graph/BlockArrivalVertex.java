package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class BlockArrivalVertex extends AbstractBlockVertex implements
    HasEdges, Comparable<BlockArrivalVertex> {

  public BlockArrivalVertex(GraphContext graphContext,
      ArrivalAndDepartureInstance instance) {
    super(graphContext, instance);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "block_arrival: " + _instance.toString();
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

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();
    ArrivalAndDepartureInstance previous = service.getPreviousStopArrivalAndDeparture(_instance);

    if (previous == null)
      return Collections.emptyList();

    return Arrays.asList((Edge) new BlockHopEdge(_context, previous, _instance));
  }

  @Override
  public int getDegreeOut() {
    return getOutgoing().size();
  }

  @Override
  public Collection<Edge> getOutgoing() {

    List<Edge> edges = new ArrayList<Edge>();
    StopEntry stop = _instance.getStop();

    /**
     * We can continue on our current route if applicable
     */
    if (SupportLibrary.hasNextStopTime(_instance)) {
      edges.add(new BlockDwellEdge(_context, _instance));
    }

    /**
     * We can alight from the vehicle to the street network
     */
    edges.add(new ArrivalEdge(_context, _instance));

    /**
     * We can alight from the vehicle AND transfer to another stop
     */
    StopTransferService stopTransferService = _context.getStopTransferService();
    List<StopTransfer> transfers = stopTransferService.getTransfersFromStop(stop);

    for (StopTransfer transfer : transfers)
      edges.add(new ArrivalAndTransferEdge(_context, _instance, transfer));

    return edges;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "block_arrival: " + _instance.toString();
  }

  @Override
  public int compareTo(BlockArrivalVertex o) {
    long t1 = this._instance.getBestArrivalTime();
    long t2 = o._instance.getBestArrivalTime();
    return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
  }
}
