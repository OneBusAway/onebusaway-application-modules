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
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class ArrivalEdge extends AbstractEdge {

  private final ArrivalAndDepartureInstance _instance;

  public ArrivalEdge(GraphContext context, ArrivalAndDepartureInstance instance) {
    super(context);
    _instance = instance;
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
      return null;

    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  private State traverseReverse(State s0) {

    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrative createNarrative(State s0) {
    Vertex fromVertex = new BlockArrivalVertex(_context, _instance);
    Vertex toVertex = new ArrivalVertex(_context, _instance.getStop(),
        s0.getTime());
    return narrative(s0, fromVertex, toVertex);
  }

}
