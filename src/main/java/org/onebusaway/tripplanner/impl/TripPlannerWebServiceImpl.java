package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.services.TripPlannerBeanFactory;
import org.onebusaway.tripplanner.services.TripPlannerService;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

public class TripPlannerWebServiceImpl implements TripPlannerWebService {

  @Autowired
  private TripPlannerService _tripPlanner;

  @Autowired
  private TripPlannerBeanFactory _beanFactory;

  public List<TripBean> getTripsBetween(double latFrom, double lonFrom, double latTo, double lonTo,
      TripPlannerConstraintsBean constraints) throws ServiceException {

    TripPlannerConstraints c = new TripPlannerConstraints();

    if (!constraints.hasMinDepartureTime())
      throw new IllegalArgumentException();

    c.setMinDepartureTime(constraints.getMinDepartureTime());

    if (constraints.hasMaxTransfers())
      c.setMaxTransferCount(constraints.getMaxTransfers());

    if (constraints.hasMaxWalkingDistance())
      c.setMaxSingleWalkDistance(constraints.getMaxWalkingDistance());

    c.setMaxTrips(3);
    c.setMaxTripDurationRatio(1.5);

    // c.setMaxComputationTime(2000);

    CoordinatePoint from = new CoordinatePoint(latFrom, lonFrom);
    CoordinatePoint to = new CoordinatePoint(latTo, lonTo);

    long t = System.currentTimeMillis();
    System.out.println("TripPlannerWebServiceImpl - A=" + (System.currentTimeMillis() - t));
    Collection<TripPlan> plans = _tripPlanner.getTripsBetween(from, to, c);
    System.out.println("TripPlannerWebServiceImpl - B=" + (System.currentTimeMillis() - t) + " plans=" + plans.size());
    List<TripBean> beans = _beanFactory.getTripsAsBeans(plans);
    System.out.println("TripPlannerWebServiceImpl - C=" + (System.currentTimeMillis() - t) + " beans=" + beans.size());

    return beans;
  }

  /*****************************************************************************
   * 
   ****************************************************************************/
}
