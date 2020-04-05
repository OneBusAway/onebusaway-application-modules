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
package org.onebusaway.alerts.impl;

import org.onebusaway.gtfs.model.AgencyAndId;

class RouteDirectionAndStopCallRef {

  private final AgencyAndId _routeId;

  private final String _direction;

  private final AgencyAndId _stopId;

  public RouteDirectionAndStopCallRef(AgencyAndId routeId, String direction,
      AgencyAndId stopId) {
    if (routeId == null)
      throw new IllegalStateException("routeId is null");
    if (direction == null)
      throw new IllegalStateException("direction is null");
    if (stopId == null)
      throw new IllegalStateException("stopId is null");
    _routeId = routeId;
    _direction = direction;
    _stopId = stopId;
  }

  @Override
  public String toString() {
    return "(routeId=" + _routeId + " direction=" + _direction + " stopId="
        + _stopId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _direction.hashCode();
    result = prime * result + _routeId.hashCode();
    result = prime * result + _stopId.hashCode();
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
    RouteDirectionAndStopCallRef other = (RouteDirectionAndStopCallRef) obj;
    if (!_direction.equals(other._direction))
      return false;
    if (!_routeId.equals(other._routeId))
      return false;
    if (!_stopId.equals(other._stopId))
      return false;
    return true;
  }

}
