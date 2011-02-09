package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public final class VehicleStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String vehicleId;

  private String phase;

  private String status;

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
