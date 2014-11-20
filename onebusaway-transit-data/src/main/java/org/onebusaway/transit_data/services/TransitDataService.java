/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.transit_data.services;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByAggregateMethod;
import org.onebusaway.federations.annotations.FederatedByAnyEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinateBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinatePointsMethod;
import org.onebusaway.federations.annotations.FederatedByCustomMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdsMethod;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.ScheduledBlockLocationBean;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.model.introspection.InstanceDetails;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
import org.onebusaway.transit_data.model.problems.PlannedTripProblemReportBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.StopProblemReportSummaryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportQueryBean;
import org.onebusaway.transit_data.model.problems.TripProblemReportSummaryBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBeanFederatedServiceMethodInvocationHandler;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.utility.GitRepositoryState;
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
 * Implementation Note: when adding methods to this interface, do not introduce
 * multiple methods with the same name and different arguments, as this seems to
 * confuse Hessian proxies of the interface. Additionally, each method must
 * specify a @FederatedBy... annotation indicating how the method will be
 * dispatched in a federated deployment.
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
   * @param agencyId
   * @return the list of all routes for the specified agency id
   */
  @FederatedByAgencyIdMethod
  public ListBean<RouteBean> getRoutesForAgencyId(String agencyId);

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
  public BlockBean getBlockForId(String blockId);

  @FederatedByEntityIdMethod
  public BlockInstanceBean getBlockInstance(String blockId, long serviceDate);

  @FederatedByEntityIdMethod
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(
      String blockId, long serviceDate, int scheduledTime);

  /****
   * Vehicle Methods
   *****/

  @FederatedByEntityIdMethod
  public VehicleStatusBean getVehicleForAgency(String vehicleId, long time);

  @FederatedByAgencyIdMethod
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time);

  @FederatedByEntityIdMethod
  public VehicleLocationRecordBean getVehicleLocationRecordForVehicleId(
      String vehicleId, long targetTime);

  /**
   * 
   * @param query
   * @return
   */
  @FederatedByAnyEntityIdMethod(properties = {"blockId", "tripId", "vehicleId"})
  public ListBean<VehicleLocationRecordBean> getVehicleLocationRecords(
      VehicleLocationRecordQueryBean query);

  @FederatedByEntityIdMethod(propertyExpression = "vehicleId")
  public void submitVehicleLocation(VehicleLocationRecordBean record);

  @FederatedByEntityIdMethod
  public void resetVehicleLocation(String vehicleId);

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

  @FederatedByEntityIdMethod(propertyExpression = "stopId")
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      ArrivalAndDepartureForStopQueryBean query) throws ServiceException;

  @FederatedByEntityIdMethod(propertyExpression = "stopId")
  public String registerAlarmForArrivalAndDepartureAtStop(
      ArrivalAndDepartureForStopQueryBean query, RegisterAlarmQueryBean alarm);

  @FederatedByEntityIdMethod()
  public void cancelAlarmForArrivalAndDepartureAtStop(String alarmId);

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
   * @param agencyId
   * @return the list of all shape ids associated with the specified agency
   */
  @FederatedByAgencyIdMethod
  public ListBean<String> getShapeIdsForAgencyId(String agencyId);

  @FederatedByCoordinatePointsMethod(propertyExpressions = "mostRecentLocation")
  public ListBean<CurrentVehicleEstimateBean> getCurrentVehicleEstimates(
      CurrentVehicleEstimateQueryBean query);

  /**
   * Plan a trip between two locations at a particular time, with the specified
   * constraints.
   * 
   * @param from
   * @param to
   * @param targetTime
   * @param constraints
   * @return a list of trip plans computed between the two locations with the
   *         specified constraints
   * @throws ServiceException
   */
  @FederatedByCoordinatePointsMethod(arguments = {0, 1}, propertyExpressions = {
      "location", "location"})
  public ItinerariesBean getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ConstraintsBean constraints)
      throws ServiceException;

  @FederatedByCoordinatePointsMethod(arguments = {0, 1}, propertyExpressions = {
      "location", "location"})
  public void reportProblemWithPlannedTrip(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ConstraintsBean constraints,
      PlannedTripProblemReportBean report);

  @FederatedByBoundsMethod
  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo) throws ServiceException;

  /**
   * 
   * @param location
   * @param time
   * @param constraints
   * @return min travel time transit-shed computation to a list of stops from
   *         the specified starting location with the specified travel
   *         constraints
   * @throws ServiceException
   */
  @FederatedByCoordinatePointsMethod
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints) throws ServiceException;

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
      ConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) throws ServiceException;

  /****
   * Historical Data
   ****/

  /****
   * Service Alert Methods
   ****/

  @FederatedByAgencyIdMethod
  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean situation);

  @FederatedByEntityIdMethod(propertyExpression = "id")
  public void updateServiceAlert(ServiceAlertBean situation);

  @FederatedByEntityIdMethod
  public void removeServiceAlert(String situationId);

  @FederatedByEntityIdMethod
  public ServiceAlertBean getServiceAlertForId(String situationId);

  @FederatedByAgencyIdMethod()
  public ListBean<ServiceAlertBean> getAllServiceAlertsForAgencyId(
      String agencyId);

  @FederatedByAgencyIdMethod()
  public void removeAllServiceAlertsForAgencyId(String agencyId);

  @FederatedByCustomMethod(handler = SituationQueryBeanFederatedServiceMethodInvocationHandler.class)
  public ListBean<ServiceAlertBean> getServiceAlerts(SituationQueryBean query);

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

  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public ListBean<StopProblemReportSummaryBean> getStopProblemReportSummaries(
      StopProblemReportQueryBean query);

  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public ListBean<StopProblemReportBean> getStopProblemReports(
      StopProblemReportQueryBean query);

  @FederatedByEntityIdMethod()
  public List<StopProblemReportBean> getAllStopProblemReportsForStopId(
      String stopId);

  @FederatedByEntityIdMethod()
  public StopProblemReportBean getStopProblemReportForStopIdAndId(
      String stopId, long id);

  @FederatedByEntityIdMethod()
  public void deleteStopProblemReportForStopIdAndId(String stopId, long id);

  /****
   * 
   ****/

  /**
   * Report a problem with a particular trip.
   * 
   * @param problem the problem summary bean
   */
  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public void reportProblemWithTrip(TripProblemReportBean problem);

  /**
   * 
   * @param query
   * @return
   * 
   * @deprecated see
   *             {@link #getTripProblemReportSummariesByGrouping(TripProblemReportQueryBean, ETripProblemGroupBy)}
   */
  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  @Deprecated
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummaries(
      TripProblemReportQueryBean query);

  /**
   * 
   * @param query
   * @param groupBy
   * @return
   */
  @FederatedByAnyEntityIdMethod(properties = {"tripId"}, agencyIdProperties = {"agencyId"})
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummariesByGrouping(
      TripProblemReportQueryBean query, ETripProblemGroupBy groupBy);

  @FederatedByAnyEntityIdMethod(properties = {"tripId"}, agencyIdProperties = {"agencyId"})
  public ListBean<TripProblemReportBean> getTripProblemReports(
      TripProblemReportQueryBean query);

  /**
   * 
   * @param tripId
   * @return
   * 
   * @deprecated see {@link #getTripProblemReports(TripProblemReportQueryBean)},
   *             using {@link TripProblemReportQueryBean#getTripId()}.
   */
  @FederatedByEntityIdMethod()
  @Deprecated
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      String tripId);

  @FederatedByEntityIdMethod()
  public TripProblemReportBean getTripProblemReportForTripIdAndId(
      String tripId, long id);

  @FederatedByEntityIdMethod(propertyExpression = "tripId")
  public void updateTripProblemReport(TripProblemReportBean tripProblemReport);

  @FederatedByEntityIdMethod()
  public void deleteTripProblemReportForTripIdAndId(String tripId, long id);

  @FederatedByAggregateMethod
  public List<String> getAllTripProblemReportLabels();
  
  /**
   * Return an id for the currently loaded bundle.  Assumes bundle meta data is loaded.
   * @return a string representing the current bundle.
   */
  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public String getActiveBundleId();

  @FederatedByAgencyIdMethod(propertyExpression = "agencyId")
  public BundleMetadata getBundleMetadata();
  
  /**
   * Retrieve a list of time predictions for the given trip as represented by the TripStatusBean.
   * @param tripStatus the query parameters of the trip
   * @return a list of TimepointPredictionRecords.
   */
  @FederatedByAgencyIdMethod
  public List<TimepointPredictionRecord> getPredictionRecordsForTrip(String agencyId, TripStatusBean tripStatus);
  
  /**
   * Check to see if scheduled service is expected.
   */
  @FederatedByAgencyIdMethod
  public Boolean routeHasUpcomingScheduledService(String agencyId, long time, String routeId, String directionId);

  /**
   * Check to see if scheduled service is expected.
   */
  @FederatedByAgencyIdMethod
  public Boolean stopHasUpcomingScheduledService(String agencyId, long time, String stopId, String routeId, String directionId);

  /**
   * Given search string input, match against GTFS route short names and return a list of 
   * potential matches. 
   */
  @FederatedByAgencyIdMethod
  public List<String> getSearchSuggestions(String agencyId, String input);
/*
  *//**
   * Return version information for this OneBusAway instance.
   * 
   * @return GitRepositoryState containing this instance's version information.
   */
  @FederatedByAggregateMethod
  public Map<String, GitRepositoryState> getGitRepositoryState();
  
  /**
   * Return instance details for this OneBusAway instance.
   * 
   * @return InstanceDetails containing this instance's details.
   */
  @FederatedByAggregateMethod
  public Map<String, InstanceDetails> getInstanceDetails();

}
