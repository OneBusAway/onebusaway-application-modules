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
package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.realtime.api.OccupancyStatus;

public final class VehicleStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String vehicleId;

  private String phase;

  private String status;

  private String occupancyStatus;

  private Integer occupancyCount;

  private Integer occupancyCapacity = -1;

  private long lastUpdateTime;

  private Long lastLocationUpdateTime;

  private CoordinatePoint location;

  private String tripId;

  private TripStatusV2Bean tripStatus;

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getOccupancyStatus() { return occupancyStatus; }

  public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
    if (occupancyStatus != null)
      this.occupancyStatus = occupancyStatus.name();
  }

  public Integer getOccupancyCount() {
    return occupancyCount;
  }

  public void setOccupancyCount(Integer occupancyCount) {
    this.occupancyCount = occupancyCount;
  }

  public Integer getOccupancyCapacity() {
    return occupancyCapacity;
  }

  public void setOccupancyCapacity(Integer occupancyCapacity) {
    this.occupancyCapacity = occupancyCapacity;
  }

  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public Long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(Long lastLocationUpdateTime) {
    this.lastLocationUpdateTime = lastLocationUpdateTime;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public TripStatusV2Bean getTripStatus() {
    return tripStatus;
  }

  public void setTripStatus(TripStatusV2Bean tripStatus) {
    this.tripStatus = tripStatus;
  }

}
