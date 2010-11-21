package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public final class TripDetailsBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String tripId;

  private long serviceDate;

  private TripBean trip;

  private TripStopTimesBean schedule;

  private TripStatusBean status;

  private List<SituationBean> situations;

  public TripDetailsBean() {

  }

  public TripDetailsBean(String tripId, long serviceDate, TripBean trip,
      TripStopTimesBean schedule, TripStatusBean status, List<SituationBean> situations) {
    this.tripId = tripId;
    this.serviceDate = serviceDate;
    this.trip = trip;
    this.schedule = schedule;
    this.status = status;
    this.situations = situations;
  }

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

  public List<SituationBean> getSituations() {
    return situations;
  }

  public void setSituations(List<SituationBean> situations) {
    this.situations = situations;
  }
}
