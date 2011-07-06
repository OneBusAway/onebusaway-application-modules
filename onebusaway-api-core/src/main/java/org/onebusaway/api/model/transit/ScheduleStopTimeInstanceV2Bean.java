package org.onebusaway.api.model.transit;

import java.io.Serializable;

public class ScheduleStopTimeInstanceV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean arrivalEnabled;

  private long arrivalTime;

  private boolean departureEnabled;

  private long departureTime;

  private String serviceId;

  private String tripId;

  private String stopHeadsign;

  public boolean isArrivalEnabled() {
    return arrivalEnabled;
  }

  public void setArrivalEnabled(boolean arrivalEnabled) {
    this.arrivalEnabled = arrivalEnabled;
  }

  public long getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(long arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public boolean isDepartureEnabled() {
    return departureEnabled;
  }

  public void setDepartureEnabled(boolean departureEnabled) {
    this.departureEnabled = departureEnabled;
  }

  public long getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(long departureTime) {
    this.departureTime = departureTime;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public void setStopHeadsign(String stopHeadsign) {
    this.stopHeadsign = stopHeadsign;
  }
}
