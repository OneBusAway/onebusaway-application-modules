package org.onebusaway.webapp.impl;

import org.onebusaway.exceptions.NoSuchStopServiceException;
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
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.UserDataService;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappService;
import org.onebusaway.webapp.services.oba.OneBusAwayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
class WebappServiceImpl implements WebappService {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private UserDataService _userService;

  @Autowired
  private OneBusAwayService _oneBusAwayService;

  /***************************************************************************
   * {@link WebappService} Interface
   **************************************************************************/

  @Override
  public UserBean getCurrentUser() {
    return _userService.getCurrentUserAsBean();
  }

  @Override
  public void setDefaultLocationForUser(String locationName, double lat,
      double lon) {
    _userService.setDefaultLocationForCurrentUser(locationName, lat, lon);
  }

  @Override
  public RoutesBean getRoutes(RoutesQueryBean query) throws ServiceException {
    return _transitDataService.getRoutes(query);
  }

  @Override
  public RouteBean getRouteForId(String routeId) {
    return _transitDataService.getRouteForId(routeId);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException {
    return _transitDataService.getStopsForRoute(routeId);
  }

  @Override
  public StopsBean getStopsByBounds(CoordinateBounds bounds, int maxCount)
      throws ServiceException {
    return _transitDataService.getStopsByBounds(bounds.getMinLat(),
        bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon(), maxCount);
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
        new Date());
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    return _transitDataService.getScheduleForStop(stopId, date);
  }

  @Override
  public TripDetailsBean getTripStatus(String tripId) throws ServiceException {
    return _transitDataService.getTripDetails(tripId);
  }

  @Override
  public ListBean<TripStatusBean> getTripsForBounds(CoordinateBounds bounds,
      long time) {
    return _transitDataService.getTripsForBounds(bounds, time);
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
  public List<TimedPlaceBean> getLocalPathsToStops(String resultId,
      List<LocalSearchResult> localResults) throws ServiceException {
    return _oneBusAwayService.getLocalPaths(resultId, localResults);
  }

  @Override
  public void clearCurrentMinTravelTimeResults() {
    _oneBusAwayService.clearCurrentResult();
  }
}
