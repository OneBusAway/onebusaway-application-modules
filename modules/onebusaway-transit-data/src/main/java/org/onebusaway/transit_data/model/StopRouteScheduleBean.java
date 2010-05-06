package org.onebusaway.transit_data.model;

import java.util.ArrayList;
import java.util.List;

public class StopRouteScheduleBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private RouteBean route;

  private List<StopTimeInstanceBean> stopTimes = new ArrayList<StopTimeInstanceBean>();

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }

  public List<StopTimeInstanceBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<StopTimeInstanceBean> stopTimes) {
    this.stopTimes = stopTimes;
  }
}
