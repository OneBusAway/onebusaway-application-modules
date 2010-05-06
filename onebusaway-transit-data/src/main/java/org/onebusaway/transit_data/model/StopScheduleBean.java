package org.onebusaway.transit_data.model;

import java.util.Date;
import java.util.List;

public class StopScheduleBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private Date date;

  private StopBean stop;

  private List<StopRouteScheduleBean> routes;

  private StopCalendarDaysBean calendarDays;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public List<StopRouteScheduleBean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<StopRouteScheduleBean> routes) {
    this.routes = routes;
  }

  public StopCalendarDaysBean getCalendarDays() {
    return calendarDays;
  }

  public void setCalendarDays(StopCalendarDaysBean calendarDays) {
    this.calendarDays = calendarDays;
  }
}
