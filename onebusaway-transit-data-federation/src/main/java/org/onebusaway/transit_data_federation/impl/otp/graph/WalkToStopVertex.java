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
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;

public final class WalkToStopVertex extends AbstractStopVertex {

  public WalkToStopVertex(GraphContext context, StopEntry stop) {
    super(context, stop);
  }

  /****
   * {@link Vertex} Interface
   ****/

  @Override
  public String getLabel() {
    return getVertexLabelForStop(_stop);
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return "WalkToStopVertex(stop=" + _stop.getId() + ")";
  }

  public static String getVertexLabelForStop(StopEntry stopEntry) {
    return "walk_to_stop_" + stopEntry.getId();
  }
}
