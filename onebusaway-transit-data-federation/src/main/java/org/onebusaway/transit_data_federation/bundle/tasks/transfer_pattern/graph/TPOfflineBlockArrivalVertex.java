package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineBlockArrivalVertex extends AbstractTPOfflineBlockVertex implements
    Comparable<TPOfflineBlockArrivalVertex> {

  public TPOfflineBlockArrivalVertex(GraphContext graphContext,
      StopTimeInstance instance) {
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
  public Collection<Edge> getOutgoing() {

    List<Edge> edges = new ArrayList<Edge>();
    StopEntry stop = _instance.getStop();

    /**
     * We can continue on our current route if applicable
     */
    if (_instance.getStopTime().hasNextStop()) {
      edges.add(new TPOfflineBlockDwellEdge(_context, _instance));
    }

    /**
     * We can alight from the vehicle
     */
    edges.add(new TPOfflineArrivalEdge(_context, _instance));

    /**
     * We can alight from the vehicle AND transfer to another stop
     */
    StopTransferService stopTransferService = _context.getStopTransferService();
    List<StopTransfer> transfers = stopTransferService.getTransfersFromStop(stop);

    for (StopTransfer transfer : transfers)
      edges.add(new TPOfflineArrivalAndTransferEdge(_context, _instance, transfer));

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
  public int compareTo(TPOfflineBlockArrivalVertex o) {
    long t1 = this._instance.getArrivalTime();
    long t2 = o._instance.getArrivalTime();
    return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
  }
}
