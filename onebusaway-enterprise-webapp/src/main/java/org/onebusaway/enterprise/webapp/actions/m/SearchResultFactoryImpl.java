/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.m;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.presentation.impl.realtime.SiriSupport;
import org.onebusaway.presentation.impl.search.AbstractSearchResultFactoryImpl;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.IntervalFactory;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.util.OneBusAwayFormats;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.enterprise.webapp.actions.m.model.GeocodeResult;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteAtStop;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteDirection;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteInRegionResult;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteResult;
import org.onebusaway.enterprise.webapp.actions.m.model.StopOnRoute;
import org.onebusaway.enterprise.webapp.actions.m.model.StopResult;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.NaturalLanguageStringStructure;
import uk.org.siri.siri.OccupancyEnumeration;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchResultFactoryImpl extends AbstractSearchResultFactoryImpl implements SearchResultFactory {

  private ConfigurationService _configurationService;

  private RealtimeService _realtimeService;

  private TransitDataService _transitDataService;

  private IntervalFactory _factory;

  private Integer _staleTimeout = null;
  private Boolean _serviceDateFilter = null;
  private String _apcMode = null;
  private Map<String, String> occupancyStatusEnumToDisplay = new HashMap<>();

  boolean debug = false;

  public SearchResultFactoryImpl(TransitDataService transitDataService,
                                 RealtimeService realtimeService, ConfigurationService configurationService,
                                 IntervalFactory factory) {
    _transitDataService = transitDataService;
    _realtimeService = realtimeService;
    _configurationService = configurationService;
    _factory = factory;
  }

  @Override
  public SearchResult getRouteResultForRegion(RouteBean routeBean) {
    return new RouteInRegionResult(routeBean);
  }

  @Override
  public SearchResult getRouteResult(RouteBean routeBean) {
    List<RouteDirection> directions = new ArrayList<RouteDirection>();

    AgencyServiceInterval serviceInterval = null;
      boolean serviceDateFilterOn = getServiceDateFilter();
      if (serviceDateFilterOn) serviceInterval = _factory.constructForDate(new Date(SystemTime.currentTimeMillis()));

      StopsForRouteBean stopsForRoute;
      if (serviceInterval == null)
          stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
      else
          stopsForRoute = _transitDataService.getStopsForRouteForServiceInterval(routeBean.getId(), serviceInterval);

    // create stop ID->stop bean map
    Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
    for (StopBean stopBean : stopsForRoute.getStops()) {
      stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
    }

    // add stops in both directions
    
    List<VehicleActivityStructure> journeyList = _realtimeService.getVehicleActivityForRoute(
        routeBean.getId(), null, 0, SystemTime.currentTimeMillis(), false);

    Map<String, List<String>> stopIdToDistanceAwayStringMap = new HashMap<String, List<String>>();
    Map<String, List<String>> stopIdToVehicleIdMap = new HashMap<String, List<String>>();
    Map<String, Boolean> stopIdToRealtimeDataMap = new HashMap<String, Boolean>();
    
    // build map of stop IDs to list of distance strings
    for (VehicleActivityStructure journey : journeyList) {
      // on detour?
      MonitoredCallStructure monitoredCall = journey.getMonitoredVehicleJourney().getMonitoredCall();
      if (monitoredCall == null) {
        continue;
      }

      String stopId = monitoredCall.getStopPointRef().getValue();
      
      fillDistanceAwayStringsList(journey.getMonitoredVehicleJourney(),journey.getRecordedAtTime(), stopId, stopIdToDistanceAwayStringMap);
      fillVehicleIdsStringList(journey.getMonitoredVehicleJourney(), journey.getRecordedAtTime(), stopId, stopIdToVehicleIdMap);
      fillRealtimeData(journey.getMonitoredVehicleJourney(), stopId, stopIdToRealtimeDataMap);
    }

    // Service Alerts for Route + Stop combinations
    Set<ServiceAlertBean> serviceAlertBeansRouteStop = new HashSet<>();

    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGroupingBean : stopGroupings) {
      for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
        NameBean name = stopGroupBean.getName();
        String type = name.getType();

        if (!type.equals("destination"))
          continue;

        // service in this direction
        Boolean hasUpcomingScheduledService = _transitDataService.routeHasUpcomingScheduledService(
        	(routeBean.getAgency()!=null?routeBean.getAgency().getId():null),	
            SystemTime.currentTimeMillis(), routeBean.getId(),
            stopGroupBean.getId());

        // if there are buses on route, always have "scheduled service"
        Boolean routeHasVehiclesInService = 
      		  _realtimeService.getVehiclesInServiceForRoute(routeBean.getId(), stopGroupBean.getId(), SystemTime.currentTimeMillis());

        if(routeHasVehiclesInService) {
      	  hasUpcomingScheduledService = true;
        }

        // stops in this direction
        List<StopOnRoute> stopsOnRoute = null;
        if (!stopGroupBean.getStopIds().isEmpty()) {
          stopsOnRoute = new ArrayList<StopOnRoute>();

          for (String stopId : stopGroupBean.getStopIds()) {
              List<ServiceAlertBean> routeStops = _realtimeService.getServiceAlertsForRouteAndStop(routeBean.getId(), stopId);
              if (!routeStops.isEmpty()) serviceAlertBeansRouteStop.addAll(routeStops);
            if (_transitDataService.stopHasRevenueServiceOnRoute((routeBean.getAgency()!=null?routeBean.getAgency().getId():null),
                    stopId, routeBean.getId(), stopGroupBean.getId())) {
              stopsOnRoute.add(new StopOnRoute(stopIdToStopBeanMap.get(stopId),
                  stopIdToDistanceAwayStringMap.get(stopId), stopIdToRealtimeDataMap.get(stopId), stopIdToVehicleIdMap.get(stopId)));
            }
          }
        }
        
        directions.add(new RouteDirection(stopGroupBean.getName().getName(), stopGroupBean, stopsOnRoute,
            hasUpcomingScheduledService, null));
      }
    }

    // service alerts in this direction
    Set<String> serviceAlertDescriptions = new HashSet<String>();

    //include both agency level and route level alerts
    List<ServiceAlertBean> serviceAlertBeansRoute = _realtimeService.getServiceAlertsForRoute(routeBean.getId());

    List<ServiceAlertBean> serviceAlertBeans = new ArrayList<ServiceAlertBean>();
    serviceAlertBeans.addAll(new ArrayList<>(serviceAlertBeansRouteStop));
    serviceAlertBeans.addAll(serviceAlertBeansRoute);
    populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans);

    serviceAlertBeans = new ArrayList<ServiceAlertBean>();

    if (routeBean.getAgency() != null) {
        List<ServiceAlertBean> serviceAlertBeansAgency = _realtimeService.getServiceAlertsForAgency(routeBean.getAgency().getId());
        serviceAlertBeans.addAll(serviceAlertBeansAgency);
    }
    populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans);

    return new RouteResult(routeBean, directions, serviceAlertDescriptions);
  }


  @Override
  public SearchResult getStopResult(StopBean stopBean, Set<RouteBean> routeFilter) {
    List<RouteAtStop> routesWithArrivals = new ArrayList<RouteAtStop>();
    List<RouteAtStop> routesWithNoVehiclesEnRoute = new ArrayList<RouteAtStop>();
    List<RouteAtStop> routesWithNoScheduledService = new ArrayList<RouteAtStop>();
    List<RouteBean> filteredRoutes = new ArrayList<RouteBean>();

    Set<String> serviceAlertDescriptions = new HashSet<String>();
    // stop visits -- we pull this out of the loop as it doesn't change
    List<MonitoredStopVisitStructure> visitList = _realtimeService.getMonitoredStopVisitsForStop(
            stopBean.getId(), 0, SystemTime.currentTimeMillis());


    for (RouteBean routeBean : stopBean.getRoutes()) {
      if (routeFilter != null && !routeFilter.isEmpty()
              && !routeFilter.contains(routeBean)) {
        filteredRoutes.add(routeBean);
        continue;
      }

      AgencyServiceInterval serviceInterval = null;
      boolean serviceDateFilterOn = getServiceDateFilter();
      if (serviceDateFilterOn) serviceInterval = _factory.constructForDate(new Date(SystemTime.currentTimeMillis()));

      StopsForRouteBean stopsForRoute;
      if (serviceInterval != null) {
        stopsForRoute = _transitDataService.getStopsForRouteForServiceInterval(routeBean.getId(), serviceInterval);
      } else {
        stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
      }

      List<RouteDirection> directions = new ArrayList<RouteDirection>();
      List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
      for (StopGroupingBean stopGroupingBean : stopGroupings) {
        for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
          NameBean name = stopGroupBean.getName();
          String type = name.getType();

          if (!type.equals("destination"))
            continue;

          // filter out route directions that don't stop at this stop
          if (!stopGroupBean.getStopIds().contains(stopBean.getId()))
            continue;

          // arrivals in this direction
          Map<String, List<StopOnRoute>> arrivalsForRouteAndDirection = getDisplayStringsByHeadsignForStopAndRouteAndDirection(
                  stopBean, routeBean, stopGroupBean, visitList);

          // service alerts for this route + direction
          List<ServiceAlertBean> serviceAlertBeans = _realtimeService.getServiceAlertsForRouteAndDirection(
                  routeBean.getId(), stopGroupBean.getId());
          populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans);

          // also include service alerts for route + stop
          serviceAlertBeans = _realtimeService.getServiceAlertsForRouteAndStop(
                  routeBean.getId(), stopBean.getId());
          populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans);

          // service in this direction
          Boolean hasUpcomingScheduledService = _transitDataService.stopHasUpcomingScheduledService(
                  (routeBean.getAgency() != null ? routeBean.getAgency().getId() : null),
                  SystemTime.currentTimeMillis(), stopBean.getId(), routeBean.getId(),
                  stopGroupBean.getId());

          // if there are buses on route, always have "scheduled service"
          if (!arrivalsForRouteAndDirection.isEmpty()) {
            hasUpcomingScheduledService = true;
          }


          if (arrivalsForRouteAndDirection.isEmpty()) {
            directions.add(new RouteDirection(stopGroupBean.getName().getName(), stopGroupBean, Collections.<StopOnRoute>emptyList(),
                    hasUpcomingScheduledService, Collections.<String>emptyList()));
          } else {
            for (Map.Entry<String, List<StopOnRoute>> entry : arrivalsForRouteAndDirection.entrySet()) {
              directions.add(new RouteDirection(entry.getKey(), stopGroupBean, entry.getValue(),
                      hasUpcomingScheduledService, Collections.<String>emptyList()));
            }
          }
        }
      }

      // For each direction, determine whether the route has no service, has no vehicles,
      // or has service with vehicles en route. Add RouteAtStop object to appropriate collection.
      // Now one RouteAtStop object exists for each direction for each route.
      for (RouteDirection direction : directions) {
        List<RouteDirection> directionList = Collections.<RouteDirection>singletonList(direction);

        RouteAtStop routeAtStop = new RouteAtStop(routeBean, directionList, serviceAlertDescriptions);

        if (!direction.getStops().isEmpty())
          routesWithArrivals.add(routeAtStop);
        else if (Boolean.FALSE.equals(direction.getHasUpcomingScheduledService()))
          routesWithNoScheduledService.add(routeAtStop);
        else
          routesWithNoVehiclesEnRoute.add(routeAtStop);
      }
    }

    // add stop level service alerts
    List<ServiceAlertBean> stopServiceAlertBeans = getServiceAlertsForStop(stopBean.getId());
    populateServiceAlerts(serviceAlertDescriptions, stopServiceAlertBeans);

    return new StopResult(stopBean, routesWithArrivals,
            routesWithNoVehiclesEnRoute, routesWithNoScheduledService, filteredRoutes, serviceAlertDescriptions);
  }

    private List<ServiceAlertBean> getServiceAlertsForStop(String stopId) {
        SituationQueryBean query = new SituationQueryBean();
        query.setTime(System.currentTimeMillis());
        SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
        query.getAffects().add(affects);
        affects.setStopId(stopId);
        ListBean<ServiceAlertBean> alerts = _transitDataService.getServiceAlerts(query);

        if (alerts != null) {
            return alerts.getList();
        }

        return Collections.emptyList();
    }

  @Override
  public SearchResult getGeocoderResult(EnterpriseGeocoderResult geocodeResult, Set<RouteBean> routeFilter) {
    return new GeocodeResult(geocodeResult);
  }

  // stop view
  private Map<String, List<StopOnRoute>> getDisplayStringsByHeadsignForStopAndRouteAndDirection(
      StopBean stopBean, RouteBean routeBean, StopGroupBean stopGroupBean, List<MonitoredStopVisitStructure> visitList) {
    
    Map<String, List<StopOnRoute>> results = new HashMap<String, List<StopOnRoute>>();

    Boolean showApc = _realtimeService.showApc();

    for (MonitoredStopVisitStructure visit : visitList) {
      String routeId = visit.getMonitoredVehicleJourney().getLineRef().getValue();
      if (!routeBean.getId().equals(routeId))
        continue;

      String directionId = visit.getMonitoredVehicleJourney().getDirectionRef().getValue();
      if (directionId != null && !stopGroupBean.getId().equals(directionId)) 
        continue;

      // on detour?
      MonitoredCallStructure monitoredCall = visit.getMonitoredVehicleJourney().getMonitoredCall();
      if (monitoredCall == null)
        continue;

      if (!results.containsKey(visit.getMonitoredVehicleJourney().getDestinationName().getValue()))
        results.put(visit.getMonitoredVehicleJourney().getDestinationName().getValue(), new ArrayList<StopOnRoute>());
      
      if(results.get(visit.getMonitoredVehicleJourney().getDestinationName().getValue()).size() >= 3)
        continue;
      
      String distance = getPresentableDistance(visit.getMonitoredVehicleJourney(),
    		visit.getRecordedAtTime().getTime(), true);

      String timePrediction = getPresentableTime(visit.getMonitoredVehicleJourney(),
    	 	visit.getRecordedAtTime().getTime(), true);
      
      String vehicleId = null;
      if (visit.getMonitoredVehicleJourney() != null && visit.getMonitoredVehicleJourney().getVehicleRef() != null) {
        vehicleId = visit.getMonitoredVehicleJourney().getVehicleRef().getValue();
      } else {
        vehicleId = "N/A"; // insert an empty element so it aligns with distanceAways
      }

      String loadOccupancy = null;
      if (visit.getMonitoredVehicleJourney() != null) {
        loadOccupancy = getPresentableOccupancy(visit.getMonitoredVehicleJourney(), visit.getRecordedAtTime().getTime());
      }

      List<String> distanceAways = new ArrayList<String>();
      List<String> vehicleIds = new ArrayList<String>();
      if (vehicleId.contains("_")) vehicleId = vehicleId.split("_")[1];
      vehicleIds.add(vehicleId);

      String qualifierText = getQualifierText(visit.getMonitoredVehicleJourney(), visit.getRecordedAtTime().getTime(), true);

      // new format: timePrediction \t icon+vehicleId \t weeble/occupancy \t distanceAways
      String columms = "";
      // column 1: prediction
      if (isNotBlank(timePrediction)) {
        if (isActiveTrip(visit.getMonitoredVehicleJourney())) {
          if (visit.getMonitoredVehicleJourney().isMonitored()) {
            columms += openSpan("predictionContainer", "activeArrivalPrediction") + timePrediction + closeSpan();
          } else {
            columms += openSpan("predictionContainer", "futureArrivalPrediction") + timePrediction + closeSpan();
          }
        } else {
          columms += openSpan("predictionContainer","futureArrivalPrediction") + timePrediction + closeSpan();
        }
      } else {
        columms += "schedule";
      }
      // column 2: vehicleId
      if (visit.getMonitoredVehicleJourney().isMonitored()
              && isNotBlank(vehicleId))
        columms+= openSpan("vehicleIdContainer", "vehicleArrival") + vehicleId + closeSpan();
      else {
        columms+= openSpan("vehicleIdContainer", "vehicleScheduledArrival") + "scheduled" + closeSpan();
      }
      // column 3: occupancy
      if (isNotBlank(loadOccupancy))
        columms+= loadOccupancy;
      else
        columms += emptyDiv("apcLadderContainer");
      // column 4: distance + qualifier
      if (isNotBlank(distance)) {
        if (isNotBlank(qualifierText))
          distance += qualifierText;
        columms += openSpan("distanceContainer", "distanceArrival") + distance + closeSpan();
      } else {
        // striving for parity in layout
        columms += openSpan("distanceContainer", "distanceArrival") + closeSpan();
      }
      distanceAways.add(columms);

      results.get(visit.getMonitoredVehicleJourney().getDestinationName().getValue()).add(
    		  		new StopOnRoute(stopBean,distanceAways, visit.getMonitoredVehicleJourney().isMonitored(), vehicleIds));
    }

    return results;
  }

  private boolean isActiveTrip(MonitoredVehicleJourneyStructure journey) {
    if (journey != null) {
      NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();
      if (progressStatus != null && progressStatus.getValue().contains("prevTrip")) {
        return false;
      } else {
        return true;
      }
    }
    return false; // we are not active if journey is null
  }

  private String openSpan(String spanClass) {
    return openSpan(null, spanClass);
  }
  private String openSpan(String divClass, String spanClass) {
    if (divClass == null)
      return "<div class='arrivalDepartureContainer'>" +
              "<span class=\"" + spanClass + "\">";
    return "<div class='" + divClass + "'>" +
            "<span class=\"" + spanClass + "\">";
  }
  private String closeSpan() {
    return "</span>"+  "</div>";
  }
  private String emptyDiv(String divClass) {
    return "<div class='" + divClass + "'><span class='weeble0'></span></div>";
  }

  private String getPresentableOccupancy(MonitoredVehicleJourneyStructure journey, long updateTime) {
    // if data is old, no occupancy
    int staleTimeout = getStaleTimeout();
    long age = (System.currentTimeMillis() - updateTime) / 1000;
    if (age > staleTimeout) {
//      System.out.println("tossing record " + journey.getVehicleRef().getValue()
//              + " with age " + age + "s old");
      return null;
    }
    if (!journey.isMonitored() || !isActiveTrip(journey))
      return null;

    String apcMode = getApcMode();

    String occupancyStr = "";
    if (apcMode != null) {
        occupancyStr = getApcModeOccupancy(journey, showIconsForOccupancy());
    }
    return occupancyStr;
  }

  private boolean showIconsForOccupancy() {
    return _configurationService.getConfigurationValueAsBoolean("display.apcIcons", true);
  }


  private String getApcModeOccupancy(MonitoredVehicleJourneyStructure journey, boolean showIcons) {

    if (journey.getOccupancy() != null) {
      String stylePrefix = "apcDot";
      if (!showIcons) {
        stylePrefix = "weeble";
      }
      String loadOccupancy = journey.getOccupancy().toString();
      loadOccupancy = loadOccupancy.toUpperCase();

      if (loadOccupancy.equals(OccupancyEnumeration.SEATS_AVAILABLE.name()) || loadOccupancy.equals(OccupancyStatus.MANY_SEATS_AVAILABLE.name())) {
        String occupancyText = getOccupancyString(OccupancyEnumeration.SEATS_AVAILABLE.name());
        loadOccupancy = "<div class='apcLadderContainer' title='" + occupancyText + "' aria-label='" + occupancyText + "'><span class='" + stylePrefix + "G'></span>";
        if (showIcons)
          loadOccupancy += "<span class='apcTextG'>" + occupancyText + "</span>";
        loadOccupancy += "</div>";
      } else if (loadOccupancy.equals(OccupancyEnumeration.STANDING_AVAILABLE.name()) || loadOccupancy.equals(OccupancyStatus.FEW_SEATS_AVAILABLE.name())) {
        String occupancyText = getOccupancyString(OccupancyEnumeration.STANDING_AVAILABLE.name());
        loadOccupancy = "<div class='apcLadderContainer' title='" + occupancyText + "' aria-label='" + occupancyText + "'><span class='" + stylePrefix + "Y'></span>";
        if (showIcons)
          loadOccupancy += "<span class='apcTextY'>" + getOccupancyString(OccupancyEnumeration.STANDING_AVAILABLE.name()) + "</span></div>";
        loadOccupancy += "</div>";
      } else if (loadOccupancy.equals(OccupancyEnumeration.FULL.name()) || loadOccupancy.equals(OccupancyStatus.FULL.name())) {
        String occupancyText = getOccupancyString(OccupancyEnumeration.FULL.name());
        loadOccupancy = "<div class='apcLadderContainer' title='" + occupancyText + "' aria-label='" + occupancyText + "'><span class='" + stylePrefix + "R'></span>";
        if (showIcons)
          loadOccupancy +="<span class='apcTextR'>" + getOccupancyString(OccupancyEnumeration.FULL.name()) + "</span></div>";
        loadOccupancy += "</div>";
      }

      return loadOccupancy;
    } else
      return "";
  }

  // allow for the occupancy strings to be overridden per local configuration
  // if not explicitly overridden, prefer GTFS-RT description to SIRI values
  private String getOccupancyString(String siriEnum) {
    String occupancyEnum = SiriSupport.mapSiriEnumToOccupancyStatus(siriEnum);
    String display = occupancyStatusEnumToDisplay.get(occupancyEnum);
    if (display == null) {
      display = _configurationService.getConfigurationValueAsString("display.occupancy." + siriEnum, prettyPrintOccupancyEnum(occupancyEnum));
      occupancyStatusEnumToDisplay.put(occupancyEnum, display);
    }

    return display;
  }


  private String prettyPrintOccupancyEnum(String loadOccupancy) {
    return OneBusAwayFormats.toPascalCaseWithSpaces(loadOccupancy);
  }

  private void fillDistanceAwayStringsList(
      MonitoredVehicleJourney mvj,
      Date recordedAtTime,
      String stopId,
      Map<String, List<String>> map) {
      
    // Distance Away
    List<String> distanceStrings = map.get(stopId);
    if (distanceStrings == null) {
      distanceStrings = new ArrayList<String>();
    }

    // getPresentableDistance doesn't include occupancy
    String distance = getPresentableDistance(
          mvj,
          recordedAtTime.getTime(), false);
    String occupancy = getPresentableOccupancy(mvj, recordedAtTime.getTime());
    if (occupancy != null)
      distance += occupancy;
    distanceStrings.add(distance);
    map.put(stopId, distanceStrings);
  }
  
  private void fillVehicleIdsStringList(
      MonitoredVehicleJourney mvj, Date recordedAtTime,
      String stopId, Map<String, List<String>> map) {

    List<String> vehicleIdStrings = map.get(stopId);
    if (vehicleIdStrings ==null) {
      vehicleIdStrings = new ArrayList<String>();
    }
    if (mvj != null && mvj.getVehicleRef() != null) {
      String id = mvj.getVehicleRef().getValue();
      if (id.contains("_")) id = id.split("_")[1];
      vehicleIdStrings.add(id);
    } else {
      vehicleIdStrings.add("N/A");
    }
  }

  
  private void fillRealtimeData(
		  MonitoredVehicleJourney mvj,
		  String stopId,
	     Map<String, Boolean> map) { 
	     
	  // Realtime Data Check for Stop
	  map.put(stopId, mvj.isMonitored());
  }

  private String getPresentableTime(
		  MonitoredVehicleJourneyStructure journey, long updateTime,
	      boolean isStopContext) {

	  MonitoredCallStructure monitoredCall = journey.getMonitoredCall();

	  if(!isStopContext) {
		  return null;
	  }
	  
	  int staleTimeout = getStaleTimeout();
	  long age = (SystemTime.currentTimeMillis() - updateTime) / 1000;

    String qualifierText = "";
	  if (age > staleTimeout) {
      qualifierText = " <b>(old data)</b>";
	  }
    String blockId = "";
    if (journey != null && journey.getBlockRef()!= null && journey.getBlockRef().getValue() != null) {
      blockId = journey.getBlockRef().getValue();
      blockId += ":" + journey.getFramedVehicleJourneyRef().getDatedVehicleJourneyRef();
    }

    if (isOriginTerminal(journey)) {
      // we show departures for origin terminal
      if (monitoredCall.getExpectedDepartureTime() != null) {
        if (debug) qualifierText += "DEPARTS(" + blockId + ") ";
        return getPresentableTime(journey, updateTime, monitoredCall.getExpectedDepartureTime().getTime(), qualifierText);
      }
    }
    if (monitoredCall.getExpectedArrivalTime() != null) {
      if (debug) qualifierText += "ARR(" + blockId + ") ";
      // otherwise show arrivals
      return getPresentableTime(journey, updateTime, monitoredCall.getExpectedArrivalTime().getTime(), qualifierText);
    }

	  return null;
  }

  private String getPresentableTime(MonitoredVehicleJourneyStructure journey, long updateTime, long arrivalOrDeparture, String qualifierText) {
    if (qualifierText == null)
      qualifierText = "";

    double minutes = Math.floor((arrivalOrDeparture - updateTime) / 60 / 1000);
    String timeString = "<strong>" + Math.round(minutes) + "</strong>"
            + " minute" + ((Math.abs(minutes) != 1) ? "s" : "" );
    if (debug) {
      SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
      timeString += ", " + sdf.format(new Date(arrivalOrDeparture));
    }

    return timeString + qualifierText;
  }

  private boolean isOriginTerminal(MonitoredVehicleJourneyStructure journey) {
    if (journey == null
            || journey.getOriginRef() == null
            || journey.getDestinationRef() == null
            || journey.getMonitoredCall() == null) {
      return false;
    }
    String origin = journey.getOriginRef().getValue();
    String currentStop = journey.getMonitoredCall().getStopPointRef().getValue();
    if (currentStop == null)
      return false;
    return (currentStop.equals(origin));
  }

  private String getQualifierText(MonitoredVehicleJourneyStructure journey, long updateTime,
                                  boolean isStopContext) {
    MonitoredCallStructure monitoredCall = journey.getMonitoredCall();
    SiriExtensionWrapper wrapper = (SiriExtensionWrapper) monitoredCall.getExtensions().getAny();
    SiriDistanceExtension distanceExtension = wrapper.getDistances();
    String message = "";
    NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();

    if (!isActiveTrip(journey)) {
      // don't comment on non-active trips
      return message;
    }

    // at terminal label only appears in stop results
    if (isStopContext && progressStatus != null
            && progressStatus.getValue().contains("layover")) {

      if(journey.getOriginAimedDepartureTime() != null) {
        DateFormat formatter = DateFormat.getTimeInstance(DateFormat.SHORT);

        if(journey.getOriginAimedDepartureTime().getTime() < new Date(SystemTime.currentTimeMillis()).getTime()) {
          message += "at terminal";
        } else {
          message += "at terminal, scheduled to depart " + formatter.format(journey.getOriginAimedDepartureTime());
        }
      } else {
        message += "at terminal";
      }
    } else if (!isActiveTrip(journey)) {
      message += " +layover";
    }

    int staleTimeout = getStaleTimeout();
    long age = (SystemTime.currentTimeMillis() - updateTime) / 1000;

    return message;
  }
  private String getPresentableDistance(
      MonitoredVehicleJourneyStructure journey, long updateTime,
      boolean isStopContext) {
    MonitoredCallStructure monitoredCall = journey.getMonitoredCall();
    SiriExtensionWrapper wrapper = (SiriExtensionWrapper) monitoredCall.getExtensions().getAny();
    SiriDistanceExtension distanceExtension = wrapper.getDistances();
    NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();
    String message = "";

    if (!isActiveTrip(journey)) {
      return message; /* don't comment on trips not active, results are not reliable */
    }
    if (!journey.isMonitored()) {
      return message;
    }
    String distance = distanceExtension.getPresentableDistance();
    if (!isStopContext) {
      // always considered active to give it treatment
      distance = "<strong>" + distance + "</strong>";
    }
    return distance;
  }

  private int getStaleTimeout() {
    if (_staleTimeout == null)
      _staleTimeout = _configurationService.getConfigurationValueAsInteger("display.staleTimeout", 120);
    return _staleTimeout;
  }

  private boolean getServiceDateFilter() {
    if (_serviceDateFilter == null)
      _serviceDateFilter =  _configurationService.getConfigurationValueAsBoolean("display.serviceDateFiltering", false);
    return _serviceDateFilter;
  }


  private String getApcMode() {
    if (_apcMode == null)
      _apcMode =_configurationService.getConfigurationValueAsString("display.apcMode", "OCCUPANCY");
    return _apcMode;
  }

}
