package org.onebusaway.users.client.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class RouteFilterBean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private Set<String> routeIds = new HashSet<String>();
  
  public RouteFilterBean() {
    
  }

  public RouteFilterBean(Set<String> routeIds) {
    this.routeIds = routeIds;
  }

  public Set<String> getRouteIds() {
    return routeIds;
  }

  public void setRouteIds(Set<String> routeIds) {
    this.routeIds = routeIds;
  }
}
