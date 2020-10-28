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

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

public final class VehicleStatusBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String vehicleId;

  private String phase;

  private String status;

  private OccupancyStatus occupancyStatus;

  private Integer occupancyCount;

  private Integer occupancyCapacity;

  private long lastUpdateTime;

  private long lastLocationUpdateTime;

  private CoordinatePoint location;

  private TripBean trip;

  private TripStatusBean tripStatus;

  private List<VehicleLocationRecordBean> allRecords;

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

  public OccupancyStatus getOccupancyStatus() { return occupancyStatus; }

  public void setOccupancyStatus(OccupancyStatus occupancyStatus) { this.occupancyStatus = occupancyStatus; }

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

  public long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(long lastLocationUpdateTime) {
    this.lastLocationUpdateTime = lastLocationUpdateTime;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public TripStatusBean getTripStatus() {
    return tripStatus;
  }

  public void setTripStatus(TripStatusBean tripStatus) {
    this.tripStatus = tripStatus;
  }

  public List<VehicleLocationRecordBean> getAllRecords() {
    return allRecords;
  }

  public void setAllRecords(List<VehicleLocationRecordBean> allRecords) {
    this.allRecords = allRecords;
  }
}
