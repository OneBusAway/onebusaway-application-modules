package org.onebusaway.transit_data_federation.impl.otp.graph.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.MinTravelTimeUsingTransitHeuristic.CustomGraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertexWithEdges;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class MinStopVertex extends AbstractStopVertexWithEdges {

  public MinStopVertex(GraphContext context, StopEntry stop) {
    super(context, stop);
  }

  @Override
  public Collection<Edge> getIncoming() {

    CustomGraphContext cgc = (CustomGraphContext) _context;

    /**
     * Check to see if we have an existing shortest path
     */
    Integer travelTime = cgc.getMinTravelTimeToTargetForStop(_stop);

    if (travelTime != null) {
      Edge edge = new MinTravelEdge(_context, this, cgc.getTarget(), travelTime);
      return Arrays.asList(edge);
    }

    StopHopService stopHopService = _context.getStopHopService();
    StopTransferService stopTransferService = _context.getStopTransferService();

    List<StopHop> hops = stopHopService.getHopsToStop(_stop);
    List<StopTransfer> transfers = stopTransferService.getTransfersToStop(_stop);

    List<Edge> edges = new ArrayList<Edge>();

    for (StopHop hop : hops) {
      Vertex from = new MinStopVertex(_context, hop.getStop());
      edges.add(new MinTravelEdge(_context, from, this, hop.getMinTravelTime()));
    }

    for (StopTransfer transfer : transfers) {
      Vertex from = new MinStopVertex(_context, transfer.getStop());
      edges.add(new MinTravelEdge(_context, from, this,
          transfer.getMinTransferTime()));
    }

    return edges;
  }

  @Override
  public Collection<Edge> getOutgoing() {

    CustomGraphContext context = (CustomGraphContext) _context;

    /**
     * Check to see if we have an existing shortest path
     */
    Integer travelTime = context.getMinTravelTimeToTargetForStop(_stop);

    if (travelTime != null) {
      Edge edge = new MinTravelEdge(_context, this, context.getTarget(),
          travelTime);
      return Arrays.asList(edge);
    }

    StopHopService stopHopService = _context.getStopHopService();
    StopTransferService stopTransferService = _context.getStopTransferService();

    List<StopHop> hops = stopHopService.getHopsFromStop(_stop);
    List<StopTransfer> transfers = stopTransferService.getTransfersFromStop(_stop);

    List<Edge> edges = new ArrayList<Edge>();

    for (StopHop hop : hops) {
      Vertex to = new MinStopVertex(_context, hop.getStop());
      edges.add(new MinTravelEdge(_context, this, to, hop.getMinTravelTime()));
    }

    TraverseOptions options = context.getOptions();

    for (StopTransfer transfer : transfers) {
      Vertex to = new MinStopVertex(_context, transfer.getStop());
      int minTransferTime = transfer.getMinTransferTime();
      if (minTransferTime == 0)
        minTransferTime = (int) (transfer.getDistance() / options.speed);
      edges.add(new MinTravelEdge(_context, this, to, minTransferTime));
    }

    return edges;
  }

  @Override
  public String toString() {
    return _stop.getId().toString();
  }
}
