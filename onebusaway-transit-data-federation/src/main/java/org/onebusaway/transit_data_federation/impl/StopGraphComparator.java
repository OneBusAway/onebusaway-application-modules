/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.onebusaway.transit_data_federation.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
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