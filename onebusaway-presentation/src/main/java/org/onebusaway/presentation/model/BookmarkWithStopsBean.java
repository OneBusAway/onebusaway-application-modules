package org.onebusaway.presentation.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.users.client.model.RouteFilterBean;

public final class BookmarkWithStopsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String name;

  private List<StopBean> stops;

  private RouteFilterBean routeFilter;

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

  public RouteFilterBean getRouteFilter() {
    return routeFilter;
  }

  public void setRouteFilter(RouteFilterBean routeFilter) {
    this.routeFilter = routeFilter;
  }
}
