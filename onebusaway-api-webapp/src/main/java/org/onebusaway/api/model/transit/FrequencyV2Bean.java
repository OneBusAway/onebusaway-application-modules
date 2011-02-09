package org.onebusaway.api.model.transit;

import java.io.Serializable;

public final class FrequencyV2Bean implements Serializable {

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

  public void setHeadway(int headwaySecs) {
    this.headway = headwaySecs;
  }
}
