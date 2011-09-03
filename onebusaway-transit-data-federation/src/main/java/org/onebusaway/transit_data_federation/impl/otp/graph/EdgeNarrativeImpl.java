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

import java.util.Collections;
import java.util.Set;

import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.MutableEdgeNarrative;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.Vertex;

import com.vividsolutions.jts.geom.Geometry;

public class EdgeNarrativeImpl implements EdgeNarrative, MutableEdgeNarrative {

  private Vertex fromVertex;

  private Vertex toVertex;

  public EdgeNarrativeImpl(Vertex fromVertex, Vertex toVertex) {
    this.fromVertex = fromVertex;
    this.toVertex = toVertex;
  }

  /****
   * {@link MutableEdgeNarrative} Interface
   ****/

  @Override
  public void setFromVertex(Vertex fromVertex) {
    this.fromVertex = fromVertex;
  }

  @Override
  public void setToVertex(Vertex toVertex) {
    this.toVertex = toVertex;
  }

  /****
   * {@link EdgeNarrative} Interface
   ****/

  @Override
  public Vertex getFromVertex() {
    return fromVertex;
  }

  @Override
  public Vertex getToVertex() {
    return toVertex;
  }

  @Override
  public TraverseMode getMode() {
    return TraverseMode.TRANSIT;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Geometry getGeometry() {
    return null;
  }

  @Override
  public double getDistance() {
    return 0;
  }

  @Override
  public Trip getTrip() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRoundabout() {
    return false;
  }

  @Override
  public Set<String> getNotes() {
    return Collections.emptySet();
  }

  /****
   * {@link Object} Interface
   ****/

  public String toString() {
    return "EdgeNarrative(from=" + fromVertex + " to=" + toVertex + ")";
  }
}
