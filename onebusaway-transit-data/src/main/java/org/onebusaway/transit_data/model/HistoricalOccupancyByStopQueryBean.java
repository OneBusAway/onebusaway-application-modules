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
package org.onebusaway.transit_data.model;

import org.onebusaway.util.SystemTime;

import java.io.Serializable;
import java.util.Date;


@QueryBean
public final class HistoricalOccupancyByStopQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  private String agencyId;

  private String tripId;

  private String routeId;

  private long serviceDate;

  private long time;

  private Date date = new Date(SystemTime.currentTimeMillis());

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public void setTripId(String tripId) { this.tripId = tripId; }

  public String getTripId() {return tripId; }

  public void setRouteId(String routeId) { this.routeId = routeId; }

  public String getRouteId() { return routeId; }

  public void setServiceDate(long serviceDate){ this.serviceDate = serviceDate; }

  public long getServiceDate() { return serviceDate;}

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

}