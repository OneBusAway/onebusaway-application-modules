package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.List;

public class StopCalendarDaysBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String timeZone;

  private List<StopCalendarDayBean> days;

  public StopCalendarDaysBean() {

  }

  public StopCalendarDaysBean(String timeZone, List<StopCalendarDayBean> days) {
    this.timeZone = timeZone;
    this.days = days;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public List<StopCalendarDayBean> getDays() {
    return days;
  }

  public void setDays(List<StopCalendarDayBean> days) {
    this.days = days;
  }
}
