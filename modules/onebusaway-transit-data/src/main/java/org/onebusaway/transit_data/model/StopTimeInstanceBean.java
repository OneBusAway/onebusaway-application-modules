package org.onebusaway.transit_data.model;

import java.util.Date;

public class StopTimeInstanceBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private int arrivalTime;

  private int departureTime;

  private Date arrivalDate;

  private Date departureDate;

  private String serviceId;

  private String tripId;

  private String stopHeadsign;

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

  public Date getArrivalDate() {
    return arrivalDate;
  }

  public void setArrivalDate(Date arrivalTime) {
    this.arrivalDate = arrivalTime;
  }

  public Date getDepartureDate() {
    return departureDate;
  }

  public void setDepartureDate(Date departureTime) {
    this.departureDate = departureTime;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String scheduleId) {
    this.serviceId = scheduleId;
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
