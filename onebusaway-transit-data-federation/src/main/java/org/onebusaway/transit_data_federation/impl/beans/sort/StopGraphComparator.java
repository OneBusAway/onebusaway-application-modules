/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.beans.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.beans.DirectedGraph;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopGraphComparator implements Comparator<StopEntry> {

  private DirectedGraph<StopEntry> _graph;

  private Map<StopEntry, Double> _maxDistance = new HashMap<StopEntry, Double>();

  public StopGraphComparator(DirectedGraph<StopEntry> graph) {
    _graph = graph;
  }

  public int compare(StopEntry o1, StopEntry o2) {
    double d1 = getMaxDistance(o1);
    double d2 = getMaxDistance(o2);
    return d1 == d2 ? 0 : (d1 < d2 ? 1 : -1);
  }

  private double getMaxDistance(StopEntry stop) {
    return getMaxDistance(stop, new HashSet<StopEntry>());
  }

  private double getMaxDistance(StopEntry stop, Set<StopEntry> visited) {
    Double d = _maxDistance.get(stop);
    if (d != null)
      return d;

    if (!visited.add(stop))
      throw new IllegalStateException("cycle");

    double dMax = 0.0;
    for (StopEntry next : _graph.getOutboundNodes(stop)) {
      double potential = SphericalGeometryLibrary.distance(stop.getStopLat(),
          stop.getStopLon(), next.getStopLat(), next.getStopLon())
          + getMaxDistance(next, visited);
      if (potential > dMax)
        dMax = potential;
    }
    _maxDistance.put(stop, dMax);
    return dMax;
  }
}