package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.model.Place;
import org.onebusaway.common.spring.Cacheable;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopWalkPlannerService;
import org.onebusaway.tripplanner.services.WalkPlannerService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class StopWalkPlannerServiceImpl implements StopWalkPlannerService {

  private WalkPlannerService _walkPlannerService;

  public void setWalkPlannerService(WalkPlannerService service) {
    _walkPlannerService = service;
  }

  @Cacheable
  public WalkPlan getWalkPlanForStopToStop(Stop from, Stop to) throws NoPathException {
    CoordinatePoint latLonFrom = new CoordinatePoint(from.getLat(), from.getLon());
    CoordinatePoint latLonTo = new CoordinatePoint(to.getLat(), to.getLon());
    return _walkPlannerService.getWalkPlan(latLonFrom, from.getLocation(), latLonTo, to.getLocation());
  }

  @Cacheable
  public WalkPlan getWalkPlanForStopProxyToStopProxy(StopProxy from, StopProxy to) throws NoPathException {
    CoordinatePoint latLonFrom = new CoordinatePoint(from.getStopLat(), from.getStopLon());
    CoordinatePoint latLonTo = new CoordinatePoint(to.getStopLat(), to.getStopLon());
    return _walkPlannerService.getWalkPlan(latLonFrom, from.getStopLocation(), latLonTo, to.getStopLocation());
  }

  @Cacheable
  public WalkPlan getWalkPlanForStopToPlace(Stop from, Place to) throws NoPathException {
    CoordinatePoint latLonFrom = new CoordinatePoint(from.getLat(), from.getLon());
    CoordinatePoint latLonTo = new CoordinatePoint(to.getLat(), to.getLon());
    return _walkPlannerService.getWalkPlan(latLonFrom, from.getLocation(), latLonTo, to.getLocation());
  }

  @Cacheable
  public WalkPlan getWalkPlanForStopProxyToPlace(StopProxy from, Place to) throws NoPathException {
    CoordinatePoint latLonFrom = new CoordinatePoint(from.getStopLat(), from.getStopLon());
    CoordinatePoint latLonTo = new CoordinatePoint(to.getLat(), to.getLon());
    return _walkPlannerService.getWalkPlan(latLonFrom, from.getStopLocation(), latLonTo, to.getLocation());
  }

}
