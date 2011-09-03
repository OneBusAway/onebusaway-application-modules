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

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;

abstract class AbstractTPPathStateVertex extends AbstractVertexWithEdges
    implements HasStopTransitVertex, HasPathStateVertex {

  protected final TPState _pathState;

  protected final boolean _isDeparture;

  public AbstractTPPathStateVertex(GraphContext context, TPState pathState,
      boolean isDeparture) {
    super(context);
    _pathState = pathState;
    _isDeparture = isDeparture;
  }

  public TPState getPathState() {
    return _pathState;
  }

  public boolean isDeparture() {
    return _isDeparture;
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public double getX() {
    return getStop().getStopLon();
  }

  @Override
  public double getY() {
    return getStop().getStopLat();
  }

  /****
   * {@link HasStopTransitVertex} Interface
   ****/

  @Override
  public StopEntry getStop() {
    Pair<StopEntry> pair = _pathState.getStops();
    boolean pickFirstStop = _isDeparture ^ _pathState.isReverse();
    return pickFirstStop ? pair.getFirst() : pair.getSecond();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isDeparture ? 1231 : 1237);
    result = prime * result + _pathState.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractTPPathStateVertex other = (AbstractTPPathStateVertex) obj;
    if (_isDeparture != other._isDeparture)
      return false;
    if (!_pathState.equals(other._pathState))
      return false;
    return true;
  }

  /****
   * {@link Object} Interface
   ****/

}
