package org.onebusaway.transit_data.model;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private String status;

  private long serviceDate;

  private CoordinatePoint position;

  private int scheduleDeviation;

  private boolean predicted = false;
  
  private String vehicleId;
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

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

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }  
}
