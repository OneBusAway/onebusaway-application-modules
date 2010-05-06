/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.impl;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class WebappServiceServletImpl extends RemoteServiceServlet implements
    WebappService {

  private static final long serialVersionUID = 1L;

  private WebappService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setService(WebappService service) {
    _service = service;
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getArrivalsByStopId(String stopId)
      throws ServiceException {
    return _service.getArrivalsByStopId(stopId);
  }

  @Override
  public UserBean getCurrentUser() {
    return _service.getCurrentUser();
  }

  @Override
  public List<TimedPlaceBean> getLocalPathsToStops(String resultId,
      List<LocalSearchResult> localResults) throws ServiceException {
    return _service.getLocalPathsToStops(resultId, localResults);
  }

  @Override
  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {
    return _service.getMinTravelTimeToStopsFrom(lat, lon, constraints,
        timeSegmentSize);
  }

  @Override
  public RouteBean getRouteForId(String routeId) {
    return _service.getRouteForId(routeId);
  }

  @Override
  public RoutesBean getRoutes(RoutesQueryBean query) throws ServiceException {
    return _service.getRoutes(query);
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    return _service.getScheduleForStop(stopId, date);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    return _service.getStop(stopId);
  }

  @Override
  public StopsBean getStopsByBounds(CoordinateBounds bounds, int maxCount)
      throws ServiceException {
    return _service.getStopsByBounds(bounds, maxCount);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException {
    return _service.getStopsForRoute(routeId);
  }

  @Override
  public TripDetailsBean getTripStatus(String tripId) throws ServiceException {
    return _service.getTripStatus(tripId);
  }
  
  @Override
  public ListBean<TripStatusBean> getTripsForBounds(CoordinateBounds bounds,
      long time) {
    return _service.getTripsForBounds(bounds,time);
  }

  @Override
  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException {
    return _service.getTripsBetween(latFrom, lonFrom, latTo, lonTo, constraints);
  }

  @Override
  public void setDefaultLocationForUser(String locationName, double lat,
      double lon) {
    _service.setDefaultLocationForUser(locationName, lat, lon);
  }

  @Override
  public void clearCurrentMinTravelTimeResults() {
    _service.clearCurrentMinTravelTimeResults();
  }


}
