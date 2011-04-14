package org.onebusaway.transit_data.model;

import java.io.Serializable;

@QueryBean
public final class RegisterAlarmQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean onArrival = false;

  private int alarmTimeOffset;

  private String url;

  public boolean isOnArrival() {
    return onArrival;
  }

  public void setOnArrival(boolean onArrival) {
    this.onArrival = onArrival;
  }

  public int getAlarmTimeOffset() {
    return alarmTimeOffset;
  }

  public void setAlarmTimeOffset(int alarmTimeOffset) {
    this.alarmTimeOffset = alarmTimeOffset;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
