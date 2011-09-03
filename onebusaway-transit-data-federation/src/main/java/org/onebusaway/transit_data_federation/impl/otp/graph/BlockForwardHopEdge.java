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
import org.onebusaway.transit_data_federation.impl.otp.OBAState;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

/**
 * A transit vehicle's journey between departure at one stop and arrival at the
 * next. This version represents a set of such journeys specified by a
 * TripPattern.
 */
public class BlockForwardHopEdge extends AbstractEdge {

  private static final long serialVersionUID = 1L;

  private final ArrivalAndDepartureInstance _from;

  public BlockForwardHopEdge(GraphContext context,
      ArrivalAndDepartureInstance from) {
    super(context);

    if (from == null)
      throw new IllegalArgumentException("from cannot be null");

    _from = from;
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

    OBATraverseOptions obaOpts = (OBATraverseOptions) s0.getOptions();
    if (obaOpts.extraSpecialMode)
      return extraSpecialMode(s0, obaOpts);

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    State results = null;

    OBAState state = (OBAState) s0;

    int maxBlockSequence = state.getMaxBlockSequence();

    if (maxBlockSequence < 0) {
      ArrivalAndDepartureInstance nextTransferStop = service.getNextTransferStopArrivalAndDeparture(_from);
      if (nextTransferStop != null) {

        long departure = _from.getBestDepartureTime();
        long arrival = nextTransferStop.getBestArrivalTime();
        int runningTime = (int) ((arrival - departure) / 1000);

        Vertex fromVertex = new BlockDepartureVertex(_context, _from);
        Vertex toVertex = new BlockArrivalVertex(_context, nextTransferStop);
        EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

        StateEditor edit = s0.edit(this, narrative);
        edit.incrementTimeInSeconds(runningTime);
        edit.incrementWeight(runningTime);

        State result = edit.makeState();
        results = result.addToExistingResultChain(results);

        maxBlockSequence = nextTransferStop.getBlockStopTime().getBlockSequence();
      } else {
        maxBlockSequence = Integer.MAX_VALUE;
      }
    }

    ArrivalAndDepartureInstance nextStop = service.getNextStopArrivalAndDeparture(_from);

    if (nextStop != null
        && nextStop.getBlockStopTime().getBlockSequence() < maxBlockSequence) {

      long departure = _from.getBestDepartureTime();
      long arrival = nextStop.getBestArrivalTime();
      int runningTime = (int) ((arrival - departure) / 1000);

      Vertex fromVertex = new BlockDepartureVertex(_context, _from);
      Vertex toVertex = new BlockArrivalVertex(_context, nextStop);
      EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

      OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);
      edit.incrementTimeInSeconds(runningTime);
      edit.incrementWeight(runningTime);

      if (state.getMaxBlockSequence() < 0)
        edit.setMaxBlockSequence(maxBlockSequence);

      State tr = edit.makeState();
      results = tr.addToExistingResultChain(results);
    }

    return results;
  }

  private State extraSpecialMode(State s0, OBATraverseOptions obaOpts) {

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();
    ArrivalAndDepartureInstance nextStop = service.getNextStopArrivalAndDeparture(_from);

    if (nextStop == null)
      return null;

    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = new BlockArrivalVertex(_context, nextStop);
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    StateEditor edit = s0.edit(this, narrative);

    long departure = _from.getBestDepartureTime();
    long arrival = nextStop.getBestArrivalTime();
    int runningTime = (int) ((arrival - departure) / 1000);
    edit.incrementTimeInSeconds(runningTime);
    edit.incrementWeight(runningTime);

    return edit.makeState();
  }

  private State traverseReverse(State s0) {

    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = null;
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    StateEditor edit = s0.edit(this, narrative);
    int runningTime = (int) ((s0.getTime() - _from.getBestDepartureTime()) / 1000);
    edit.setTime(_from.getBestDepartureTime());
    edit.incrementWeight(runningTime);
    return edit.makeState();
  }
}
