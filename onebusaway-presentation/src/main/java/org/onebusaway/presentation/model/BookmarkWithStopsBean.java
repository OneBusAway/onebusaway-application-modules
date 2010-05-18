package org.onebusaway.presentation.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

public final class BookmarkWithStopsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String name;

  private List<StopBean> stops;

  private List<RouteBean> routes;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<RouteBean> getRoutes() {
    return routes;
  }

  public void setRoutes(List<RouteBean> routes) {
    this.routes = routes;
  }  
}
