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

import java.io.Serializable;

public class HistoricalRidershipBean implements Serializable {

  private AgencyAndId _stopId;
  private double _loadFactor;
  private AgencyAndId _routeId;
  private AgencyAndId _tripId;

  public HistoricalRidershipBean() {

  }

  public void setStopId(AgencyAndId stopId) {
    _stopId = stopId;
  }

  public void setLoadFactor(double loadFactor) {
    _loadFactor = loadFactor;
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

  public double getLoadFactor() {
    return _loadFactor;
  }

  public AgencyAndId getRouteId() {
    return _routeId;
  }

  public AgencyAndId getTripId() {
    return _tripId;
  }
}
