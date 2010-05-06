package org.onebusaway.transit_data.services;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByAgencyIdsMethod;
import org.onebusaway.federations.annotations.FederatedByAggregateMethod;
import org.onebusaway.federations.annotations.FederatedByBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinateBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByLocationMethod;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TransitDataService extends FederatedService {

  @FederatedByAggregateMethod
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException;

  @FederatedByAgencyIdMethod
  public TripDetailsBean getTripDetails(String tripId) throws ServiceException;

  @FederatedByAgencyIdMethod
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, Date time) throws ServiceException;

  /**
   * 
   * @param stopIds
   * @param time
   * @return
   * @throws ServiceException
   */
  // Note that this used to be java.lang.Iterable, but that confused Hessian
  @FederatedByAgencyIdsMethod
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, Date time) throws ServiceException;

  @FederatedByAgencyIdMethod
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;

  @FederatedByBoundsMethod
  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2, int maxCount) throws ServiceException;

  @FederatedByBoundsMethod
  public StopsBean getStopsByBoundsAndQuery(double lat1, double lon1,
      double lat2, double lon2, String query, int maxCount)
      throws ServiceException;

  @FederatedByAgencyIdMethod
  public StopBean getStop(String stopId) throws ServiceException;

  @FederatedByCoordinateBoundsMethod(propertyExpression="bounds")
  public RoutesBean getRoutes(RoutesQueryBean query) throws ServiceException;

  @FederatedByAgencyIdMethod
  public RouteBean getRouteForId(String routeId) throws ServiceException;

  @FederatedByAgencyIdMethod
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException;
  
  @FederatedByCoordinateBoundsMethod
  public ListBean<TripStatusBean> getTripsForBounds(CoordinateBounds bounds, long time);

  @FederatedByBoundsMethod
  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException;

  @FederatedByLocationMethod
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException;

  @FederatedByAgencyIdMethod
  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults);
  
  
}
