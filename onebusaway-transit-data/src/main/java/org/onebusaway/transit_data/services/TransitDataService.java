package org.onebusaway.transit_data.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByAggregateMethod;
import org.onebusaway.federations.annotations.FederatedByBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinateBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdsMethod;
import org.onebusaway.federations.annotations.FederatedByLocationMethod;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;

public interface TransitDataService extends FederatedService {

  @FederatedByAggregateMethod
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException;

  @FederatedByAgencyIdMethod
  public AgencyBean getAgency(String agencyId) throws ServiceException;

  @FederatedByEntityIdMethod
  public TripBean getTrip(String tripId) throws ServiceException;

  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public TripDetailsBean getSpecificTripDetails(TripDetailsQueryBean query)
      throws ServiceException;

  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query);

  @FederatedByEntityIdMethod(propertyExpression="vehicleId")
  public TripDetailsBean getTripDetailsForVehicleAndTime(
      TripForVehicleQueryBean query);

  @FederatedByEntityIdMethod
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, Date timeFrom, Date timeTo) throws ServiceException;

  // Note that this used to be java.lang.Iterable, but that confused Hessian
  @FederatedByEntityIdsMethod
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, Date timeFrom, Date timeTo)
      throws ServiceException;

  @FederatedByEntityIdMethod
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;

  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  @FederatedByEntityIdMethod
  public StopBean getStop(String stopId) throws ServiceException;

  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException;

  @FederatedByAgencyIdMethod
  public ListBean<String> getStopIdsForAgencyId(String agencyId);

  @FederatedByEntityIdMethod
  public RouteBean getRouteForId(String routeId) throws ServiceException;

  @FederatedByAgencyIdMethod
  public ListBean<String> getRouteIdsForAgencyId(String agencyId);

  @FederatedByEntityIdMethod
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException;

  @FederatedByEntityIdMethod
  public EncodedPolylineBean getShapeForId(String shapeId);

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
      List<LocalSearchResult> localResults) throws ServiceException;
}
