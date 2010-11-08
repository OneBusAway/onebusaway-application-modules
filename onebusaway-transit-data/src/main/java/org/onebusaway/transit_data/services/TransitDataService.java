package org.onebusaway.transit_data.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.onebusaway.exceptions.NoSuchStopServiceException;
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
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopProblemReportBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TripProblemReportBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationExchangeDeliveryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;

/**
 * The {@link TransitDataService} is the primary interface separating
 * user-interface modules that access transit data from the data providers that
 * contain the data.
 * 
 * The service is a {@link FederatedService}, which means that multiple
 * {@link TransitDataService} instances covering transit agencies across the
 * county can be stitched into one virtual service that seamlessly passes calls
 * to the appropriate underlying instance. As such, you'll notice that all the
 * service methods here are annotated with @FederatedBy... annotations that give
 * hints how the method should be dispatched between multiple instances.
 * 
 * Note that all methods return "bean" objects, which are POJOs designed for
 * flexibility in over-the-wire serialization for RPC and are separate from the
 * underlying representations in the datastore.
 * 
 * @author bdferris
 * 
 */
public interface TransitDataService extends FederatedService {

  /**
   * The coverage area for each agency is generally the lat-lon bounds of all
   * stops served by that agency.
   * 
   * @return the list of all transit agencies in the service, along with their
   *         coverage information.
   * @throws ServiceException
   */
  @FederatedByAggregateMethod
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException;

  /**
   * @param agencyId
   * @return the agency with the specified id, or null if not found
   * @throws ServiceException
   */
  @FederatedByAgencyIdMethod
  public AgencyBean getAgency(String agencyId) throws ServiceException;

  /**
   * @param query specifies the bounds of the query and an optional route name
   *          query
   * @return return all routes matching the specified query
   * @throws ServiceException
   */
  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException;

  /**
   * 
   * @param routeId
   * @return the route with specified id, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public RouteBean getRouteForId(String routeId) throws ServiceException;

  /**
   * @param agencyId
   * @return the list of all route ids for the specified agency id
   */
  @FederatedByAgencyIdMethod
  public ListBean<String> getRouteIdsForAgencyId(String agencyId);

  /**
   * @param routeId
   * @return the stops for the specified route, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException;

  /**
   * @param tripId
   * @return the trip with the specifid id, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public TripBean getTrip(String tripId) throws ServiceException;

  /**
   * @param query details about which trip to query, and what to include in the
   *          response
   * @return trip details for trip matching the specified query, or null if not
   *         found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public TripDetailsBean getSingleTripDetails(TripDetailsQueryBean query)
      throws ServiceException;

  /**
   * @param query details about which trips to query, and what to include in the
   *          response
   * @return trip details for trips matching the specified query, or empty if
   *         not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public ListBean<TripDetailsBean> getTripDetails(TripDetailsQueryBean query)
      throws ServiceException;

  /**
   * @param query determines the time and location of the query
   * @return trips details for the trips matching the specified bounds query
   */
  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query);

  /**
   * @param query determines the time and location of the query
   * @return trips details for the trips matching the specified bounds query
   */
  @FederatedByEntityIdMethod(propertyExpression = "routeId")
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query);

  /**
   * @param query determines the time and location of the query
   * @return trips details for the trips matching the specified bounds query
   */
  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query);

  @FederatedByEntityIdMethod
  public VehicleStatusBean getVehicleForAgency(String vehicleId, long time);

  @FederatedByAgencyIdMethod
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time);

  /**
   * 
   * @param query determines the vehicle and time of the trip query
   * @return trip details for the trip matching the specified query, or null if
   *         not found
   */
  @FederatedByEntityIdMethod(propertyExpression = "vehicleId")
  public TripDetailsBean getTripDetailsForVehicleAndTime(
      TripForVehicleQueryBean query);

  /**
   * @param stopId
   * @param timeFrom
   * @param timeTo
   * @return stop with arrival and departure information for the specified stop
   *         and time range, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException;

  /**
   * @param stopIds
   * @param timeFrom
   * @param timeTo
   * @return stops with arrival and departure information for the specified
   *         stops and time range
   * @throws ServiceException
   * @throws NoSuchStopServiceException if one of the specified stops could not
   *           be found
   */
  @FederatedByEntityIdsMethod
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException;

  /**
   * @param stopId
   * @param date
   * @return retrieve the full schedule for the stop on the specified date
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;

  /**
   * 
   * @param query determines the query bounds, along with an optional stop code
   * @return find all stops within the specified bounds query
   * @throws ServiceException
   */
  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  /**
   * @param stopId
   * @return the stop with the specified id, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopBean getStop(String stopId) throws ServiceException;

  /**
   * @param agencyId
   * @return the list of all stops operated by the specified agency
   */
  @FederatedByAgencyIdMethod
  public ListBean<String> getStopIdsForAgencyId(String agencyId);

  /**
   * 
   * @param shapeId
   * @return an encoded polyline of the shape with the specified id, or null if
   *         not found
   */
  @FederatedByEntityIdMethod
  public EncodedPolylineBean getShapeForId(String shapeId);

  /**
   * 
   * @param latFrom
   * @param lonFrom
   * @param latTo
   * @param lonTo
   * @param constraints
   * @return a list of trip plans computed between the two locations with the
   *         specified constraints
   * @throws ServiceException
   */
  @FederatedByBoundsMethod
  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException;

  /**
   * 
   * @param lat
   * @param lon
   * @param constraints
   * @return min travel time transit-shed computation to a list of stops from
   *         the specified starting location with the specified travel
   *         constraints
   * @throws ServiceException
   */
  @FederatedByLocationMethod
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException;

  /**
   * 
   * @param agencyId
   * @param constraints
   * @param minTravelTimeToStops
   * @param localResults
   * @return finish off the transit-shed computation by computing last-mile
   *         walking paths to target locations from min-travel-time-to-stops
   *         results
   * @throws ServiceException
   */
  @FederatedByAgencyIdMethod
  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) throws ServiceException;

  /**
   * @return **
   * 
   ****/

  @FederatedByAgencyIdMethod
  public SituationBean createServiceAlert(String agencyId, SituationBean situation);

  @FederatedByEntityIdMethod(propertyExpression = "id")
  public void updateServiceAlert(SituationBean situation);

  @FederatedByAgencyIdMethod
  public void updateServiceAlerts(String agencyId,
      SituationExchangeDeliveryBean alerts);

  @FederatedByEntityIdMethod
  public SituationBean getServiceAlertForId(String situationId);

  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public ListBean<SituationBean> getServiceAlerts(SituationQueryBean query);

  /****
   * These methods are going to pile up. How do we handle this gracefully in the
   * future?
   ****/

  /**
   * Report a problem with a particular stop.
   * 
   * @param problem the problem summary bean
   */
  @FederatedByEntityIdMethod(propertyExpression = "stopId")
  public void reportProblemWithStop(StopProblemReportBean problem);

  /**
   * Report a problem with a particular trip.
   * 
   * @param problem the problem summary bean
   */
  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public void reportProblemWithTrip(TripProblemReportBean problem);

  @FederatedByEntityIdMethod()
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      String tripId);

  @FederatedByEntityIdMethod()
  public TripProblemReportBean getTripProblemReportForTripIdAndId(
      String tripId, long id);

  @FederatedByEntityIdMethod()
  public void deleteTripProblemReportForTripIdAndId(String tripId, long id);
}
