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

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;

public class ArrivalAndTransferEdge extends AbstractEdge {

  private ArrivalAndDepartureInstance _instance;

  private StopTransfer _transfer;

  public ArrivalAndTransferEdge(GraphContext context,
      ArrivalAndDepartureInstance instance, StopTransfer transfer) {
    super(context);
    _instance = instance;
    _transfer = transfer;
  }

  @Override
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();
    if (options.isArriveBy())
      return traverseReverse(s0);
    else
      return traverseForward(s0);
  }

  private State traverseForward(State s0) {

    TraverseOptions options = s0.getOptions();

    /**
     * Check if we've reached our transfer limit
     */
    if (s0.getNumBoardings() >= options.maxTransfers)
      return null;

    int transferTime = ItineraryWeightingLibrary.computeTransferTime(_transfer,
        options);
    int t = transferTime + options.minTransferTime;

    EdgeNarrative narrative = createNarrative(s0, s0.getTime() + t * 1000);

    StateEditor edit = s0.edit(this, narrative);
    edit.incrementTimeInSeconds(t);

    double weight = ItineraryWeightingLibrary.computeTransferWeight(
        transferTime, options);
    edit.incrementWeight(weight);

    return edit.makeState();
  }

  private State traverseReverse(State s0) {

    TraverseOptions options = s0.getOptions();

    int transferTime = ItineraryWeightingLibrary.computeTransferTime(_transfer,
        options);
    double weight = ItineraryWeightingLibrary.computeTransferWeight(
        transferTime, options);

    EdgeNarrative narrative = createNarrative(s0, s0.getTime());
    StateEditor edit = s0.edit(this, narrative);
    edit.setTime(_instance.getBestArrivalTime());
    edit.incrementWeight(weight);

    return edit.makeState();
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrative createNarrative(State s0, long time) {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    DepartureVertex toVertex = new DepartureVertex(_context,
        _transfer.getStop(), time);
    return narrative(s0, fromVertex, toVertex);
  }
}
