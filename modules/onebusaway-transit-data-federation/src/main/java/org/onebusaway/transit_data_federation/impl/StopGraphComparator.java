/**
 * 
 */
package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;

import edu.washington.cs.rse.collections.graph.DirectedGraph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StopGraphComparator implements Comparator<Stop> {

  private DirectedGraph<Stop> _graph;

  private Map<Stop, Double> _maxDistance = new HashMap<Stop, Double>();

  public StopGraphComparator(DirectedGraph<Stop> graph) {
    _graph = graph;
  }

  public int compare(Stop o1, Stop o2) {
    double d1 = getMaxDistance(o1);
    double d2 = getMaxDistance(o2);
    return d1 == d2 ? 0 : (d1 < d2 ? 1 : -1);
  }

  private double getMaxDistance(Stop stop) {
    return getMaxDistance(stop, new HashSet<Stop>());
  }

  private double getMaxDistance(Stop stop, Set<Stop> visited) {
    Double d = _maxDistance.get(stop);
    if (d != null)
      return d;

    if (!visited.add(stop))
      throw new IllegalStateException("cycle");

    double dMax = 0.0;
    for (Stop next : _graph.getOutboundNodes(stop)) {
      double potential = DistanceLibrary.distance(stop, next) + getMaxDistance(next, visited);
      if (potential > dMax)
        dMax = potential;
    }
    _maxDistance.put(stop, dMax);
    return dMax;
  }
}