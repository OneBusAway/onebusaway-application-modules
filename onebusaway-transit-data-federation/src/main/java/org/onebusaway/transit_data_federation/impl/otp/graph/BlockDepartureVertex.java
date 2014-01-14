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
package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class BlockDepartureVertex extends AbstractBlockVertex implements
    HasEdges {

  public BlockDepartureVertex(GraphContext context,
      ArrivalAndDepartureInstance instance) {
    super(context, instance);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return "block_departure: " + _instance.toString();
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {

    List<Edge> edges = new ArrayList<Edge>();
    StopEntry stop = _instance.getStop();

    /**
     * We can stay on the bus if applicable
     */
    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    if (bst.getBlockSequence() > 0) {
      edges.add(new BlockDwellEdge(_context, _instance));
    }

    /**
     * We can get off the bus
     */
    edges.add(new DepartureReverseEdge(_context, _instance));

    /**
     * We can be coming from a transfer from another stop.
     */
    StopTransferService stopTransferService = _context.getStopTransferService();
    List<StopTransfer> transfers = stopTransferService.getTransfersToStop(stop);

    for (StopTransfer transfer : transfers)
      edges.add(new TransferAndDepartureEdge(_context, _instance, transfer));

    return edges;
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new BlockForwardHopEdge(_context, _instance));
    // ArrivalAndDepartureService service =
    // _context.getArrivalAndDepartureService();
    // ArrivalAndDepartureInstance nextStop =
    // service.getNextStopArrivalAndDeparture(_instance);
    // return Arrays.asList((Edge) new BlockHopEdge(_context, _instance,
    // nextStop));
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "block_departure: " + _instance.toString();
  }
}
