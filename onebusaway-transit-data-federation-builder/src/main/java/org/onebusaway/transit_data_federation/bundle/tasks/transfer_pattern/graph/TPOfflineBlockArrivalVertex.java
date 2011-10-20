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
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineBlockArrivalVertex extends AbstractTPOfflineBlockVertex
    implements Comparable<TPOfflineBlockArrivalVertex> {

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
      edges.add(new TPOfflineTransferEdge(_context, this, transfer));

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
