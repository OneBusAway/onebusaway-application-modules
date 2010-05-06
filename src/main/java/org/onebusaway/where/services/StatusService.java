package org.onebusaway.where.services;

import org.onebusaway.gtfs.model.Route;

import java.util.Set;

public interface StatusService {

  public static final String STATUS_DEFAULT = "default";

  public static final String STATUS_CANCELLED = "cancelled";

  public static final String STATUS_REROUTE = "reroute";

  public String getRouteStatus(Route route);

  public void setCancelledRoutes(Set<String> routeShortNames);

  public void setReroutedRoutes(Set<String> routeShortNames);

  public Set<Route> getCancelledRoutes();

  public Set<Route> getReroutedRoutes();

  public boolean isRouteCancelled(Route route);

  public boolean isRouteRerouted(Route route);

}
