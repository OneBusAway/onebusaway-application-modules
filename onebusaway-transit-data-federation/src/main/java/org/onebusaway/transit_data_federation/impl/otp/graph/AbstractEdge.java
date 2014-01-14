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

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.patch.Patch;

public abstract class AbstractEdge implements Edge {

  protected final GraphContext _context;

  private Vertex _fromVertex = null;
  private Vertex _toVertex = null;

  public AbstractEdge(GraphContext context) {
    _context = context;
  }

  public void setFromVertex(Vertex fromVertex) {
    _fromVertex = fromVertex;
  }

  public void setToVertex(Vertex toVertex) {
    _toVertex = toVertex;
  }

  /****
   * {@link Edge} Interface
   ****/

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public State optimisticTraverse(State s0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addPatch(Patch patch) {

  }

  @Override
  public List<Patch> getPatches() {
    return null;
  }

  @Override
  public void removePatch(Patch patch) {

  }

  /****
   * Protected Methods
   * 
   * @param state TODO
   ****/

  protected EdgeNarrative narrative(State state, Vertex fromVertex,
      Vertex toVertex) {
    if (_fromVertex != null)
      fromVertex = _fromVertex;
    if (_toVertex != null)
      toVertex = _toVertex;
    if (state.getOptions().isArriveBy())
      toVertex = state.getVertex();
    else
      fromVertex = state.getVertex();
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
