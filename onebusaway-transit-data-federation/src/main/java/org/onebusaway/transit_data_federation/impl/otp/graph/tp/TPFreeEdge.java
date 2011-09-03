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
package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.Vertex;

public class TPFreeEdge extends AbstractEdge {

  private final Vertex _from;
  private final Vertex _to;

  public TPFreeEdge(GraphContext context, Vertex from, Vertex to) {
    super(context);
    _from = from;
    _to = to;
  }

  @Override
  public State traverse(State s0) {
    return s0.edit(this, createNarrative(s0)).makeState();
  }

  private EdgeNarrative createNarrative(State s0) {
    return narrative(s0, _from, _to);
  }
}
