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
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class TransferAndDepartureEdge extends AbstractEdge {

  private ArrivalAndDepartureInstance _instance;

  private StopTransfer _transfer;

  public TransferAndDepartureEdge(GraphContext context,
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

    EdgeNarrative narrative = createNarrative(s0, s0.getTime());
    StateEditor edit = s0.edit(this, narrative);

    TraverseOptions options = s0.getOptions();
    int transferTime = computeTransferTime(options);
    edit.setTime(_instance.getBestArrivalTime());

    double weight = computeWeightForTransferTime(options, transferTime);
    edit.incrementWeight(weight);

    return edit.makeState();
  }

  private State traverseReverse(State s0) {

    TraverseOptions options = s0.getOptions();

    /**
     * Check if we've reached our transfer limit
     */
    if (s0.getNumBoardings() >= options.maxTransfers)
      return null;

    int transferTime = computeTransferTime(options);
    long t = s0.getTime() - ((transferTime + options.minTransferTime) * 1000);
    EdgeNarrative narrative = createNarrative(s0, t);
    StateEditor edit = s0.edit(this, narrative);
    edit.setTime(t);

    double weight = computeWeightForTransferTime(options, transferTime);
    edit.incrementWeight(weight);

    return edit.makeState();
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrative createNarrative(State s0, long time) {
    Vertex fromVertex = new ArrivalVertex(_context, _transfer.getStop(), time);
    Vertex toVertex = new BlockDepartureVertex(_context, _instance);
    return narrative(s0, fromVertex, toVertex);
  }

  private double computeWeightForTransferTime(TraverseOptions options,
      int transferTime) {
    return transferTime * options.walkReluctance + options.boardCost
        + options.minTransferTime * options.waitReluctance;
  }

  private int computeTransferTime(TraverseOptions options) {

    int transferTime = _transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    double walkingVelocity = options.speed;
    double distance = _transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t;
  }
}
