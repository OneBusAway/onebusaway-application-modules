package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class TimeRangeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long from;

  private long to;

  /**
   * 
   * @return the from time, or zero if not set
   */
  public long getFrom() {
    return from;
  }

  public void setFrom(long from) {
    this.from = from;
  }

  /**
   * 
   * @return the to time, or zero if not set
   */
  public long getTo() {
    return to;
  }

  public void setTo(long to) {
    this.to = to;
  }
}
