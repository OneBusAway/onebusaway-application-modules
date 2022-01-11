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

import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.annotations.FederatedByAgencyIdMethod;
import org.onebusaway.federations.annotations.FederatedByEntityIdMethod;
import org.onebusaway.geospatial.model.CoordinateBounds;
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
import org.onebusaway.transit_data.model.problems.*;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.trips.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.apc.VehicleOccupancyRecordCache;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.*;
import org.onebusaway.transit_data_federation.services.beans.*;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.bundle.TransitDataServiceTemplate;
import org.onebusaway.transit_data_federation.services.realtime.CurrentVehicleEstimationService;
import org.onebusaway.transit_data_federation.services.reporting.UserReportingService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

public class TransitDataServiceTemplateImpl implements TransitDataServiceTemplate {
  
  private static Logger _log = LoggerFactory.getLogger(TransitDataServiceTemplateImpl.class);
  
  @Autowired
  private TransitGraphDao _transitGraphDao;

  @Autowired
  private AgencyBeanService _agencyBeanService;

  @Autowired
  private AgencyService _agencyService;

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private RouteBeanService _routeBeanService;

  @Autowired
  private StopScheduleBeanService _stopScheduleBeanService;

  @Autowired
  private RouteScheduleBeanService _routeScheduleBeanService;

  @Autowired
  private StopWithArrivalsAndDeparturesBeanService _stopWithArrivalsAndDepaturesBeanService;

  @Autowired
  private ArrivalsAndDeparturesBeanService _arrivalsAndDeparturesBeanService;

  @Autowired
  private ArrivalAndDepartureAlarmService _arrivalAndDepartureAlarmService;

  @Autowired
  private StopsBeanService _stopsBeanService;

  @Autowired
  private RoutesBeanService _routesBeanService;

  @Autowired
  private TripBeanService _tripBeanService;

  @Autowired
  private TripDetailsBeanService _tripDetailsBeanService;

  @Autowired
  private BlockBeanService _blockBeanService;

  @Autowired
  private BlockCalendarService _blockCalendarService;

  @Autowired
  private ShapeBeanService _shapeBeanService;

  @Autowired
  private ServiceAlertsBeanService _serviceAlertsBeanService;

  @Autowired
  private UserReportingService _userReportingService;

  @Autowired
  private CurrentVehicleEstimationService _currentVehicleEstimateService;

  @Autowired
  private VehicleStatusBeanService _vehicleStatusBeanService;

  @Autowired
  private PredictionHelperService _predictionHelperService;
  
  @Autowired
  private ScheduleHelperService _scheduleHelperService;

  @Autowired
  private ConsolidatedStopsService _consolidatedStopsService;

  @Autowired
  private RidershipService _ridershipService;

  @Autowired
  private VehicleOccupancyRecordCache _vehicleOccupancyRecordCache;

  /****
   * {@link TransitDataService} Interface
   ****/

  //@Override
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea() {
    
    Map<String, CoordinateBounds> agencyIdsAndCoverageAreas = _agencyService.getAgencyIdsAndCoverageAreas();
    Map<String, List<CoordinateBounds>> result = new HashMap<String, List<CoordinateBounds>>();
    for (Map.Entry<String, CoordinateBounds> entry : agencyIdsAndCoverageAreas.entrySet()) {
      String agencyId = entry.getKey();
      CoordinateBounds bounds = entry.getValue();
      List<CoordinateBounds> coverage = Arrays.asList(bounds);
      result.put(agencyId, coverage);
    }
    return result;
  }

  //@Override
  public List<AgencyWithCoverageBean> getAgenciesWithCoverage()
      throws ServiceException {
    
    Map<String, CoordinateBounds> agencyIdsAndCoverageAreas = _agencyService.getAgencyIdsAndCoverageAreas();
    List<AgencyWithCoverageBean> beans = new ArrayList<AgencyWithCoverageBean>();

    for (Map.Entry<String, CoordinateBounds> entry : agencyIdsAndCoverageAreas.entrySet()) {

      String agencyId = entry.getKey();
      CoordinateBounds bounds = entry.getValue();

      AgencyBean agencyBean = _agencyBeanService.getAgencyForId(agencyId);
      if (agencyBean == null)
        throw new ServiceException("agency not found: " + agencyId);

      AgencyWithCoverageBean bean = new AgencyWithCoverageBean();
      bean.setAgency(agencyBean);
      bean.setLat((bounds.getMaxLat() + bounds.getMinLat()) / 2);
      bean.setLon((bounds.getMaxLon() + bounds.getMinLon()) / 2);
      bean.setLatSpan(bounds.getMaxLat() - bounds.getMinLat());
      bean.setLonSpan(bounds.getMaxLon() - bounds.getMinLon());

      beans.add(bean);
    }
    return beans;
  }

  //@Override
  public AgencyBean getAgency(String agencyId) throws ServiceException {
    
    return _agencyBeanService.getAgencyForId(agencyId);
  }

  //@Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    
    StopScheduleBean bean = new StopScheduleBean();
    bean.setDate(date);

    AgencyAndId id = convertAgencyAndId(stopId);
    StopBean stopBean = _stopBeanService.getStopForId(id, null);
    if (stopBean == null)
      return null;
    bean.setStop(stopBean);

    ServiceDate serviceDate = new ServiceDate(date);
    List<StopRouteScheduleBean> routes = _stopScheduleBeanService.getScheduledArrivalsForStopAndDate(
        id, serviceDate);
    bean.setRoutes(routes);

    StopCalendarDaysBean calendarDays = _stopScheduleBeanService.getCalendarForStop(id);
    bean.setCalendarDays(calendarDays);

    return bean;
  }

  //@Override
  public RouteScheduleBean getScheduleForRoute(AgencyAndId routeId, ServiceDate serviceDate) {
    return _routeScheduleBeanService.getScheduledArrivalsForDate(routeId, serviceDate);

  }

  //@Override
  public StopsBean getStops(SearchQueryBean query) throws ServiceException {
    checkBounds(query.getBounds());
    return _stopsBeanService.getStops(query);
  }

  //@Override
  public StopsBean getStopsByName(String stopName) throws ServiceException {

    return _stopsBeanService.getStopsByName(stopName);
  }


  //@Override
  public StopBean getStop(String stopId) throws ServiceException {
    
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopBeanService.getStopForId(id, null);
  }

  //@Override
  public StopBean getStopForServiceDate(String stopId, ServiceDate serviceDate) throws ServiceException {

    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopBeanService.getStopForIdForServiceDate(id, serviceDate);
  }

  //@Override
  public ListBean<String> getStopIdsForAgencyId(String agencyId) {
    
    return _stopsBeanService.getStopsIdsForAgencyId(agencyId);
  }

  //@Override
  public StopWithArrivalsAndDeparturesBean getStopWithArrivalsAndDepartures(
      String stopId, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    
    AgencyAndId id = convertAgencyAndId(stopId);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesByStopId(
        id, query);
  }

  //@Override
  public StopsWithArrivalsAndDeparturesBean getStopsWithArrivalsAndDepartures(
      Collection<String> stopIds, ArrivalsAndDeparturesQueryBean query)
      throws ServiceException {
    
    Set<AgencyAndId> ids = convertAgencyAndIds(stopIds);
    return _stopWithArrivalsAndDepaturesBeanService.getArrivalsAndDeparturesForStopIds(
        ids, query);
  }

  //@Override
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      ArrivalAndDepartureForStopQueryBean query) throws ServiceException {
    
    ArrivalAndDepartureQuery adQuery = createArrivalAndDepartureQuery(query);

    return _arrivalsAndDeparturesBeanService.getArrivalAndDepartureForStop(adQuery);
  }

  //@Override
  public String registerAlarmForArrivalAndDepartureAtStop(
      ArrivalAndDepartureForStopQueryBean query, RegisterAlarmQueryBean alarm) {
    
    
    
    ArrivalAndDepartureQuery adQuery = createArrivalAndDepartureQuery(query);

    AgencyAndId alarmId = _arrivalAndDepartureAlarmService.registerAlarmForArrivalAndDepartureAtStop(
        adQuery, alarm);

    return AgencyAndIdLibrary.convertToString(alarmId);
  }

  //@Override
  public void cancelAlarmForArrivalAndDepartureAtStop(String alarmId) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(alarmId);
    _arrivalAndDepartureAlarmService.cancelAlarmForArrivalAndDepartureAtStop(id);
  }

  //@Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    
    return _routeBeanService.getRouteForId(convertAgencyAndId(routeId));
  }

  //@Override
  public ListBean<String> getRouteIdsForAgencyId(String agencyId) {
    
    return _routesBeanService.getRouteIdsForAgencyId(agencyId);
  }

  //@Override
  public ListBean<RouteBean> getRoutesForAgencyId(String agencyId) {
    
    return _routesBeanService.getRoutesForAgencyId(agencyId);
  }

  //@Override
  public StopsForRouteBean getStopsForRoute(String routeId) {
    
    return _routeBeanService.getStopsForRoute(convertAgencyAndId(routeId));
  }

  //@Override
  public StopsForRouteBean getStopsForRouteForServiceDate(String routeId, ServiceDate serviceDate) {

    return _routeBeanService.getStopsForRouteForServiceDate(convertAgencyAndId(routeId), serviceDate);
  }

  //@Override
  public TripBean getTrip(String tripId) throws ServiceException {
    
    return _tripBeanService.getTripForId(convertAgencyAndId(tripId));
  }

  //@Override
  public TripDetailsBean getSingleTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    
    return _tripDetailsBeanService.getTripForId(query);
  }

  //@Override
  public ListBean<TripDetailsBean> getTripDetails(TripDetailsQueryBean query)
      throws ServiceException {
    
    return _tripDetailsBeanService.getTripsForId(query);
  }

  //@Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    checkBounds(query.getBounds());
    return _tripDetailsBeanService.getTripsForBounds(query);
  }

  //@Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
    
    return _tripDetailsBeanService.getTripsForRoute(query);
  }

  //@Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {
    
    return _tripDetailsBeanService.getTripsForAgency(query);
  }

  //@Override
  public BlockBean getBlockForId(String blockId) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getBlockForId(id);
  }

  //@Override
  public BlockInstanceBean getBlockInstance(String blockId, long serviceDate) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getBlockInstance(id, serviceDate);
  }

  //@Override
  public ScheduledBlockLocationBean getScheduledBlockLocationFromScheduledTime(
      String blockId, long serviceDate, int scheduledTime) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(blockId);
    return _blockBeanService.getScheduledBlockLocationFromScheduledTime(id,
        serviceDate, scheduledTime);
  }

  public List<BlockInstanceBean> getActiveBlocksForRoute(AgencyAndId route, long timeFrom, long timeTo){
    List<BlockInstanceBean> blockInstanceBeans = new ArrayList<>();
    List<BlockInstance> blockInstances = _blockCalendarService.getActiveBlocksForRouteInTimeRange(route, timeFrom, timeTo);
    for(BlockInstance block : blockInstances){
      // TODO - Refactor blockBeanService.getBlockInstanceAsBean to factory
      blockInstanceBeans.add(_blockBeanService.getBlockInstanceAsBean(block));
    }
    return blockInstanceBeans;
  }

  //@Override
  public VehicleStatusBean getVehicleForAgency(String vehicleId, long time) {
    
    AgencyAndId vid = AgencyAndIdLibrary.convertFromString(vehicleId);
    return _vehicleStatusBeanService.getVehicleForId(vid, time);
  }

  //@Override
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time) {
    
    return _vehicleStatusBeanService.getAllVehiclesForAgency(agencyId, time);
  }

  //@Override
  public VehicleLocationRecordBean getVehicleLocationRecordForVehicleId(
      String vehicleId, long targetTime) {
    
    AgencyAndId id = convertAgencyAndId(vehicleId);
    return _vehicleStatusBeanService.getVehicleLocationRecordForVehicleId(id,
        targetTime);
  }

  //@Override
  public VehicleLocationRecordBean getVehiclePositionForVehicleId(String vehicleId) {
    AgencyAndId id = convertAgencyAndId(vehicleId);
    return _vehicleStatusBeanService.getVehiclePositionForVehicleId(id);
  }

    //@Override
  public TripDetailsBean getTripDetailsForVehicleAndTime(
      TripForVehicleQueryBean query) {
    
    AgencyAndId id = convertAgencyAndId(query.getVehicleId());
    return _tripDetailsBeanService.getTripForVehicle(id,
        query.getTime().getTime(), query.getInclusion());
  }

  //@Override
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException {
    checkBounds(query.getBounds());
    return _routesBeanService.getRoutesForQuery(query);
  }

  //@Override
  public EncodedPolylineBean getShapeForId(String shapeId) {
    
    AgencyAndId id = convertAgencyAndId(shapeId);
    return _shapeBeanService.getPolylineForShapeId(id);
  }

  //@Override
  public ListBean<String> getShapeIdsForAgencyId(String agencyId) {
    
    return _shapeBeanService.getShapeIdsForAgencyId(agencyId);
  }

  //@Override
  public ListBean<CurrentVehicleEstimateBean> getCurrentVehicleEstimates(
      CurrentVehicleEstimateQueryBean query) {
    
    return _currentVehicleEstimateService.getCurrentVehicleEstimates(query);
  }


  public List<OccupancyStatusBean> getHistoricalRidershipForStop(HistoricalOccupancyByStopQueryBean query){
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(query.getStopId());
    List<HistoricalRidership> hrs = _ridershipService.getHistoricalRidershipsForStop(id, query.getServiceDate());
    return _ridershipService.convertToOccupancyStatusBeans(hrs);
  }


  public List<OccupancyStatusBean> getHistoricalRidershipsForTrip(AgencyAndId tripId, long serviceDate) {

    List<HistoricalRidership> hrs = _ridershipService.getHistoricalRidershipsForTrip(tripId, serviceDate);
    return _ridershipService.convertToOccupancyStatusBeans(hrs);
  }

  public List<OccupancyStatusBean> getHistoricalRidershipsForRoute(AgencyAndId routeId, long serviceDate) {

    List<HistoricalRidership> hrs = _ridershipService.getHistoricalRidershipsForRoute(routeId, serviceDate);
    return _ridershipService.convertToOccupancyStatusBeans(hrs);
  }

  public List<OccupancyStatusBean> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId, long serviceDate) {

    List<HistoricalRidership> hrs = _ridershipService.getHistoricalRiderships(routeId, tripId, stopId, serviceDate);
    return _ridershipService.convertToOccupancyStatusBeans(hrs);
  }

  public List<OccupancyStatusBean> getAllHistoricalRiderships(long serviceDate) {

    List<HistoricalRidership> hrs = _ridershipService.getAllHistoricalRiderships(serviceDate);
    return _ridershipService.convertToOccupancyStatusBeans(hrs);
  }

  /****
   * 
   ****/

  public ListBean<VehicleLocationRecordBean> getVehicleLocationRecords(
      VehicleLocationRecordQueryBean query) {
    
    return _vehicleStatusBeanService.getVehicleLocations(query);
  }


  //@Override
  public VehicleOccupancyRecord getVehicleOccupancyRecordForVehicleIdAndRoute(AgencyAndId vehicleId, String routeId, String directionId) {
    return _vehicleOccupancyRecordCache.getRecordForVehicleIdAndRoute(vehicleId, routeId, directionId);
  }


  //@Override
  public void submitVehicleLocation(VehicleLocationRecordBean record) {
    
    _vehicleStatusBeanService.submitVehicleLocation(record);
  }

  @FederatedByEntityIdMethod
  public void resetVehicleLocation(String vehicleId) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(vehicleId);
    _vehicleStatusBeanService.resetVehicleLocation(id);
  }

  /****
   * Service Alert Methods
   ****/

  //@Override
  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean situation) {
    
    return _serviceAlertsBeanService.createServiceAlert(agencyId, situation);
  }

  //@Override
  public void updateServiceAlert(ServiceAlertBean situation) {
    _serviceAlertsBeanService.updateServiceAlert(situation);
  }
  
  public ServiceAlertBean copyServiceAlert(String agencyId,
	      ServiceAlertBean situation) {
    return _serviceAlertsBeanService.copyServiceAlert(agencyId, situation);
  }

  //@Override
  public ServiceAlertBean getServiceAlertForId(String situationId) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(situationId);
    return _serviceAlertsBeanService.getServiceAlertForId(id);
  }

  //@Override
  public void removeServiceAlert(String situationId) {
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(situationId);
    _serviceAlertsBeanService.removeServiceAlert(id);
  }

  //@Override
  public ListBean<ServiceAlertBean> getAllServiceAlertsForAgencyId(
      String agencyId) {
    
    List<ServiceAlertBean> situations = _serviceAlertsBeanService.getServiceAlertsForFederatedAgencyId(agencyId);
    return new ListBean<ServiceAlertBean>(situations, false);
  }
  
  //@Override
  public ListBean<ServiceAlertRecordBean> getAllServiceAlertRecordsForAgencyId(
      String agencyId) {
    
    List<ServiceAlertRecordBean> situations = _serviceAlertsBeanService.getServiceAlertRecordsForFederatedAgencyId(agencyId);
    return new ListBean<ServiceAlertRecordBean>(situations, false);
  }

  //@Override
  public void removeAllServiceAlertsForAgencyId(String agencyId) {
    
    _serviceAlertsBeanService.removeAllServiceAlertsForFederatedAgencyId(agencyId);
  }

  //@Override
  public ListBean<ServiceAlertBean> getServiceAlerts(SituationQueryBean query) {
    
    List<ServiceAlertBean> situations = _serviceAlertsBeanService.getServiceAlerts(query);
    return new ListBean<ServiceAlertBean>(situations, false);
  }

  //@Override
  public void reportProblemWithStop(StopProblemReportBean problem) {
    
    _userReportingService.reportProblemWithStop(problem);
  }

  //@Override
  public void reportProblemWithTrip(TripProblemReportBean problem) {
    
    _userReportingService.reportProblemWithTrip(problem);
  }

  //@Override
  public ListBean<StopProblemReportSummaryBean> getStopProblemReportSummaries(
      StopProblemReportQueryBean query) {
    
    return _userReportingService.getStopProblemReportSummaries(query);
  }

  //@Override
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummaries(
      TripProblemReportQueryBean query) {
    
    return getTripProblemReportSummariesByGrouping(query, ETripProblemGroupBy.TRIP);
  }

  //@Override
  public ListBean<TripProblemReportSummaryBean> getTripProblemReportSummariesByGrouping(
      TripProblemReportQueryBean query, ETripProblemGroupBy groupBy) {
    
    return _userReportingService.getTripProblemReportSummaries(query, groupBy);
  }

  //@Override
  @FederatedByAgencyIdMethod()
  public ListBean<StopProblemReportBean> getStopProblemReports(
      StopProblemReportQueryBean query) {
    
    return _userReportingService.getStopProblemReports(query);
  }

  //@Override
  public ListBean<TripProblemReportBean> getTripProblemReports(
      TripProblemReportQueryBean query) {
    
    return _userReportingService.getTripProblemReports(query);
  }

  //@Override
  public List<StopProblemReportBean> getAllStopProblemReportsForStopId(
      String stopId) {
    
    return _userReportingService.getAllStopProblemReportsForStopId(convertAgencyAndId(stopId));
  }

  //@Override
  public List<TripProblemReportBean> getAllTripProblemReportsForTripId(
      String tripId) {
    
    return _userReportingService.getAllTripProblemReportsForTripId(convertAgencyAndId(tripId));
  }

  //@Override
  public StopProblemReportBean getStopProblemReportForStopIdAndId(
      String stopId, long id) {
    
    return _userReportingService.getStopProblemReportForId(id);
  }

  //@Override
  public TripProblemReportBean getTripProblemReportForTripIdAndId(
      String tripId, long id) {
    
    return _userReportingService.getTripProblemReportForId(id);
  }

  //@Override
  public void deleteStopProblemReportForStopIdAndId(String stopId, long id) {
    
    _userReportingService.deleteStopProblemReportForId(id);
  }

  //@Override
  public void updateTripProblemReport(TripProblemReportBean tripProblemReport) {
    
    _userReportingService.updateTripProblemReport(tripProblemReport);
  }

  //@Override
  public void deleteTripProblemReportForTripIdAndId(String tripId, long id) {
    
    _userReportingService.deleteTripProblemReportForId(id);
  }

  //@Override
  public List<String> getAllTripProblemReportLabels() {
    
    return _userReportingService.getAllTripProblemReportLabels();
  }

  //@Override
  public List<TimepointPredictionRecord> getPredictionRecordsForTrip(
		  String agencyId,
		  TripStatusBean tripStatus) {
    
	  return _predictionHelperService.getPredictionRecordsForTrip(agencyId, tripStatus);
  }

  //@Override
  public Boolean routeHasUpcomingScheduledService(String routeAgencyId, long time, String routeId,
		String directionId) {
    
	  return _scheduleHelperService.routeHasUpcomingScheduledService(routeAgencyId, time, routeId, directionId);
  }

  //@Override
  public Boolean stopHasUpcomingScheduledService(String stopAgencyId, long time, String stopId,
		String routeId, String directionId) {
    
	  return _scheduleHelperService.stopHasUpcomingScheduledService(stopAgencyId, time, stopId, routeId, directionId);
  }

  //@Override
  public List<String> getSearchSuggestions(String agencyId, String input) {
	  return _scheduleHelperService.getSearchSuggestions(agencyId, input);
  }
  
  public Boolean stopHasRevenueServiceOnRoute(String stopAgencyId, String stopId, String routeId, String directionId) {
      return _scheduleHelperService.stopHasRevenueServiceOnRoute(stopAgencyId, stopId, routeId, directionId);
  }
  
  public Boolean stopHasRevenueService(String agencyId, String stopId) {
      return _scheduleHelperService.stopHasRevenueService(agencyId, stopId);    
  }
  
  public List<StopBean> getAllRevenueStops(AgencyWithCoverageBean agency) {
    SearchQueryBean query = new SearchQueryBean();
    CoordinateBounds bounds = new CoordinateBounds();

    double lat = agency.getLat();
    double lon = agency.getLon();
    double latSpan = agency.getLatSpan() / 2;
    double lonSpan = agency.getLonSpan() / 2;
    bounds.addPoint(lat - latSpan, lon - lonSpan);
    bounds.addPoint(lat + latSpan, lon + lonSpan);
    query.setBounds(bounds);
    query.setMaxCount(Integer.MAX_VALUE);
    return _scheduleHelperService.filterRevenueService(agency.getAgency(), getStops(query));
  }


  //@Override
  public String getActiveBundleId() {
    // TODO Auto-generated method stub
    return null;
  }

  //@Override
  public BundleMetadata getBundleMetadata() {
    // TODO Auto-generated method stub
    return null;
  }

  public ListBean<ConsolidatedStopMapBean> getAllConsolidatedStops() {
    ListBean<ConsolidatedStopMapBean> ret = new ListBean<ConsolidatedStopMapBean>();
    Collection<ConsolidatedStopMapBean> beans = _consolidatedStopsService.getAllConsolidatedStops();
    ret.setList(new ArrayList<ConsolidatedStopMapBean>(beans));
    return ret;
  }

  /****
   * Private Methods
   ****/

  protected AgencyAndId convertAgencyAndId(String id) {
    return AgencyAndIdLibrary.convertFromString(id);
  }

  protected Set<AgencyAndId> convertAgencyAndIds(Iterable<String> ids) {
    Set<AgencyAndId> converted = new HashSet<AgencyAndId>();
    for (String id : ids)
      converted.add(convertAgencyAndId(id));
    return converted;
  }

  protected ArrivalAndDepartureQuery createArrivalAndDepartureQuery(
      ArrivalAndDepartureForStopQueryBean query) {

    ArrivalAndDepartureQuery adQuery = new ArrivalAndDepartureQuery();

    AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(query.getStopId());
    StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
    TripEntry trip = _transitGraphDao.getTripEntryForId(tripId);
    if (trip == null)
      throw new NoSuchTripServiceException(query.getTripId());

    adQuery.setStop(stop);
    adQuery.setStopSequence(query.getStopSequence());
    adQuery.setTrip(trip);
    adQuery.setServiceDate(query.getServiceDate());
    adQuery.setVehicleId(AgencyAndIdLibrary.convertFromString(query.getVehicleId()));
    adQuery.setTime(query.getTime());

    return adQuery;
  }

  protected void checkBounds(CoordinateBounds cb) {
    if (cb == null) {
      return;
    }

    Collection<CoordinateBounds> allAgencyBounds = _agencyService.getAgencyIdsAndCoverageAreas().values();

    for (CoordinateBounds agencyBounds : allAgencyBounds) {
      if (agencyBounds.intersects(cb)) {
        return;
      }
    }

    throw new OutOfServiceAreaServiceException();
  }
}
