package org.onebusaway.users.model.properties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class RouteFilter implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Set<String> routeIds = new HashSet<String>();

  public Set<String> getRouteIds() {
    return routeIds;
  }
}
