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

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

class RouteAndStopCallRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId _routeId;

  private final AgencyAndId _stopId;

  public RouteAndStopCallRef(AgencyAndId routeId, AgencyAndId stopId) {
    _routeId = routeId;
    _stopId = stopId;
  }

  @Override
  public String toString() {
    return "(routeId=" + _routeId + ", stopId=" + _stopId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_routeId == null) ? 0 : _routeId.hashCode());
    result = prime * result + ((_stopId == null) ? 0 : _stopId.hashCode());
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
    RouteAndStopCallRef other = (RouteAndStopCallRef) obj;
    if (_routeId == null) {
      if (other._routeId != null)
        return false;
    } else if (!_routeId.equals(other._routeId))
      return false;
    if (_stopId == null) {
      if (other._stopId != null)
        return false;
    } else if (!_stopId.equals(other._stopId))
      return false;
    return true;
  }
}
