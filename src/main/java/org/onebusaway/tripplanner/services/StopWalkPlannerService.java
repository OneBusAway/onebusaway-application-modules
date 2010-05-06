package org.onebusaway.tripplanner.services;

import org.onebusaway.common.model.Place;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.tripplanner.model.WalkPlan;

public interface StopWalkPlannerService {

  public WalkPlan getWalkPlanForStopToStop(Stop from, Stop to) throws NoPathException;

  public WalkPlan getWalkPlanForStopProxyToStopProxy(StopProxy from, StopProxy to) throws NoPathException;

  public WalkPlan getWalkPlanForStopToPlace(Stop from, Place to) throws NoPathException;

  public WalkPlan getWalkPlanForStopProxyToPlace(StopProxy from, Place to) throws NoPathException;
}
