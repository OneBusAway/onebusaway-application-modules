package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.List;

public class RoutesBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<RouteBean> routes;

  private boolean limitExceeded = false;

  public List<RouteBean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<RouteBean> routes) {
    this.routes = routes;
  }

  public boolean isLimitExceeded() {
    return limitExceeded;
  }

  public void setLimitExceeded(boolean limitExceeded) {
    this.limitExceeded = limitExceeded;
  }

}
