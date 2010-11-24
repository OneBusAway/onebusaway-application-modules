package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

public class TimeRange implements Serializable {

  private static final long serialVersionUID = 1L;

  private long from;

  private long to;

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
