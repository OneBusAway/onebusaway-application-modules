package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public final class TripStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private CoordinatePoint position;

  private boolean predicted = false;

  private Double scheduleDeviation;

  private Double distanceAlongTrip;

  private String vehicleId;

  private String closestStop;

  private int closestStopTimeOffset;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public Double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(Double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(String closestStop) {
    this.closestStop = closestStop;
  }

  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }
}
