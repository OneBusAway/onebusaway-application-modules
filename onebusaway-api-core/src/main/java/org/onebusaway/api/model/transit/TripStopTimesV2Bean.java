package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TripStopTimesV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String timeZone;

  private List<TripStopTimeV2Bean> stopTimes = new ArrayList<TripStopTimeV2Bean>();

  private String previousTripId;

  private String nextTripId;

  private FrequencyV2Bean frequency;

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public List<TripStopTimeV2Bean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<TripStopTimeV2Bean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public String getPreviousTripId() {
    return previousTripId;
  }

  public void setPreviousTripId(String previousTripId) {
    this.previousTripId = previousTripId;
  }

  public String getNextTripId() {
    return nextTripId;
  }

  public void setNextTripId(String nextTripId) {
    this.nextTripId = nextTripId;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }
}
