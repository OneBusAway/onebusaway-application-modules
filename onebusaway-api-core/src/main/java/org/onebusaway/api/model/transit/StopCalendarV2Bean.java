package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.List;

public class StopCalendarV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  private String timeZone;

  private List<StopCalendarDayV2Bean> stopCalendarDays;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public List<StopCalendarDayV2Bean> getStopCalendarDays() {
    return stopCalendarDays;
  }

  public void setStopCalendarDays(List<StopCalendarDayV2Bean> stopCalendarDays) {
    this.stopCalendarDays = stopCalendarDays;
  }
}
