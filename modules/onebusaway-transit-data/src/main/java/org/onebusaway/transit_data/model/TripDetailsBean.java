package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TripDetailsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String status;

  private TripBean trip;

  private List<StopAndTimeBean> stopsWithTime = new ArrayList<StopAndTimeBean>();

  private TripBean previousTrip;

  private TripBean nextTrip;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public List<StopAndTimeBean> getStopsWithTime() {
    return stopsWithTime;
  }

  public void setStopsWithTime(List<StopAndTimeBean> stopsWithTime) {
    this.stopsWithTime = stopsWithTime;
  }
  

  public void addStopAndTimeBean(StopAndTimeBean satBean) {
    this.stopsWithTime.add(satBean);
  }

  public TripBean getPreviousTrip() {
    return previousTrip;
  }

  public void setPreviousTrip(TripBean previousTrip) {
    this.previousTrip = previousTrip;
  }

  public TripBean getNextTrip() {
    return nextTrip;
  }

  public void setNextTrip(TripBean nextTrip) {
    this.nextTrip = nextTrip;
  }

}
