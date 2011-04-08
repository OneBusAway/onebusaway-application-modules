package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class TimeIntervalBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long from = 0;

  private long to = 0;

  public TimeIntervalBean() {

  }

  public TimeIntervalBean(long from, long to) {
    this.from = from;
    this.to = to;
  }

  public long getFrom() {
    return from;
  }

  public void setFrom(long from) {
    this.from = from;
  }

  public long getTo() {
    return to;
  }

  public void setTo(long to) {
    this.to = to;
  }
}
