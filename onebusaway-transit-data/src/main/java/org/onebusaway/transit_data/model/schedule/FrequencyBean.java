package org.onebusaway.transit_data.model.schedule;

import java.io.Serializable;

public final class FrequencyBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private int headway;

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
}
