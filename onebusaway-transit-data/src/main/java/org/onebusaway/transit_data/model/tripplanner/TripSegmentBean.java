package org.onebusaway.transit_data.model.tripplanner;

import java.io.Serializable;

public class TripSegmentBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time;

  public TripSegmentBean() {

  }

  public TripSegmentBean(long time) {
    this.time = time;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
