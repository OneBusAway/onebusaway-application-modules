package org.onebusaway.tripplanner.impl;

import java.util.ArrayList;
import java.util.List;

public class RouteKey {

  private final List<String> _routes;

  public RouteKey() {
    _routes = new ArrayList<String>();
  }

  public RouteKey(String routeId) {
    _routes = new ArrayList<String>(1);
    _routes.add(routeId);
  }

  private RouteKey(List<String> routeIds, String next) {
    _routes = new ArrayList<String>(routeIds.size() + 1);
    _routes.addAll(routeIds);
    _routes.add(next);
  }

  public RouteKey extend(String routeId) {
    if (_routes.size() > 0 && _routes.get(_routes.size() - 1).equals(routeId))
      return this;
    return new RouteKey(_routes, routeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteKey))
      return false;
    RouteKey key = (RouteKey) obj;
    return _routes.equals(key._routes);
  }

  @Override
  public int hashCode() {
    return _routes.hashCode();
  }

  @Override
  public String toString() {
    return _routes.toString();
  }
}
