package org.onebusaway.api.model.transit;

import java.io.Serializable;

public class ScheduleFrequencyInstanceV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private long startTime;

  private long endTime;

  private int headway;

  private String serviceId;

  private String tripId;

  private String stopHeadsign;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public int getHeadway() {
    return headway;
  }

  public void setHeadway(int headway) {
    this.headway = headway;
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
