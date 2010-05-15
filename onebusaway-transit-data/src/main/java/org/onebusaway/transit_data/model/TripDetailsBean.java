package org.onebusaway.transit_data.model;

import java.io.Serializable;

public final class TripDetailsBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String tripId;

  private TripBean trip;

  private TripStopTimesBean schedule;

  private TripStatusBean status;

  public TripDetailsBean() {

  }

  public TripDetailsBean(String tripId, TripBean trip, TripStopTimesBean schedule) {
    this.tripId = tripId;
    this.trip = trip;
    this.schedule = schedule;
  }

  public TripDetailsBean(String tripId, TripBean trip, TripStopTimesBean schedule,
      TripStatusBean status) {
    this(tripId, trip, schedule);
    this.status = status;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public TripStopTimesBean getSchedule() {
    return schedule;
  }

  public void setSchedule(TripStopTimesBean schedule) {
    this.schedule = schedule;
  }

  public TripStatusBean getStatus() {
    return status;
  }

  public void setStatus(TripStatusBean status) {
    this.status = status;
  }
}
