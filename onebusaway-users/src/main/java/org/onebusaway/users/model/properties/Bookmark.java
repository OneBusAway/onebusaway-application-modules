package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Bookmark implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private final int id;

  private final String name;

  private final List<String> stopIds;

  private final RouteFilter routeFilter;

  public Bookmark(int id, String name, List<String> stopIds, RouteFilter routeFilter) {
    this.id = id;
    this.name = name;
    this.stopIds = new ArrayList<String>(stopIds);
    this.routeFilter = routeFilter;
  }
  
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public RouteFilter getRouteFilter() {
    return routeFilter;
  }
}
