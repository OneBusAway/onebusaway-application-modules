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

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.Vertex;

/**
 * A transit vehicle's journey between departure at one stop and arrival at the
 * next. This version represents a set of such journeys specified by a
 * TripPattern.
 */
public class TPOfflineBlockHopEdge extends AbstractEdge {

  private static final long serialVersionUID = 1L;

  private final StopTimeInstance _from;

  private final StopTimeInstance _to;

  public TPOfflineBlockHopEdge(GraphContext context, StopTimeInstance from,
      StopTimeInstance to) {
    super(context);

    if (from == null)
      throw new IllegalArgumentException("from cannot be null");
    if (to == null)
      throw new IllegalArgumentException("to cannot be null");

    _from = from;
    _to = to;
  }

  @Override
  public State traverse(State s0) {

    EdgeNarrative narrative = createNarrative(s0);
    StateEditor edit = s0.edit(this, narrative);
    int runningTime = computeRunningTime();
    edit.incrementTimeInSeconds(runningTime);
    edit.incrementWeight(runningTime);
    return edit.makeState();
  }

  /****
   * Private Methods
   ****/

  private int computeRunningTime() {
    long departure = _from.getDepartureTime();
    long arrival = _to.getArrivalTime();
    int runningTime = (int) ((arrival - departure) / 1000);
    return runningTime;
  }

  private EdgeNarrative createNarrative(State s0) {
    Vertex fromVertex = new TPOfflineBlockDepartureVertex(_context, _from);
    Vertex toVertex = new TPOfflineBlockArrivalVertex(_context, _to);
    return narrative(s0, fromVertex, toVertex);
  }
}
