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

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByAggregateMethod;
import org.onebusaway.federations.annotations.FederatedByAnyEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinateBoundsMethod;
import org.onebusaway.federations.annotations.FederatedByCoordinatePointsMethod;
import org.onebusaway.federations.annotations.FederatedByCustomMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdsMethod;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.ScheduledBlockLocationBean;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.model.problems.ETripProblemGroupBy;
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
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBeanFederatedServiceMethodInvocationHandler;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
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
   * @param routeId
   * @param serviceDate
   * @return the stops for the specified route and service date, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopsForRouteBean getStopsForRouteForServiceDate(String routeId, ServiceDate serviceDate)
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

  @FederatedByEntityIdMethod
  public List<BlockInstanceBean> getActiveBlocksForRoute(AgencyAndId route, long timeFrom, long timeTo);

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

  @FederatedByEntityIdMethod
  public VehicleLocationRecordBean getVehiclePositionForVehicleId(String vehicleId);

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

  @FederatedByEntityIdMethod
  VehicleOccupancyRecord getVehicleOccupancyRecordForVehicleIdAndRoute(AgencyAndId var1, String var2, String var3);

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
   * @param query
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
   * @param query
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
   * @param routeId
   * @param serviceDate
   * @return retrieve the full schedule for the route on the specified date
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public RouteScheduleBean getScheduleForRoute(AgencyAndId routeId, ServiceDate serviceDate);

  /**
   * 
   * @param query determines the query bounds, along with an optional stop code
   * @return find all stops within the specified bounds query
   * @throws ServiceException
   */
  @FederatedByCoordinateBoundsMethod(propertyExpression = "bounds")
  public StopsBean getStops(SearchQueryBean query) throws ServiceException;


  /**
   * Search for stops based on stop name
   * @param stopName
   * @return
   * @throws ServiceException
   */
  @FederatedByAggregateMethod
  public StopsBean getStopsByName(String stopName) throws ServiceException;


    /**
     * @param stopId
     * @return the stop with the specified id, or null if not found
     * @throws ServiceException
     */
  @FederatedByEntityIdMethod
  public StopBean getStop(String stopId) throws ServiceException;

  /**
   * @param stopId
   * @param serviceDate
   * @return the stop with the specified id, or null if not found
   * @throws ServiceException
   */
  @FederatedByEntityIdMethod
  public StopBean getStopForServiceDate(String stopId, ServiceDate serviceDate) throws ServiceException;

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

  /****
   * Historical Data
   ****/

  List<OccupancyStatusBean> getHistoricalRidershipForStop(HistoricalOccupancyByStopQueryBean query);
  List<OccupancyStatusBean> getAllHistoricalRiderships(long serviceDate);
  List<OccupancyStatusBean> getHistoricalRidershipsForTrip(AgencyAndId tripId, long serviceDate);
  List<OccupancyStatusBean> getHistoricalRidershipsForRoute(AgencyAndId routeId, long serviceDate);
  List<OccupancyStatusBean> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId, long serviceDate);


  /****
   * Service Alert Methods
   ****/

  @FederatedByAgencyIdMethod
  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean situation);

  @FederatedByEntityIdMethod(propertyExpression = "id")
  public void updateServiceAlert(ServiceAlertBean situation);
  
  @FederatedByEntityIdMethod
  public ServiceAlertBean copyServiceAlert(String agencyId, ServiceAlertBean situation);

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

  /**
   * Given a stop, route, and direction, test if that stop has revenue service
   * on the given route in the given direction.
   * 
   * @param agencyId    Agency ID of stop; used only for routing requests
   *                    to federated backends
   * @param stopId      Agency-and-ID of stop being tested
   * @param routeId     Agency-and-ID of route to filter for
   * @param directionId Direction ID to filter for
   * @return true if the stop being tested ever permits boarding or alighting
   *         from the specified route in the specified direction in the 
   *         currently-loaded bundle; false otherwise
   */
  @FederatedByAgencyIdMethod
  public Boolean stopHasRevenueServiceOnRoute(String agencyId, String stopId,
                String routeId, String directionId);
  
  /**
   * Given a stop, test if that stop has revenue service.
   * 
   * @param agencyId Agency ID of stop; used only for routing requests
   *                 to federated backends
   * @param stopId   Agency-and-ID of stop being tested
   * @return true if the stop being tested ever permits boarding or alighting
   *         from any route in any direction in the currently-loaded bundle;
   *         false otherwise
   */
  @FederatedByAgencyIdMethod
  public Boolean stopHasRevenueService(String agencyId, String stopId);

  /**
   * Get all stops that have revenue service for the listed agency.
   */
  @FederatedByAgencyIdMethod
  public List<StopBean> getAllRevenueStops(AgencyWithCoverageBean agency);

  /**
   * Get consolidated stops for agency
   */
  @FederatedByAgencyIdMethod
  public ListBean<ConsolidatedStopMapBean> getAllConsolidatedStops();
  
  @FederatedByAgencyIdMethod
  public ListBean<ServiceAlertRecordBean> getAllServiceAlertRecordsForAgencyId(
		String agencyId);
}
