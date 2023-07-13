/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;
import java.util.Objects;

/**
 * index into Canonical shape.
 */
public class RouteShapeDirectionKey implements Serializable {

  private AgencyAndId routeId;
  private String directionId;
  private String type;

  public RouteShapeDirectionKey(AgencyAndId routeId, String directionId, String type) {
    this.routeId = routeId;
    this.directionId = directionId;
    this.type = type;
  }
  public RouteShapeDirectionKey() {

  }

  public AgencyAndId getRouteId() {
    return routeId;
  }

  public void setRouteId(AgencyAndId routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteShapeDirectionKey))
      return false;

    return Objects.equals(this.routeId, ((RouteShapeDirectionKey) obj).routeId)
            && Objects.equals(this.directionId, ((RouteShapeDirectionKey) obj).directionId)
            && Objects.equals(this.type, ((RouteShapeDirectionKey) obj).type);
  }

  @Override
  public int hashCode() {
    int code = 43;
    if (routeId != null)
      code = code + routeId.hashCode();
    if (directionId != null)
      code = code + directionId.hashCode();
    if (type != null)
      code = code + type.hashCode();
    return code;
  }
}
