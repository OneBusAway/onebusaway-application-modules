/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.OnwardCallsMode;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.api.actions.siri.model.RouteDirection;
import org.onebusaway.api.actions.siri.model.RouteForDirection;
import org.onebusaway.api.actions.siri.model.RouteResult;
import org.onebusaway.api.actions.siri.model.StopOnRoute;
import org.onebusaway.api.actions.siri.model.StopRouteDirection;
import org.onebusaway.api.actions.siri.service.RealtimeServiceV2;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
//import org.onebusaway.nyc.siri.support.SiriJsonSerializerV2;
//import org.onebusaway.nyc.siri.support.SiriXmlSerializerV2;
import org.onebusaway.presentation.impl.DateUtil;
//import org.onebusaway.presentation.impl.realtime.SiriSupport.OnwardCallsMode;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripForVehicleQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.transit_data_federation.siri.SiriJsonSerializerV2;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializerV2;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.org.siri.siri_2.AnnotatedLineStructure;
import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.MonitoredStopVisitStructure;
import uk.org.siri.siri_2.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri_2.VehicleActivityStructure;
import uk.org.siri.siri_2.VehicleActivityStructure.MonitoredVehicleJourney;

/**
 * A source of SIRI classes containing real time data, subject to the
 * conventions expressed in the PresentationService.
 * 
 * @author jmaki
 * @authoer lcaraballo
 *
 */
@Component
public class RealtimeServiceV2Impl implements RealtimeServiceV2 {

  @Autowired
  public TransitDataService _transitDataService;

  private ConfigurationService _configurationService;

  private PresentationService _presentationService;

  private SiriXmlSerializerV2 _siriXmlSerializer = new SiriXmlSerializerV2();

  private SiriJsonSerializerV2 _siriJsonSerializer = new SiriJsonSerializerV2();

  private Long _now = null;

  private Boolean _useApc = null;

  @Override
  public void setTime(long time) {
    _now = time;
    _presentationService.setTime(time);
  }

  public long getTime() {
    if (_now != null)
      return _now;
    else
      return SystemTime.currentTimeMillis();
  }

//  @Autowired
  public void setTransitDataService(
      TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setConfigurationService(ConfigurationService configService) {
    _configurationService = configService;
  }

  @Autowired
  public void setPresentationService(PresentationService presentationService) {
    _presentationService = presentationService;
  }

  @Override
  public PresentationService getPresentationService() {
    return _presentationService;
  }

  @Override
  public SiriJsonSerializerV2 getSiriJsonSerializer() {
    return _siriJsonSerializer;
  }

  @Override
  public SiriXmlSerializerV2 getSiriXmlSerializer() {
    return _siriXmlSerializer;
  }

  /**
   * SIRI METHODS
   */

  @Override
  public List<VehicleActivityStructure> getVehicleActivityForRoute(
      String routeId, String directionId, int maximumOnwardCalls, DetailLevel detailLevel,
      long currentTime) {
    List<VehicleActivityStructure> output = new ArrayList<VehicleActivityStructure>();

    ListBean<TripDetailsBean> trips = getAllTripsForRoute(routeId,
        currentTime);
    for (TripDetailsBean tripDetails : trips.getList()) {
      if(tripDetails == null) continue;
      // filter out interlined routes
      if (routeId != null
          && !tripDetails.getTrip().getRoute().getId()
              .equals(routeId))
        continue;

      // filtered out by user
      if (directionId != null
          && !tripDetails.getTrip().getDirectionId()
              .equals(directionId))
        continue;

      if (!_presentationService.include(tripDetails.getStatus()))
        continue;

      VehicleActivityStructure activity = new VehicleActivityStructure();
      activity.setRecordedAtTime(DateUtil
          .toXmlGregorianCalendar(tripDetails.getStatus()
              .getLastUpdateTime()));

      List<TimepointPredictionRecord> timePredictionRecords = null;
      timePredictionRecords = _transitDataService
          .getPredictionRecordsForTrip(AgencyAndId
              .convertFromString(routeId).getAgencyId(),
                tripDetails.getStatus());

      boolean showApc = useApc();
      if (!TransitDataConstants.STATUS_CANCELED.equals(tripDetails.getStatus().getStatus())) {
        activity.setMonitoredVehicleJourney(new MonitoredVehicleJourney());
        SiriSupportV2.fillMonitoredVehicleJourney(
                activity.getMonitoredVehicleJourney(),
                tripDetails.getTrip(), tripDetails.getStatus(), null,
                OnwardCallsMode.VEHICLE_MONITORING, _presentationService,
                _transitDataService, maximumOnwardCalls,
                timePredictionRecords, tripDetails.getStatus().isPredicted(),
                showApc, detailLevel, currentTime, null);
      }
      output.add(activity);
    }

    Collections.sort(output, new Comparator<VehicleActivityStructure>() {
      public int compare(VehicleActivityStructure arg0,
          VehicleActivityStructure arg1) {
        try {
          SiriExtensionWrapper wrapper0 = (SiriExtensionWrapper) arg0
              .getMonitoredVehicleJourney().getMonitoredCall()
              .getExtensions().getAny();
          SiriExtensionWrapper wrapper1 = (SiriExtensionWrapper) arg1
              .getMonitoredVehicleJourney().getMonitoredCall()
              .getExtensions().getAny();
          return wrapper0
              .getDistances()
              .getDistanceFromCall()
              .compareTo(
                  wrapper1.getDistances()
                      .getDistanceFromCall());
        } catch (Exception e) {
          return -1;
        }
      }
    });

    return output;
  }

  @Override
  public VehicleActivityStructure getVehicleActivityForVehicle(
      String vehicleId, int maximumOnwardCalls, DetailLevel detailLevel, long currentTime) {

    TripForVehicleQueryBean query = new TripForVehicleQueryBean();
    query.setTime(new Date(currentTime));
    query.setVehicleId(vehicleId);

    TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();
    inclusion.setIncludeTripStatus(true);
    inclusion.setIncludeTripBean(true);
    query.setInclusion(inclusion);

    TripDetailsBean tripDetailsForCurrentTrip = _transitDataService
        .getTripDetailsForVehicleAndTime(query);
    if (tripDetailsForCurrentTrip != null) {
      if (!_presentationService.include(tripDetailsForCurrentTrip
          .getStatus()))
        return null;

      VehicleActivityStructure output = new VehicleActivityStructure();
      output.setRecordedAtTime(DateUtil
          .toXmlGregorianCalendar(tripDetailsForCurrentTrip
              .getStatus().getLastUpdateTime()));

      List<TimepointPredictionRecord> timePredictionRecords = null;
      timePredictionRecords = _transitDataService
          .getPredictionRecordsForTrip(AgencyAndId
              .convertFromString(vehicleId).getAgencyId(),
              tripDetailsForCurrentTrip.getStatus());

      boolean showApc = useApc();
      if (!TransitDataConstants.STATUS_CANCELED.equals(tripDetailsForCurrentTrip.getStatus().getStatus())) {
        output.setMonitoredVehicleJourney(new MonitoredVehicleJourney());
        SiriSupportV2.fillMonitoredVehicleJourney(
                output.getMonitoredVehicleJourney(),
                tripDetailsForCurrentTrip.getTrip(),
                tripDetailsForCurrentTrip.getStatus(), null,
                OnwardCallsMode.VEHICLE_MONITORING, _presentationService,
                _transitDataService, maximumOnwardCalls,
                timePredictionRecords, tripDetailsForCurrentTrip.getStatus().isPredicted(),
                showApc, detailLevel, currentTime, null);
      }
      return output;
    }

    return null;
  }

  @Override
  public List<MonitoredStopVisitStructure> getMonitoredStopVisitsForStop(
      String stopId, int maximumOnwardCalls, DetailLevel detailLevel, 
      long currentTime, List<AgencyAndId> routeIds, Map<Filters, String> filters) {
    List<MonitoredStopVisitStructure> output = new ArrayList<MonitoredStopVisitStructure>();

    String directionId = filters.get(Filters.DIRECTION_REF);
    int maximumStopVisits = SiriSupportV2.convertToNumeric(filters.get(Filters.MAX_STOP_VISITS), Integer.MAX_VALUE);
    Integer minimumStopVisitsPerLine = SiriSupportV2.convertToNumeric(filters.get(Filters.MIN_STOP_VISITS), null);

    Map<AgencyAndId, Integer> visitCountByLine = new HashMap<AgencyAndId, Integer>();
    int visitCount = 0;

    for (ArrivalAndDepartureBean adBean : getArrivalsAndDeparturesForStop(
        stopId, currentTime)) {

      TripStatusBean statusBeanForCurrentTrip = adBean.getTripStatus();
      TripBean tripBeanForAd = adBean.getTrip();

      if (statusBeanForCurrentTrip == null)
        continue;

      if (!_presentationService.include(statusBeanForCurrentTrip)
          || !_presentationService.include(adBean,
              statusBeanForCurrentTrip))
        continue;

      MonitoredStopVisitStructure stopVisit = new MonitoredStopVisitStructure();
      stopVisit.setRecordedAtTime(DateUtil
          .toXmlGregorianCalendar(statusBeanForCurrentTrip
              .getLastUpdateTime()));

      List<TimepointPredictionRecord> timePredictionRecords = null;
      timePredictionRecords = _transitDataService
          .getPredictionRecordsForTrip(AgencyAndId
              .convertFromString(stopId).getAgencyId(),
                statusBeanForCurrentTrip);

      MonitoredVehicleJourneyStructure mvjourney = new MonitoredVehicleJourneyStructure();

      if (!TransitDataConstants.STATUS_CANCELED.equals(statusBeanForCurrentTrip.getStatus())) {
        stopVisit.setMonitoredVehicleJourney(mvjourney);
        boolean showApc = useApc();
        SiriSupportV2.fillMonitoredVehicleJourney(
                mvjourney, tripBeanForAd,
                statusBeanForCurrentTrip, adBean.getStop(),
                OnwardCallsMode.STOP_MONITORING, _presentationService,
                _transitDataService, maximumOnwardCalls,
                timePredictionRecords, statusBeanForCurrentTrip.isPredicted(),
                showApc, detailLevel, currentTime, filters);
      }
      // FILTERS
      AgencyAndId thisRouteId = AgencyAndIdLibrary
          .convertFromString(mvjourney.getLineRef().getValue());
      String thisDirectionId = mvjourney.getDirectionRef().getValue();

      if (routeIds.size() > 0 && !routeIds.contains(thisRouteId))
        continue;

      if (directionId != null && !thisDirectionId.equals(directionId))
        continue;

      // Monitored Stop Visits
      Map<String, MonitoredStopVisitStructure> visitsMap = new HashMap<String, MonitoredStopVisitStructure>();
 
      // visit count filters
      Integer visitCountForThisLine = visitCountByLine.get(thisRouteId);
      if (visitCountForThisLine == null) {
        visitCountForThisLine = 0;
      }

      if (visitCount >= maximumStopVisits) {
        if (minimumStopVisitsPerLine == null) {
          break;
        } else {
          if (visitCountForThisLine >= minimumStopVisitsPerLine) {
            continue;
          }
        }
      }

      // unique stops filters
      if (stopVisit.getMonitoredVehicleJourney() == null
          || stopVisit.getMonitoredVehicleJourney().getVehicleRef() == null
          || StringUtils.isBlank(stopVisit.getMonitoredVehicleJourney()
              .getVehicleRef().getValue())) {
        continue;
      }   else {
        String visitKey = stopVisit.getMonitoredVehicleJourney()
            .getVehicleRef().getValue();
        if (visitsMap.containsKey(stopVisit.getMonitoredVehicleJourney()
            .getVehicleRef().getValue())) {
          if (stopVisit.getMonitoredVehicleJourney().getProgressStatus() == null) {
            visitsMap.remove(visitKey);
            visitsMap.put(visitKey, stopVisit);
          }
          continue;
        }  else {
          visitsMap.put(stopVisit.getMonitoredVehicleJourney()
              .getVehicleRef().getValue(), stopVisit);
        }
      }

      output.add(stopVisit);

      visitCount++;
      visitCountForThisLine++;
      visitCountByLine.put(thisRouteId, visitCountForThisLine);
    }

    Collections.sort(output, new Comparator<MonitoredStopVisitStructure>() {
      public int compare(MonitoredStopVisitStructure arg0,
          MonitoredStopVisitStructure arg1) {
        try {
          SiriExtensionWrapper wrapper0 = (SiriExtensionWrapper) arg0
              .getMonitoredVehicleJourney().getMonitoredCall()
              .getExtensions().getAny();
          SiriExtensionWrapper wrapper1 = (SiriExtensionWrapper) arg1
              .getMonitoredVehicleJourney().getMonitoredCall()
              .getExtensions().getAny();
          return wrapper0
              .getDistances()
              .getDistanceFromCall()
              .compareTo(
                  wrapper1.getDistances()
                      .getDistanceFromCall());
        } catch (Exception e) {
          return -1;
        }
      }
    });

    return output;
  }

  @Override
  public Map<Boolean, List<AnnotatedStopPointStructure>> getAnnotatedStopPointStructures(
      CoordinateBounds bounds,
      List<String> agencyIds,
      List<AgencyAndId> routeIds, 
      DetailLevel detailLevel, 
      long currentTime,
      Map<Filters, String> filters) {

    // Cache stops by route so we don't need to call the transit data service repeatedly for the same route
    Map<String, StopsForRouteBean> stopsForRouteCache = new HashMap<String, StopsForRouteBean>();

    // Store processed StopBean as AnnotatedStopPointStructure 
    List<AnnotatedStopPointStructure> annotatedStopPoints = new ArrayList<AnnotatedStopPointStructure>();

    // AnnotatedStopPointStructures List with hasUpcomingScheduledService
    Map<Boolean, List<AnnotatedStopPointStructure>> output = new HashMap<Boolean, List<AnnotatedStopPointStructure>>();

    String upcomingScheduledService = filters.get(Filters.UPCOMING_SCHEDULED_SERVICE);

    Boolean upcomingServiceAllStops = true;

    if(upcomingScheduledService != null && upcomingScheduledService.trim().equalsIgnoreCase("false")){
      upcomingServiceAllStops = false;
    }

    List<StopBean> stopBeans = getStopsForBounds(bounds);

    processAnnotatedStopPoints(agencyIds, routeIds,stopBeans,annotatedStopPoints, 
        filters, stopsForRouteCache, detailLevel, currentTime);

    output.put(upcomingServiceAllStops, annotatedStopPoints);

    return output;
  }

  @Override
  public Map<Boolean, List<AnnotatedStopPointStructure>> getAnnotatedStopPointStructures(
      List<String> agencyIds,
      List<AgencyAndId> routeIds, 
      DetailLevel detailLevel, 
      long currentTime,
      Map<Filters, String> filters) {

    // Cache stops by route so we don't need to call the transit data service repeatedly for the same route
    Map<String, StopsForRouteBean> stopsForRouteCache = new HashMap<String, StopsForRouteBean>();

    // Store processed StopBean as AnnotatedStopPointStructure 
    List<AnnotatedStopPointStructure> annotatedStopPoints = new ArrayList<AnnotatedStopPointStructure>();

    // AnnotatedStopPointStructures List with hasUpcomingScheduledService
    Map<Boolean, List<AnnotatedStopPointStructure>> output = new HashMap<Boolean, List<AnnotatedStopPointStructure>>();

    String upcomingScheduledService = filters.get(Filters.UPCOMING_SCHEDULED_SERVICE);

    Boolean upcomingServiceAllStops = true;

    if(upcomingScheduledService != null && upcomingScheduledService.trim().equalsIgnoreCase("false")){
      upcomingServiceAllStops = false;
    }

    for(AgencyAndId aid : routeIds){

      String routeId = AgencyAndId.convertToString(aid);

      StopsForRouteBean stopsForLineRef = _transitDataService.getStopsForRoute(routeId);

        processAnnotatedStopPoints(agencyIds, routeIds, stopsForLineRef.getStops(), annotatedStopPoints, filters, 
              stopsForRouteCache, detailLevel, currentTime);

    }

    output.put(upcomingServiceAllStops, annotatedStopPoints);
    return output;

  }

  @Override
  public Map<Boolean, List<AnnotatedLineStructure>> getAnnotatedLineStructures(
      List<String> agencyIds,
      List<AgencyAndId> routeIds, 
      DetailLevel detailLevel,
      long currentTime, 
      Map<Filters, String> filters) {

    // Store processed StopBean as AnnotatedStopPointStructure 
    List<AnnotatedLineStructure> annotatedLines = new ArrayList<AnnotatedLineStructure>();

    // AnnotatedStopPointStructures List with hasUpcomingScheduledService
    Map<Boolean, List<AnnotatedLineStructure>> output = new HashMap<Boolean, List<AnnotatedLineStructure>>();

    Boolean upcomingServiceAllStops = null; 

    for(AgencyAndId rteId : routeIds){

      String routeId = AgencyAndId.convertToString(rteId);

      RouteBean routeBean = _transitDataService.getRouteForId(routeId);

      // Filter By AgencyID
      if(routeBean.getAgency() == null || !agencyIds.contains(routeBean.getAgency().getId()))
        continue;

        AnnotatedLineStructure annotatedLineStructure = new AnnotatedLineStructure();

        RouteResult routeResult = getRouteResult(routeBean, filters);

        // Skip Routes with no stops
        if(routeResult.getDirections() == null || routeResult.getDirections().size() == 0)
          continue;

      boolean isValid = SiriSupportV2.fillAnnotatedLineStructure(
          annotatedLineStructure, 
          routeResult, 
          filters, 
          detailLevel, 
          currentTime);

      if(isValid)
        annotatedLines.add(annotatedLineStructure);
    }

    output.put(upcomingServiceAllStops, annotatedLines);
    return output;
  }

  @Override
  public Map<Boolean, List<AnnotatedLineStructure>> getAnnotatedLineStructures(
      List<String> agencyIds, CoordinateBounds bounds, DetailLevel detailLevel,
      long responseTimestamp, Map<Filters, String> filters) {

    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();

    for(RouteBean route : getRoutesForBounds(bounds)){
      routeIds.add(AgencyAndId.convertFromString(route.getId()));
    }

    return getAnnotatedLineStructures(agencyIds, routeIds, detailLevel, responseTimestamp, filters);
  }

  /**
   * CURRENT IN-SERVICE VEHICLE STATUS FOR ROUTE
   */

  /**
   * Returns true if there are vehicles in service for given route+direction
   */
  @Override
  public boolean getVehiclesInServiceForRoute(String routeId,
      String directionId, long currentTime) {
    ListBean<TripDetailsBean> trips = getAllTripsForRoute(routeId,
        currentTime);
    for (TripDetailsBean tripDetails : trips.getList()) {
      if(tripDetails == null) continue;
      // filter out interlined routes
      if (routeId != null
          && !tripDetails.getTrip().getRoute().getId()
              .equals(routeId))
        continue;

      // filtered out by user
      if (directionId != null
          && !tripDetails.getTrip().getDirectionId()
              .equals(directionId))
        continue;

      if (!_presentationService.include(tripDetails.getStatus()))
        continue;

      return true;
    }

    return false;
  }

  /**
   * Returns true if there are vehicles in service for given route+direction
   * that will stop at the indicated stop in the future.
   */
  /* These methods were used in nyc branch.  Not used here so far.
  @Override
  public boolean getVehiclesInServiceForStopAndRoute(String stopId,
      String routeId, long currentTime) {
    for (ArrivalAndDepartureBean adBean : getArrivalsAndDeparturesForStop(
        stopId, currentTime)) {
      TripStatusBean statusBean = adBean.getTripStatus();
      if (!_presentationService.include(statusBean)
          || !_presentationService.include(adBean, statusBean))
        continue;

      // filtered out by user
      if (routeId != null
          && !adBean.getTrip().getRoute().getId().equals(routeId))
        continue;

      return true;
    }

    return false;
  }
*/
  /**
   * SERVICE ALERTS METHODS
   */
/*
  @Override
  public List<ServiceAlertBean> getServiceAlertsForRoute(String routeId) {
    return getServiceAlertsForRouteAndDirection(routeId, null);
  }

  @Override
  public List<ServiceAlertBean> getServiceAlertsForRouteAndDirection(
      String routeId, String directionId) {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);

    affects.setRouteId(routeId);
    if (directionId != null) {
      affects.setDirectionId(directionId);
    } else {
    */
      /*
       * TODO The route index is not currently being populated correctly;
       * query by route and direction, and supply both directions if not
       * present
       */
  /*
      SituationQueryBean.AffectsBean affects1 = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects1);
      affects1.setRouteId(routeId);
      affects1.setDirectionId("0");
      SituationQueryBean.AffectsBean affects2 = new SituationQueryBean.AffectsBean();
      query.getAffects().add(affects2);
      affects2.setRouteId(routeId);
      affects2.setDirectionId("1");
    }

    ListBean<ServiceAlertBean> serviceAlerts = _nycTransitDataService
        .getServiceAlerts(query);
    return serviceAlerts.getList();
  }
*/
  @Override
  public List<ServiceAlertBean> getServiceAlertsGlobal() {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();

    affects.setAgencyId("__ALL_OPERATORS__");
    query.getAffects().add(affects);

    ListBean<ServiceAlertBean> serviceAlerts = _transitDataService
        .getServiceAlerts(query);
    return serviceAlerts.getList();
  }

  /**
   * PRIVATE METHODS
   */

  private ListBean<TripDetailsBean> getAllTripsForRoute(String routeId,
      long currentTime) {
    TripsForRouteQueryBean tripRouteQueryBean = new TripsForRouteQueryBean();
    tripRouteQueryBean.setRouteId(routeId);
    tripRouteQueryBean.setTime(currentTime);

    TripDetailsInclusionBean inclusionBean = new TripDetailsInclusionBean();
    inclusionBean.setIncludeTripBean(true);
    inclusionBean.setIncludeTripStatus(true);
    tripRouteQueryBean.setInclusion(inclusionBean);

    return _transitDataService.getTripsForRoute(tripRouteQueryBean);
  }

  private List<ArrivalAndDepartureBean> getArrivalsAndDeparturesForStop(
      String stopId, long currentTime) {
    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    query.setTime(currentTime);
    query.setMinutesBefore(5 * 60);
    query.setMinutesAfter(5 * 60);

    StopWithArrivalsAndDeparturesBean stopWithArrivalsAndDepartures = _transitDataService
        .getStopWithArrivalsAndDepartures(stopId, query);

    return stopWithArrivalsAndDepartures.getArrivalsAndDepartures();
  }

  private List<StopBean> getStopsForBounds(CoordinateBounds bounds) {
    if (bounds != null) {
      SearchQueryBean queryBean = new SearchQueryBean();
      queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
      queryBean.setBounds(bounds);
      queryBean.setMaxCount(Integer.MAX_VALUE);

      StopsBean stops = _transitDataService.getStops(queryBean);
      return stops.getStops();
    }
    return new ArrayList<StopBean>();
  }

  private List<RouteBean> getRoutesForBounds(CoordinateBounds bounds) {
    if (bounds != null) {
      SearchQueryBean queryBean = new SearchQueryBean();
      queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
      queryBean.setBounds(bounds);
      queryBean.setMaxCount(Integer.MAX_VALUE);

      RoutesBean routes = _transitDataService.getRoutes(queryBean);
      return routes.getRoutes();
    }
    return new ArrayList<RouteBean>();
  }

  private void processAnnotatedStopPoints(
      List<String> agencyIds,
      List<AgencyAndId> routeIds,
      List<StopBean> stopBeans, 
      List<AnnotatedStopPointStructure> annotatedStopPoints, 
      Map<Filters, String> filters,
      Map<String, StopsForRouteBean> stopsForRouteCache,
      DetailLevel detailLevel,
      long currentTime
      ) {

    for (StopBean stopBean : stopBeans) {

      List<StopsForRouteBean> stopsForRouteList = new ArrayList<StopsForRouteBean>();

      boolean filterByLineRef = (routeIds != null && routeIds.size() > 0) ? true : false;
      boolean containsLineRef = false;

      // Get a list of all the routes for the stop
      for (RouteBean route : stopBean.getRoutes()) {

        // Filter By AgencyID
        if(route.getAgency() == null || !agencyIds.contains(route.getAgency().getId()))
          continue;

        // Add list of stops retreived from route to cache
        StopsForRouteBean stopsForRoute = stopsForRouteCache.get(route.getId());
        if (stopsForRoute == null) {
          stopsForRoute = _transitDataService.getStopsForRoute(route.getId());
          stopsForRouteCache.put(route.getId(), stopsForRoute);
        }

        if(stopsForRoute != null)
          stopsForRouteList.add(stopsForRoute);

        if(filterByLineRef && routeIds.contains(AgencyAndIdLibrary.convertFromString(route.getId())))
          containsLineRef = true;
      }

      // Filter By LineRef
      if(filterByLineRef && !containsLineRef)
        continue;

      // Get Stops with List of Routes, Direction, and Upcoming Service Info
      StopRouteDirection stopRouteDirection = getStopRouteDirection(stopBean, stopsForRouteList, filters);

      // Skip if No Route Directions Found
      if(stopRouteDirection == null)
        continue;

      // Used to filter stops that don't have any routes that match hasUpcomingScheduledStop
      if(stopRouteDirection.getRouteDirections() == null || stopRouteDirection.getRouteDirections().size() == 0)
        continue;

      AnnotatedStopPointStructure annotatedStopPoint = new AnnotatedStopPointStructure();

      boolean isValid = SiriSupportV2.fillAnnotatedStopPointStructure(annotatedStopPoint,
          stopRouteDirection, filters, detailLevel, currentTime);

      if(isValid)
        annotatedStopPoints.add(annotatedStopPoint);
    }

  }

  private RouteResult getRouteResult(
      RouteBean routeBean,
      Map<Filters, String> filters){

    List<RouteDirection> directions = new ArrayList<RouteDirection>();
      StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());

      // Filter Values
    String directionIdFilter = filters.get(Filters.DIRECTION_REF);
    String upcomingScheduledServiceFilter = filters.get(Filters.UPCOMING_SCHEDULED_SERVICE);

      // create stop ID->stop bean map
      Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
      for (StopBean stopBean : stopsForRoute.getStops()) {
        stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
      }

      List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
      for (StopGroupingBean stopGroupingBean : stopGroupings) {
        for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
          NameBean name = stopGroupBean.getName();
          String type = name.getType();
          String directionId = stopGroupBean.getId();

          // Destination and DirectionId Filter
      if(!type.equals("destination") || !SiriSupportV2.passFilter(directionId, directionIdFilter))
        continue;

          List<String> polylines = new ArrayList<String>();
          for(EncodedPolylineBean polyline : stopGroupBean.getPolylines()) {
            polylines.add(polyline.getPoints());
          }

          // TODO - Re-evaluate the best method to determine upcoming scheduled service
          Boolean routeHasUpcomingScheduledService = 
              _transitDataService.routeHasUpcomingScheduledService((routeBean.getAgency()!=null?routeBean.getAgency().getId():null), SystemTime.currentTimeMillis(), routeBean.getId(), directionId);

          // if there are buses on route, always have "scheduled service"
          Boolean routeHasVehiclesInService = 
              getVehiclesInServiceForRoute(routeBean.getId(), directionId, SystemTime.currentTimeMillis());

          if(routeHasVehiclesInService) {
            routeHasUpcomingScheduledService = true;
          }

          String hasUpcomingScheduledServiceVal = String.valueOf(routeHasUpcomingScheduledService);
          
          //String hasUpcomingScheduledServiceVal = String.valueOf(routeHasVehiclesInService);
          if(!SiriSupportV2.passFilter(hasUpcomingScheduledServiceVal,upcomingScheduledServiceFilter) 
              || routeHasUpcomingScheduledService == null || !routeHasUpcomingScheduledService)
            continue;

          // stops in this direction
          List<StopOnRoute> stopsOnRoute = null;
          if (!stopGroupBean.getStopIds().isEmpty()) {
            stopsOnRoute = new ArrayList<StopOnRoute>();

            for (String stopId : stopGroupBean.getStopIds()) {
              // service in this direction
              StopBean stopBean = stopIdToStopBeanMap.get(stopId);
              
              Boolean stopHasUpcomingScheduledService = _transitDataService.stopHasUpcomingScheduledService(
                  (routeBean.getAgency()!=null?routeBean.getAgency().getId():null),
                    SystemTime.currentTimeMillis(), stopBean.getId(), routeBean.getId(),
                    stopGroupBean.getId());  
              stopsOnRoute.add(new StopOnRoute(stopBean, stopHasUpcomingScheduledService));
            }
          }
          directions.add(new RouteDirection(stopGroupBean, polylines, stopsOnRoute, routeHasUpcomingScheduledService));
        }
      }

      return new RouteResult(routeBean, directions);

  }

  private StopRouteDirection getStopRouteDirection(
      StopBean stop,
      List<StopsForRouteBean> stopsForRouteList,
      Map<Filters, String> filters){

    // Filter Values
    String upcomingScheduledServiceFilter = filters.get(Filters.UPCOMING_SCHEDULED_SERVICE);
    String directionIdFilter = filters.get(Filters.DIRECTION_REF);

    StopRouteDirection stopRouteDirection = new StopRouteDirection(stop);

    for(StopsForRouteBean stopsForRoute : stopsForRouteList)
      // Check to see which stop group the specified stop exists in (usually 2 stop groups)
      for (StopGroupingBean stopGrouping : stopsForRoute.getStopGroupings()) {
        for (StopGroupBean stopGroup : stopGrouping.getStopGroups()) {
          NameBean name = stopGroup.getName();
              String type = name.getType();
          String directionId = stopGroup.getId();
          RouteBean route = stopsForRoute.getRoute();

          // Destination and DirectionId Filter
          if(!type.equals("destination") || !SiriSupportV2.passFilter(directionId, directionIdFilter))
            continue;

           // filter out route directions that don't stop at this stop
          if (!stopGroup.getStopIds().contains(stop.getId()))
                  continue;

          // filter hasUpcomingScheduledService
          Boolean hasUpcomingScheduledService = _transitDataService
              .stopHasUpcomingScheduledService((route
                  .getAgency() != null ? route
                  .getAgency().getId() : null), 
              SystemTime.currentTimeMillis(), 
              stop.getId(), 
              stopsForRoute.getRoute().getId(), 
              directionId
          );

          String hasUpcomingScheduledServiceVal = String.valueOf(hasUpcomingScheduledService);

          if(!hasUpcomingScheduledServiceVal.trim().equals("false")){
            hasUpcomingScheduledServiceVal = "true";
          }

          if(!SiriSupportV2.passFilter(hasUpcomingScheduledServiceVal,upcomingScheduledServiceFilter))
            continue;

          stopRouteDirection.addRouteDirection(new RouteForDirection(route.getId(), directionId, hasUpcomingScheduledService));
        } 
      }

    return stopRouteDirection;
  }

  private boolean useApc(){
    // cache this value as its called frequently
    if (_useApc == null)
      _useApc = _configurationService.getConfigurationValueAsBoolean("tds.useApc", Boolean.TRUE);
    return _useApc;
  }

}
