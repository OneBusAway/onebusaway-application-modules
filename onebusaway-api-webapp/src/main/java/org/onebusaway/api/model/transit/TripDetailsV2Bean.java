package org.onebusaway.api.model.transit;

import java.io.Serializable;

public final class TripDetailsV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;
  
  private long serviceDate;

  private TripStatusV2Bean status;

  private TripStopTimesV2Bean schedule;

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public TripStatusV2Bean getStatus() {
    return status;
  }

  public void setStatus(TripStatusV2Bean status) {
    this.status = status;
  }

  public TripStopTimesV2Bean getSchedule() {
    return schedule;
  }

  public void setSchedule(TripStopTimesV2Bean schedule) {
    this.schedule = schedule;
  }
}
