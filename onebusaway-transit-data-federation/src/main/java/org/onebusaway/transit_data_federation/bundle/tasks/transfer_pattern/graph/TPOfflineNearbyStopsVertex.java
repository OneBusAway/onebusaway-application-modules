package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertexWithEdges;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;

public class TPOfflineNearbyStopsVertex extends AbstractStopVertexWithEdges {

  private final Map<StopEntry, Integer> _nearbyStopsAndWalkTimes;
  private final Map<StopEntry, List<StopTimeInstance>> _nearbyStopTimeInstances;

  public TPOfflineNearbyStopsVertex(GraphContext context, StopEntry stop,
      Map<StopEntry, Integer> nearbyStopsAndWalkTimes,
      Map<StopEntry, List<StopTimeInstance>> nearbyStopTimeInstances) {
    super(context, stop);
    _nearbyStopsAndWalkTimes = nearbyStopsAndWalkTimes;
    _nearbyStopTimeInstances = nearbyStopTimeInstances;
  }

  @Override
  public Collection<Edge> getOutgoing() {

    List<Edge> edges = new ArrayList<Edge>();

    for (Map.Entry<StopEntry, Integer> entry : _nearbyStopsAndWalkTimes.entrySet()) {

      StopEntry nearbyStop = entry.getKey();
      int walkTime = entry.getValue();

      List<StopTimeInstance> instances = _nearbyStopTimeInstances.get(nearbyStop);

      if (instances.isEmpty())
        continue;

      edges.add(new TPOfflineStopTimeInstancesEdge(_context, this, instances,
          walkTime));
    }

    return edges;
  }

  @Override
  public String toString() {
    return "TPOfflineNearbyStopsVertex()";
  }

}
