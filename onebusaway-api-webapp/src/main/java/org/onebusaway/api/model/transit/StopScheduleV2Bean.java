package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.List;

public class StopScheduleV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long date;

  private StopV2Bean stop;

  private List<StopRouteScheduleV2Bean> stopRouteSchedules;

  private String timeZone;

  private List<StopCalendarDayV2Bean> stopCalendarDays;

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public StopV2Bean getStop() {
    return stop;
  }

  public void setStop(StopV2Bean stop) {
    this.stop = stop;
  }

  public List<StopRouteScheduleV2Bean> getStopRouteSchedules() {
    return stopRouteSchedules;
  }

  public void setStopRouteSchedules(List<StopRouteScheduleV2Bean> stopRouteSchedules) {
    this.stopRouteSchedules = stopRouteSchedules;
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
