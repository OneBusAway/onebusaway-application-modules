package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class StopAndTimeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private StopTimeInstanceBean stopTime;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public StopTimeInstanceBean getStopTime() {
    return stopTime;
  }

  public void setStopTime(StopTimeInstanceBean stopTime) {
    this.stopTime = stopTime;
  }
}
