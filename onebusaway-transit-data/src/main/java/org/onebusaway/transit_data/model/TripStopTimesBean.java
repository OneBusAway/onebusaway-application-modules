package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TripStopTimesBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<TripStopTimeBean> stopTimes = new ArrayList<TripStopTimeBean>();

  private TripBean previousTrip;

  private TripBean nextTrip;

  private String timeZone;

  public List<TripStopTimeBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<TripStopTimeBean> stopTimes) {
    this.stopTimes = stopTimes;
  }
  
  public void addStopTime(TripStopTimeBean stopTime) {
    stopTimes.add(stopTime);
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

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }
}
