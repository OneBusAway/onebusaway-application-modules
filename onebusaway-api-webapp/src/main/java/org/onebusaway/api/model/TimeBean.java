package org.onebusaway.api.model;

import java.io.Serializable;
import java.util.Date;

public final class TimeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time;

  private String readableTime;

  public TimeBean(Date date, String readableTime) {
    this.time = date.getTime();
    this.readableTime = readableTime;
  }

  public long getTime() {
    return time;
  }

  public String getReadableTime() {
    return readableTime;
  }
}
