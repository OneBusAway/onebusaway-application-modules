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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractVertexWithEdges;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public abstract class AbstractTPOfflineBlockVertex extends AbstractVertexWithEdges implements
    HasStopTransitVertex, HasStopTimeInstanceTransitVertex {

  protected final StopTimeInstance _instance;

  public AbstractTPOfflineBlockVertex(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
  }

  public StopTimeInstance getInstance() {
    return _instance;
  }

  @Override
  public StopEntry getStop() {
    return _instance.getStop();
  }

  @Override
  public AgencyAndId getStopId() {
    StopEntry stop = _instance.getStop();
    return stop.getId();
  }

  @Override
  public double getX() {
    return _instance.getStop().getStopLon();
  }

  @Override
  public double getY() {
    return _instance.getStop().getStopLat();
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    return _instance.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractTPOfflineBlockVertex bav = (AbstractTPOfflineBlockVertex) obj;
    return _instance.equals(bav.getInstance());
  }
}
