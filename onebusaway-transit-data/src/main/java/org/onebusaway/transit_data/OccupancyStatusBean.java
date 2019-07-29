/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
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

package org.onebusaway.transit_data;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.OccupancyStatus;

import java.io.Serializable;

public class OccupancyStatusBean implements Serializable {

  private OccupancyStatus _occStatus;

  private String _status;

  private AgencyAndId _stopId;

  private AgencyAndId _routeId;

  private AgencyAndId _tripId;

  public void setOccpancyStatus(OccupancyStatus status) { _occStatus = status; _status = status.toString(); }

  public OccupancyStatus getOccupancyStatus() { return _occStatus; }

  public void setStatus(String status) {_status = status; }

  public void setStatus(OccupancyStatus status) { if (status != null) _status = status.toString(); }

  public String getStaus() {return _status; }

  public void setStopId(AgencyAndId stopId) {
    _stopId = stopId;
  }

  public void setRouteId(AgencyAndId routeId) {
    _routeId = routeId;
  }

  public void setTripId(AgencyAndId tripId) {
    _tripId = tripId;
  }

  public AgencyAndId getStopId() {
    return _stopId;
  }

  public AgencyAndId getRouteId() {
    return _routeId;
  }

  public AgencyAndId getTripId() {
    return _tripId;
  }
}
