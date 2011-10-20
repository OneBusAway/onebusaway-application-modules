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
