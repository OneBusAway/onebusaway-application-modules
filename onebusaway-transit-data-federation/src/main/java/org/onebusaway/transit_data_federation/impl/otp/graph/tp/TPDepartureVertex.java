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

import java.util.Arrays;
import java.util.Collection;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.SearchLocal;
import org.onebusaway.transit_data_federation.impl.otp.graph.WaitingBeginsAtStopEdge;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferNode;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class TPDepartureVertex extends AbstractTPPathStateVertex implements
    SearchLocal {

  public TPDepartureVertex(GraphContext context, TPState pathState) {
    super(context, pathState, true);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public Collection<Edge> getIncoming() {
    // Return to the street network
    WaitingBeginsAtStopEdge edge = new WaitingBeginsAtStopEdge(_context,
        getStop(), true);
    edge.setToVertex(this);
    return Arrays.asList((Edge) edge);
  }

  @Override
  public Collection<Edge> getOutgoing() {
    TPDepartureEdge edge = new TPDepartureEdge(_context, _pathState);
    edge.setFromVertex(this);
    return Arrays.asList((Edge) edge);
  }

  /****
   * {@link SearchLocal} Interface
   ****/

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getSearchLocalValue() {
    return (T) _pathState.getNode().getDepartureLocalValue();
  }

  @Override
  public <T> void setSearchLocalValue(T value) {
    _pathState.getNode().setDepartureLocalValue(value);
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    return _pathState.getNode().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TPDepartureVertex v = (TPDepartureVertex) obj;
    TransferNode a = _pathState.getNode();
    TransferNode b = v._pathState.getNode();
    return a.equals(b);
  }

  @Override
  public String toString() {
    return "TPDepartureVertex(" + _pathState + ")";
  }
}
