package org.onebusaway.transit_data.model.schedule;

import java.io.Serializable;

import org.onebusaway.transit_data.model.StopBean;

public class StopTimeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private int arrivalTime;

  private int departureTime;

  private int pickupType;

  private int dropOffType;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  public int getPickupType() {
    return pickupType;
  }

  public void setPickupType(int pickupType) {
    this.pickupType = pickupType;
  }

  public int getDropOffType() {
    return dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    this.dropOffType = dropOffType;
  }
}
