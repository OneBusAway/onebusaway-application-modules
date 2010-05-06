package org.onebusaway.tripplanner.model;

import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;

import java.io.Serializable;
import java.util.List;

public class TripEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<StopTime> stopTimes;

  private Trip nextTrip;

  private int stopPatternId;

  public void setStopTimes(List<StopTime> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public List<StopTime> getStopTimes() {
    return this.stopTimes;
  }

  public void setNextTrip(Trip next) {
    this.nextTrip = next;
  }

  public Trip getNextTrip() {
    return this.nextTrip;
  }

  public void setStopPatternId(int stopPatternId) {
    this.stopPatternId = stopPatternId;
  }

  public int getStopPatternId() {
    return this.stopPatternId;
  }
}
