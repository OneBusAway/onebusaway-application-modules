package org.onebusaway.where.impl;

import org.onebusaway.common.spring.Cacheable;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.services.StatusService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class StatusServiceImpl implements StatusService {

  @Autowired
  private GtfsDao _dao;

  private Set<Route> _cancelledRoutes = new HashSet<Route>();

  private Set<Route> _reroutedRoutes = new HashSet<Route>();

  @Cacheable
  public String getRouteStatus(Route route) {
    if (isRouteCancelled(route))
      return StatusService.STATUS_CANCELLED;
    if (isRouteRerouted(route))
      return StatusService.STATUS_REROUTE;
    return StatusService.STATUS_DEFAULT;
  }

  public void setCancelledRoutes(Set<String> routeShortNames) {
    List<Route> routes = _dao.getRoutesByShortNames(routeShortNames);
    Set<Route> routeSet = new HashSet<Route>(routes);
    synchronized (this) {
      _cancelledRoutes = routeSet;
    }
  }

  public Set<Route> getCancelledRoutes() {
    synchronized (this) {
      return new HashSet<Route>(_cancelledRoutes);
    }
  }

  @Cacheable
  public boolean isRouteCancelled(Route route) {
    synchronized (this) {
      return _cancelledRoutes.contains(route);
    }
  }

  public void setReroutedRoutes(Set<String> routeShortNames) {
    List<Route> routes = _dao.getRoutesByShortNames(routeShortNames);
    Set<Route> routeSet = new HashSet<Route>(routes);
    synchronized (this) {
      _reroutedRoutes = routeSet;
    }
  }

  public Set<Route> getReroutedRoutes() {
    synchronized (this) {
      return new HashSet<Route>(_reroutedRoutes);
    }
  }

  @Cacheable
  public boolean isRouteRerouted(Route route) {
    synchronized (this) {
      return _reroutedRoutes.contains(route);
    }
  }
}
