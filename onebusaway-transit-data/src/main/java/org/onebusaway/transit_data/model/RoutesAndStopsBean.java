package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class RoutesAndStopsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private RoutesBean routes;

  private StopsBean stops;

  public RoutesAndStopsBean() {

  }

  public RoutesAndStopsBean(RoutesBean routes, StopsBean stops) {
    this.routes = routes;
    this.stops = stops;
  }

  public RoutesBean getRoutes() {
    return routes;
  }

  public void setRoutes(RoutesBean routes) {
    this.routes = routes;
  }

  public StopsBean getStops() {
    return stops;
  }

  public void setStops(StopsBean stops) {
    this.stops = stops;
  }
}
