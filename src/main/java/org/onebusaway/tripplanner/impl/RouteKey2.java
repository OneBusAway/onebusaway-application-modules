package org.onebusaway.tripplanner.impl;

import java.util.ArrayList;
import java.util.List;

public class RouteKey2 {

  private final List<String> _routes;

  public RouteKey2() {
    _routes = new ArrayList<String>();
  }

  public RouteKey2(String routeId) {
    _routes = new ArrayList<String>(1);
    _routes.add(routeId);
  }

  private RouteKey2(List<String> routeIds, String next) {
    _routes = new ArrayList<String>(routeIds.size() + 1);
    _routes.addAll(routeIds);
    _routes.add(next);
  }

  public RouteKey2 extend(String routeId) {
    if (_routes.size() > 0 && _routes.get(_routes.size() - 1).equals(routeId))
      return this;
    return new RouteKey2(_routes, routeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteKey2))
      return false;
    RouteKey2 key = (RouteKey2) obj;
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
