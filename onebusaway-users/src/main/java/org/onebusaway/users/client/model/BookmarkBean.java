package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.users.model.properties.Bookmark;

/**
 * 
 * @author bdferris
 * @see Bookmark
 */
public final class BookmarkBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String name;

  private List<String> stopIds;

  private RouteFilterBean routeFilter = new RouteFilterBean();

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

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  public RouteFilterBean getRouteFilter() {
    return routeFilter;
  }

  public void setRouteFilter(RouteFilterBean routeFilter) {
    this.routeFilter = routeFilter;
  }
}
