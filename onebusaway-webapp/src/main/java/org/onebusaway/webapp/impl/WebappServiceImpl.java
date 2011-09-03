/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.impl;

import java.util.Date;
import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesAndStopsBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappService;
import org.onebusaway.webapp.services.oba.OneBusAwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class WebappServiceImpl implements WebappService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private CurrentUserService _currentUserService;

  @Autowired
  private OneBusAwayService _oneBusAwayService;

  @Autowired
  private DefaultSearchLocationService _defaultSearchLocationService;

  /***************************************************************************
   * {@link WebappService} Interface
   **************************************************************************/

  @Override
  public UserBean getCurrentUser() {
    UserBean user = _currentUserService.getCurrentUser();
    if (user == null)
      user = _currentUserService.getAnonymousUser();
    return user;
  }

  @Override
  public UserBean setDefaultLocationForUser(String locationName, double lat,
      double lon) {
    _defaultSearchLocationService.setDefaultLocationForCurrentUser(
        locationName, lat, lon);
    return getCurrentUser();
  }

  @Override
  public UserBean clearDefaultLocationForUser() {
    _defaultSearchLocationService.clearDefaultLocationForCurrentUser();
    return getCurrentUser();
  }

  @Override
  public List<AgencyWithCoverageBean> getAgencies() throws ServiceException {
    return _transitDataService.getAgenciesWithCoverage();
  }

  @Override
  public RoutesAndStopsBean getRoutesAndStops(SearchQueryBean query)
      throws ServiceException {
    RoutesBean routes = _transitDataService.getRoutes(query);
    StopsBean stops = _transitDataService.getStops(query);
    return new RoutesAndStopsBean(routes, stops);
  }

  @Override
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException {
    RoutesBean result = _transitDataService.getRoutes(query);
    return result;
  }

  @Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    return _transitDataService.getRouteForId(routeId);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException {
    return _transitDataService.getStopsForRoute(routeId);
  }

  @Override
  public StopsBean getStops(SearchQueryBean query) throws ServiceException {
    return _transitDataService.getStops(query);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    return _transitDataService.getStop(stopId);
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getArrivalsByStopId(String stopId)
      throws ServiceException {

    return _transitDataService.getStopWithArrivalsAndDepartures(stopId,
        new ArrivalsAndDeparturesQueryBean());
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    return _transitDataService.getScheduleForStop(stopId, date);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    return _transitDataService.getTripsForBounds(query);
  }

  @Override
  public ItinerariesBean getTripsBetween(CoordinatePoint from,
      CoordinatePoint to, long time, ConstraintsBean constraints)
      throws ServiceException {
    TransitLocationBean fromLoc = new TransitLocationBean();
    fromLoc.setLat(from.getLat());
    fromLoc.setLon(from.getLon());
    TransitLocationBean toLoc = new TransitLocationBean();
    toLoc.setLat(to.getLat());
    toLoc.setLon(to.getLon());
    return _transitDataService.getItinerariesBetween(fromLoc, toLoc, time,
        constraints);
  }

  @Override
  public MinTransitTimeResult getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {
    return _oneBusAwayService.getMinTravelTimeToStopsFrom(location, time,
        constraints, timeSegmentSize);
  }

  @Override
  public List<TimedPlaceBean> getLocalPathsToStops(ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {

    return _oneBusAwayService.getLocalPaths(constraints, travelTimes,
        localResults);
  }
}
