/**
 * Copyright (C) 2011 Google, Inc.
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

class TripAndStopCallRef implements Serializable {

  private static final long serialVersionUID = 1L;

  private final AgencyAndId _tripId;

  private final AgencyAndId _stopId;

  public TripAndStopCallRef(AgencyAndId tripId, AgencyAndId stopId) {
    _tripId = tripId;
    _stopId = stopId;
  }

  @Override
  public String toString() {
    return "(tripId=" + _tripId + ", stopId=" + _stopId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_tripId == null) ? 0 : _tripId.hashCode());
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
    TripAndStopCallRef other = (TripAndStopCallRef) obj;
    if (_tripId == null) {
      if (other._tripId != null)
        return false;
    } else if (!_tripId.equals(other._tripId))
      return false;
    if (_stopId == null) {
      if (other._stopId != null)
        return false;
    } else if (!_stopId.equals(other._stopId))
      return false;
    return true;
  }
}
