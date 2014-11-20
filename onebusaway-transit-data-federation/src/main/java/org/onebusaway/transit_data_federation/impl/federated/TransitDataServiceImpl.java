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
package org.onebusaway.transit_data_federation.impl.federated;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.geospatial.model.CoordinateBounds;
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
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.IntrospectionService;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.onebusaway.utility.GitRepositoryState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransitDataServiceImpl implements TransitDataService {
  
  private static Logger _log = LoggerFactory.getLogger(TransitDataServiceImpl.class);
  
  @Autowired
  private TransitDataServiceTemplateImpl _transitDataService;
  
  @Autowired
  private IntrospectionService _introspectionService;
  
  private BundleManagementService _bundleManagementService;
  
  @Autowired
  public void set_bundleManagementService(
      BundleManagementService _bundleManagementService) {
    this._bundleManagementService = _bundleManagementService;
  }

  private int _blockedRequestCounter = 0;

  /**
   * This method blocks until the bundle is ready--this method is called as part of the proxy to each of the underlying
   * methods of the TDS to ensure all calls to those bundle-backed methods succeed (i.e. the bundle is ready
   * to be queried.)
   */
  private void blockUntilBundleIsReady() {
    try {
      while(_bundleManagementService != null && !_bundleManagementService.bundleIsReady()) {
        _blockedRequestCounter++;

        // only print this every 25 times so we don't fill up the logs!
        if(_blockedRequestCounter > 25) {
          _log.warn("Bundle is not ready or none is loaded--we've blocked 25 TDS requests since last log event.");
          _blockedRequestCounter = 0;
        }

        synchronized(this) {
          Thread.sleep(250);
          Thread.yield();
        }
      }
    } catch(InterruptedException e) {
      return;
    }
  }

  /****
   * {@link TransitDataService} Interface
   ****/

  @Override
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea() {
    blockUntilBundleIsReady();
    return _transitDataService.getAgencyIdsWithCoverageArea();
  }

  @Override
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getAgenciesWithCoverage();
  }

  @Override
  public AgencyBean getAgency(String agencyId) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getAgency(agencyId);
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getScheduleForStop(stopId, date);
  }

  @Override
  public StopsBean getStops(SearchQueryBean query) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getStops(query);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getStop(stopId);
  }

  @Override
  public ListBean<String> getStopIdsForAgencyId(String agencyId) {
    blockUntilBundleIsReady();
    return _transitDataService.getStopIdsForAgencyId(agencyId);
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getStopWithArrivalsAndDepartures(
        stopId, query);
  }

  @Override
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getStopsWithArrivalsAndDepartures(
        stopIds, query);
  }

  @Override
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      ArrivalAndDepartureForStopQueryBean query) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getArrivalAndDepartureForStop(query);
  }

  @Override
  public String registerAlarmForArrivalAndDepartureAtStop(
      ArrivalAndDepartureForStopQueryBean query, RegisterAlarmQueryBean alarm) {
    blockUntilBundleIsReady();
    return _transitDataService.registerAlarmForArrivalAndDepartureAtStop(query, alarm);
  }

  @Override
  public void cancelAlarmForArrivalAndDepartureAtStop(String alarmId) {
    blockUntilBundleIsReady();
    _transitDataService.cancelAlarmForArrivalAndDepartureAtStop(alarmId);
  }

  @Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getRouteForId(routeId);
  }

  @Override
  public ListBean<String> getRouteIdsForAgencyId(String agencyId) {
    blockUntilBundleIsReady();
    return _transitDataService.getRouteIdsForAgencyId(agencyId);
  }

  @Override
  public ListBean<RouteBean> getRoutesForAgencyId(String agencyId) {
    blockUntilBundleIsReady();
    return _transitDataService.getRoutesForAgencyId(agencyId);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId) {
    blockUntilBundleIsReady();
    return _transitDataService.getStopsForRoute(routeId);
  }

  @Override
  public TripBean getTrip(String tripId) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getTrip(tripId);
  }

  @Override
  public TripDetailsBean getSingleTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getSingleTripDetails(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getTripDetails(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripsForBounds(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripsForRoute(query);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripsForAgency(query);
  }

  @Override
  public BlockBean getBlockForId(String blockId) {
    blockUntilBundleIsReady();
    return _transitDataService.getBlockForId(blockId);
  }

  @Override
  public BlockInstanceBean getBlockInstance(String blockId, long serviceDate) {
    blockUntilBundleIsReady();
    return _transitDataService.getBlockInstance(blockId, serviceDate);
  }

  @Override
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(
      String blockId, long serviceDate, int scheduledTime) {
    blockUntilBundleIsReady();
    return _transitDataService.getScheduledBlockLocationFromScheduledTime(blockId,
        serviceDate, scheduledTime);
  }

  @Override
  public VehicleStatusBean getVehicleForAgency(String vehicleId, long time) {
    blockUntilBundleIsReady();
    return _transitDataService.getVehicleForAgency(vehicleId, time);
  }

  @Override
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time) {
    blockUntilBundleIsReady();
    return _transitDataService.getAllVehiclesForAgency(agencyId, time);
  }

  @Override
  public VehicleLocationRecordBean getVehicleLocationRecordForVehicleId(
      String vehicleId, long targetTime) {
    blockUntilBundleIsReady();
    return _transitDataService.getVehicleLocationRecordForVehicleId(vehicleId,
        targetTime);
  }

  @Override
  public TripDetailsBean getTripDetailsForVehicleAndTime(
      TripForVehicleQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripDetailsForVehicleAndTime(query);
  }

  @Override
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getRoutes(query);
  }

  @Override
  public EncodedPolylineBean getShapeForId(String shapeId) {
    blockUntilBundleIsReady();
    return _transitDataService.getShapeForId(shapeId);
  }

  @Override
  public ListBean<String> getShapeIdsForAgencyId(String agencyId) {
    blockUntilBundleIsReady();
    return _transitDataService.getShapeIdsForAgencyId(agencyId);
  }

  @Override
  public ListBean<CurrentVehicleEstimateBean> getCurrentVehicleEstimates(
      CurrentVehicleEstimateQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getCurrentVehicleEstimates(query);
  }

  @Override
  public ItinerariesBean getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ConstraintsBean constraints)
      throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getItinerariesBetween(from, to, targetTime,
        constraints);
  }

  @Override
  public void reportProblemWithPlannedTrip(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ConstraintsBean constraints,
      PlannedTripProblemReportBean report) {
    blockUntilBundleIsReady();
    _transitDataService.reportProblemWithPlannedTrip(from, to, targetTime,
        constraints, report);
  }

  @Override
  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getStreetGraphForRegion(latFrom, lonFrom,
        latTo, lonTo);
  }

  @Override
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getMinTravelTimeToStopsFrom(location, time,
        constraints);
  }

  public List<TimedPlaceBean> getLocalPaths(String agencyId,
      ConstraintsBean constraints,
      MinTravelTimeToStopsBean minTravelTimeToStops,
      List<LocalSearchResult> localResults) throws ServiceException {
    blockUntilBundleIsReady();
    return _transitDataService.getLocalPaths(agencyId,
        constraints, minTravelTimeToStops, localResults);
  }

  /****
   * 
   ****/

  public ListBean<VehicleLocationRecordBean> getVehicleLocationRecords(
      VehicleLocationRecordQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getVehicleLocationRecords(query);
  }

  @Override
  public void submitVehicleLocation(VehicleLocationRecordBean record) {
    blockUntilBundleIsReady();
    _transitDataService.submitVehicleLocation(record);
  }

  @FederatedByEntityIdMethod
  public void resetVehicleLocation(String vehicleId) {
    blockUntilBundleIsReady();
    _transitDataService.resetVehicleLocation(vehicleId);
  }

  /****
   * Service Alert Methods
   ****/

  @Override
  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean situation) {
    blockUntilBundleIsReady();
    return _transitDataService.createServiceAlert(agencyId, situation);
  }

  @Override
  public void updateServiceAlert(ServiceAlertBean situation) {
    blockUntilBundleIsReady();
    _transitDataService.updateServiceAlert(situation);
  }

  @Override
  public ServiceAlertBean getServiceAlertForId(String situationId) {
    blockUntilBundleIsReady();
    return _transitDataService.getServiceAlertForId(situationId);
  }

  @Override
  public void removeServiceAlert(String situationId) {
    blockUntilBundleIsReady();
    _transitDataService.removeServiceAlert(situationId);
  }

  @Override
  public ListBean<ServiceAlertBean> getAllServiceAlertsForAgencyId(
      String agencyId) {
    blockUntilBundleIsReady();
    return _transitDataService.getAllServiceAlertsForAgencyId(agencyId);
  }

  @Override
  public void removeAllServiceAlertsForAgencyId(String agencyId) {
    blockUntilBundleIsReady();
    _transitDataService.removeAllServiceAlertsForAgencyId(agencyId);
  }

  @Override
  public ListBean<ServiceAlertBean> getServiceAlerts(SituationQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getServiceAlerts(query);
  }

  @Override
  public void reportProblemWithStop(StopProblemReportBean problem) {
    blockUntilBundleIsReady();
    _transitDataService.reportProblemWithStop(problem);
  }

  @Override
  public void reportProblemWithTrip(TripProblemReportBean problem) {
    blockUntilBundleIsReady();
    _transitDataService.reportProblemWithTrip(problem);
  }

  @Override
  public ListBean<StopProblemReportSummaryBean> getStopProblemReportSummaries(
      StopProblemReportQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getStopProblemReportSummaries(query);
  }

  @Override
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummaries(
      TripProblemReportQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripProblemReportSummaries(query);
  }

  @Override
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummariesByGrouping(
      TripProblemReportQueryBean query, ETripProblemGroupBy groupBy) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripProblemReportSummariesByGrouping(query, groupBy);
  }

  @Override
  @FederatedByAgencyIdMethod()
  public ListBean<StopProblemReportBean> getStopProblemReports(
      StopProblemReportQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getStopProblemReports(query);
  }

  @Override
  public ListBean<TripProblemReportBean> getTripProblemReports(
      TripProblemReportQueryBean query) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripProblemReports(query);
  }

  @Override
  public List<StopProblemReportBean> getAllStopProblemReportsForStopId(
      String stopId) {
    blockUntilBundleIsReady();
    return _transitDataService.getAllStopProblemReportsForStopId(stopId);
  }

  @Override
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      String tripId) {
    blockUntilBundleIsReady();
    return _transitDataService.getAllTripProblemReportsForTripId(tripId);
  }

  @Override
  public StopProblemReportBean getStopProblemReportForStopIdAndId(
      String stopId, long id) {
    blockUntilBundleIsReady();
    return _transitDataService.getStopProblemReportForStopIdAndId(stopId, id);
  }

  @Override
  public TripProblemReportBean getTripProblemReportForTripIdAndId(
      String tripId, long id) {
    blockUntilBundleIsReady();
    return _transitDataService.getTripProblemReportForTripIdAndId(tripId, id);
  }

  @Override
  public void deleteStopProblemReportForStopIdAndId(String stopId, long id) {
    blockUntilBundleIsReady();
    _transitDataService.deleteStopProblemReportForStopIdAndId(stopId, id);
  }

  @Override
  public void updateTripProblemReport(TripProblemReportBean tripProblemReport) {
    blockUntilBundleIsReady();
    _transitDataService.updateTripProblemReport(tripProblemReport);
  }

  @Override
  public void deleteTripProblemReportForTripIdAndId(String tripId, long id) {
    blockUntilBundleIsReady();
    _transitDataService.deleteTripProblemReportForTripIdAndId(tripId, id);
  }

  @Override
  public List<String> getAllTripProblemReportLabels() {
    blockUntilBundleIsReady();
    return _transitDataService.getAllTripProblemReportLabels();
  }
  

  @Override
  public String getActiveBundleId() {
    blockUntilBundleIsReady();
	  return _bundleManagementService.getActiveBundleId();
  }
  
  @Override
  public BundleMetadata getBundleMetadata() {
    blockUntilBundleIsReady();
    return _bundleManagementService.getBundleMetadata();
  }

  @Override
  public List<TimepointPredictionRecord> getPredictionRecordsForTrip(
		  String agencyId,
		  TripStatusBean tripStatus) {
    blockUntilBundleIsReady();
	  return _transitDataService.getPredictionRecordsForTrip(agencyId, tripStatus);
  }

  @Override
  public Boolean routeHasUpcomingScheduledService(String agencyId, long time, String routeId,
		String directionId) {
    blockUntilBundleIsReady();
	  return _transitDataService.routeHasUpcomingScheduledService(agencyId, time, routeId, directionId);
  }

  @Override
  public Boolean stopHasUpcomingScheduledService(String agencyId, long time, String stopId,
		String routeId, String directionId) {
    blockUntilBundleIsReady();
	  return _transitDataService.stopHasUpcomingScheduledService(agencyId, time, stopId, routeId, directionId);
  }

  @Override
  public List<String> getSearchSuggestions(String agencyId, String input) {
	  return _transitDataService.getSearchSuggestions(agencyId, input);
  }
  
  public Map<String, GitRepositoryState> getGitRepositoryState() {
	  GitRepositoryState grs = _introspectionService.getGitRepositoryState();
	  Map<String, GitRepositoryState> grsmap = Collections.singletonMap(
			  _introspectionService.getInstanceDetails().getInstanceName(),grs);
	  
	  return grsmap;
  }
  
  public Map<String, InstanceDetails> getInstanceDetails() {
    return Collections.singletonMap(_introspectionService.getInstanceDetails().getInstanceName(),
    _introspectionService.getInstanceDetails());
  }

}
