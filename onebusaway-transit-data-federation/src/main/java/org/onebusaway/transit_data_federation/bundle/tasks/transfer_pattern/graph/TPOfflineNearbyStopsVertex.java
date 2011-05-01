package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertexWithEdges;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;

public class TPOfflineNearbyStopsVertex extends AbstractStopVertexWithEdges {

  private final int _walkTime;

  private final List<StopTimeInstance> _instances;

  public TPOfflineNearbyStopsVertex(GraphContext context, StopEntry stop,
      int walkTime, List<StopTimeInstance> instances) {
    super(context, stop);
    _walkTime = walkTime;
    _instances = instances;
  }

  @Override
  public Collection<Edge> getOutgoing() {
    List<Edge> edges = new ArrayList<Edge>();
    edges.add(new TPOfflineStopTimeInstancesEdge(_context, this, _instances,
        _walkTime));
    return edges;
  }

  @Override
  public String toString() {
    return "TPOfflineNearbyStopsVertex(stop=" + _stop.getId() + ")";
  }

}
