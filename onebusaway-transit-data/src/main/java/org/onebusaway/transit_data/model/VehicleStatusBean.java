package org.onebusaway.transit_data.model;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

public final class VehicleStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String vehicleId;

  private String status;

  private long lastUpdateTime;

  private CoordinatePoint location;

  private TripBean trip;

  private TripStatusBean tripStatus;

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
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
}
