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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.presentation.impl.search.AbstractSearchResultFactoryImpl;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.siri.SiriDistanceExtension;
import org.onebusaway.transit_data_federation.siri.SiriExtensionWrapper;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.enterprise.webapp.actions.m.model.GeocodeResult;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteAtStop;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteDirection;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteInRegionResult;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteResult;
import org.onebusaway.enterprise.webapp.actions.m.model.StopOnRoute;
import org.onebusaway.enterprise.webapp.actions.m.model.StopResult;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;

import uk.org.siri.siri.MonitoredCallStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.NaturalLanguageStringStructure;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleActivityStructure.MonitoredVehicleJourney;

public class SearchResultFactoryImpl extends AbstractSearchResultFactoryImpl implements SearchResultFactory {

  private ConfigurationService _configurationService;

  private RealtimeService _realtimeService;

  private TransitDataService _transitDataService;

  public SearchResultFactoryImpl(TransitDataService transitDataService,
      RealtimeService realtimeService, ConfigurationService configurationService) {
    _transitDataService = transitDataService;
    _realtimeService = realtimeService;
    _configurationService = configurationService;
  }

  @Override
  public SearchResult getRouteResultForRegion(RouteBean routeBean) {
    return new RouteInRegionResult(routeBean);
  }

  @Override
  public SearchResult getRouteResult(RouteBean routeBean) {
    List<RouteDirection> directions = new ArrayList<RouteDirection>();

    StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());

    // create stop ID->stop bean map
    Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
    for (StopBean stopBean : stopsForRoute.getStops()) {
      stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
    }

    // add stops in both directions
    
    List<VehicleActivityStructure> journeyList = _realtimeService.getVehicleActivityForRoute(
        routeBean.getId(), null, 0, SystemTime.currentTimeMillis());

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

    List<ServiceAlertBean> serviceAlertBeans = _realtimeService.getServiceAlertsForRoute(routeBean.getId());
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

    for (RouteBean routeBean : stopBean.getRoutes()) {
      if (routeFilter != null && !routeFilter.isEmpty()
          && !routeFilter.contains(routeBean)) {
        filteredRoutes.add(routeBean);
        continue;
      }

      StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());

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
              stopBean, routeBean, stopGroupBean);

          // service alerts for this route + direction
          List<ServiceAlertBean> serviceAlertBeans = _realtimeService.getServiceAlertsForRouteAndDirection(
              routeBean.getId(), stopGroupBean.getId());
          populateServiceAlerts(serviceAlertDescriptions, serviceAlertBeans);
          
          // service in this direction
          Boolean hasUpcomingScheduledService = _transitDataService.stopHasUpcomingScheduledService(
        	  (routeBean.getAgency()!=null?routeBean.getAgency().getId():null),
              SystemTime.currentTimeMillis(), stopBean.getId(), routeBean.getId(),
              stopGroupBean.getId());

          // if there are buses on route, always have "scheduled service"
          if (!arrivalsForRouteAndDirection.isEmpty()) {
            hasUpcomingScheduledService = true;
          }

          
          if(arrivalsForRouteAndDirection.isEmpty()) {
            directions.add(new RouteDirection(stopGroupBean.getName().getName(), stopGroupBean, Collections.<StopOnRoute>emptyList(), 
                hasUpcomingScheduledService, Collections.<String>emptyList()));
          } else {
            for (Map.Entry<String,List<StopOnRoute>> entry : arrivalsForRouteAndDirection.entrySet()) {
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
    
    return new StopResult(stopBean, routesWithArrivals,
        routesWithNoVehiclesEnRoute, routesWithNoScheduledService, filteredRoutes, serviceAlertDescriptions);
  }

  @Override
  public SearchResult getGeocoderResult(EnterpriseGeocoderResult geocodeResult, Set<RouteBean> routeFilter) {
    return new GeocodeResult(geocodeResult);
  }

  // stop view
  private Map<String, List<StopOnRoute>> getDisplayStringsByHeadsignForStopAndRouteAndDirection(
      StopBean stopBean, RouteBean routeBean, StopGroupBean stopGroupBean) {
    
    Map<String, List<StopOnRoute>> results = new HashMap<String, List<StopOnRoute>>();

    // stop visits
    List<MonitoredStopVisitStructure> visitList = _realtimeService.getMonitoredStopVisitsForStop(
        stopBean.getId(), 0, SystemTime.currentTimeMillis());

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
      
      List<String> distanceAways = new ArrayList<String>();
      List<String> vehicleIds = new ArrayList<String>();
      if (vehicleId.contains("_")) vehicleId = vehicleId.split("_")[1];
      vehicleIds.add(vehicleId);
      if(timePrediction != null) {
    	  distanceAways.add(timePrediction);
      } else {
    	  distanceAways.add(distance);
      }
      
      results.get(visit.getMonitoredVehicleJourney().getDestinationName().getValue()).add(
    		  		new StopOnRoute(stopBean,distanceAways, visit.getMonitoredVehicleJourney().isMonitored(), vehicleIds));
    }

    return results;
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

      distanceStrings.add(getPresentableDistance(
    		  mvj,
    		  recordedAtTime.getTime(), false));
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

	  NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();
	  MonitoredCallStructure monitoredCall = journey.getMonitoredCall();
	  
	  if(!isStopContext) {
		  return null;
	  }
	  
	  int staleTimeout = _configurationService.getConfigurationValueAsInteger("display.staleTimeout", 120);
	  long age = (SystemTime.currentTimeMillis() - updateTime) / 1000;

	  if (age > staleTimeout) {
		  return null;
	  }
	  
	  if(monitoredCall.getExpectedArrivalTime() != null) {
		  long predictedArrival = monitoredCall.getExpectedArrivalTime().getTime();

		  SiriExtensionWrapper wrapper = (SiriExtensionWrapper) monitoredCall.getExtensions().getAny();
		  SiriDistanceExtension distanceExtension = wrapper.getDistances();
		  String distance = distanceExtension.getPresentableDistance();
		  
		  double minutes = Math.floor((predictedArrival - updateTime) / 60 / 1000);
		  String timeString = Math.round(minutes) + " minute" + ((Math.abs(minutes) != 1) ? "s" : "");
				  
		  if(progressStatus != null && progressStatus.getValue().contains("prevTrip")) {
		    	return timeString;
		  } else {
		    	return "<strong>" + timeString + "</strong>" + ", " + distance;
		  }
	  }
	  
	  return null;
  }	  
  
  private String getPresentableDistance(
      MonitoredVehicleJourneyStructure journey, long updateTime,
      boolean isStopContext) {
    MonitoredCallStructure monitoredCall = journey.getMonitoredCall();
    SiriExtensionWrapper wrapper = (SiriExtensionWrapper) monitoredCall.getExtensions().getAny();
    SiriDistanceExtension distanceExtension = wrapper.getDistances();

    String message = "";
    String distance = "<strong>" + distanceExtension.getPresentableDistance() + "</strong>";

    NaturalLanguageStringStructure progressStatus = journey.getProgressStatus();

    // at terminal label only appears in stop results
    if (isStopContext && progressStatus != null
            && progressStatus.getValue().contains("layover")) {
   
    	if(journey.getOriginAimedDepartureTime() != null) {
        	DateFormat formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        	
        	if(journey.getOriginAimedDepartureTime().getTime() < new Date().getTime()) {
        		message += "at terminal";
        	} else {    			
        		message += "at terminal, scheduled to depart " + formatter.format(journey.getOriginAimedDepartureTime());
        	}
        } else {
        	message += "at terminal";
        }
    } else if (isStopContext && progressStatus != null
        && progressStatus.getValue().contains("prevTrip")) {
    	message += "+ scheduled layover at terminal";
    }
    	
    int staleTimeout = _configurationService.getConfigurationValueAsInteger(
        "display.staleTimeout", 120);
    long age = (SystemTime.currentTimeMillis() - updateTime) / 1000;

    if (age > staleTimeout) {
      if (message.length() > 0) {
        message += ", ";
      }

      message += "old data";
    }

    if (message.length() > 0)
      return distance + " (" + message + ")";
    else
      return distance;
  }
}
