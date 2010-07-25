package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.users.client.model.RouteFilterBean;

/**
 * A route filter, as filtered by a set of route ids. If the set of ids is
 * empty, we consider all routes to be enabled. if the set of ids is not empty,
 * then we consider only routes with ids contained in the filter set to be
 * enabled.
 * 
 * @author bdferris
 * @see RouteFilterBean
 */
public final class RouteFilter implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Set<String> routeIds;

  public RouteFilter() {
    this(new HashSet<String>());
  }

  public RouteFilter(Set<String> routeIds) {
    this.routeIds = new HashSet<String>(routeIds);
  }

  public Set<String> getRouteIds() {
    return routeIds;
  }
}
