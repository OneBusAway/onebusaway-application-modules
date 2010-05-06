package org.onebusaway.tripplanner.web.common.client.model;

import org.onebusaway.common.web.common.client.model.StopBean;

public class DepartureSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private String routeName;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }
}
