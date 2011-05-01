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
import org.opentripplanner.routing.edgetype.FreeEdge;

public class TPOfflineOriginVertex extends AbstractStopVertexWithEdges {

  private final List<StopTimeInstance> _instances;

  private final Map<StopEntry, Integer> _nearbyStopsAndWalkTimes;

  private final Map<StopEntry, List<StopTimeInstance>> _nearbyStopTimeInstances;

  public TPOfflineOriginVertex(GraphContext context, StopEntry stop,
      List<StopTimeInstance> instances,
      Map<StopEntry, Integer> nearbyStopsAndWalkTimes,
      Map<StopEntry, List<StopTimeInstance>> nearbyStopTimeInstances) {
    super(context, stop);
    _instances = instances;
    _nearbyStopsAndWalkTimes = nearbyStopsAndWalkTimes;
    _nearbyStopTimeInstances = nearbyStopTimeInstances;
  }

  @Override
  public Collection<Edge> getOutgoing() {

    List<Edge> edges = new ArrayList<Edge>();
    edges.add(new TPOfflineStopTimeInstancesEdge(_context, this, _instances, 0));

    /**
     * We can't ignore the fact that it might be faster to just walk to a
     * different stop (like across the street)
     */
    for (Map.Entry<StopEntry, Integer> entry : _nearbyStopsAndWalkTimes.entrySet()) {

      StopEntry nearbyStop = entry.getKey();
      int walkTime = entry.getValue();

      List<StopTimeInstance> instances = _nearbyStopTimeInstances.get(nearbyStop);

      if (instances.isEmpty())
        continue;

      TPOfflineNearbyStopsVertex vNearby = new TPOfflineNearbyStopsVertex(
          _context, nearbyStop, walkTime, instances);

      edges.add(new FreeEdge(this, vNearby));
    }

    return edges;
  }
}
