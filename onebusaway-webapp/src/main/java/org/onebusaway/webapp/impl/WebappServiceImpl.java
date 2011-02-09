package org.onebusaway.webapp.impl;

import java.util.Date;
import java.util.List;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
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
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
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
    StopBean stop = _transitDataService.getStop(stopId);
    if (stop == null)
      throw new NoSuchStopServiceException(stopId);
    return stop;
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
  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException {
    return _transitDataService.getTripsBetween(latFrom, lonFrom, latTo, lonTo,
        constraints);
  }

  @Override
  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {
    return _oneBusAwayService.getMinTravelTimeToStopsFrom(lat, lon,
        constraints, timeSegmentSize);
  }

  @Override
  public List<TimedPlaceBean> getLocalPathsToStops(
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {

    return _oneBusAwayService.getLocalPaths(constraints, travelTimes,
        localResults);
  }
}
