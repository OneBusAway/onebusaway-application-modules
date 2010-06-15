package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public final class TripStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private CoordinatePoint position;

  private boolean predicted = false;

  private int scheduleDeviation;
  
  private String vehicleId;

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

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
}
