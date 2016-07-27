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
package org.onebusaway.api.actions.siri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.siri.impl.ServiceAlertsHelperV2;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.DateUtil;
import org.onebusaway.presentation.services.cachecontrol.CacheService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.PageViewHit;

import uk.org.siri.siri_2.ErrorDescriptionStructure;
import uk.org.siri.siri_2.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri_2.OtherErrorStructure;
import uk.org.siri.siri_2.ServiceDelivery;
import uk.org.siri.siri_2.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri_2.Siri;
import uk.org.siri.siri_2.VehicleActivityStructure;
import uk.org.siri.siri_2.VehicleMonitoringDeliveryStructure;

public class VehicleMonitoringV2Action extends MonitoringActionBase 
    implements ServletRequestAware, ServletResponseAware {
  
  private static final long serialVersionUID = 1L;
  protected static Logger _log = LoggerFactory.getLogger(VehicleMonitoringV2Action.class);

  private static final String VEHICLE_REF = "VehicleRef";

  @Autowired
  private GoogleAnalyticsServiceImpl _gaService;
  
  private Siri _response;

  private String _cachedResponse = null;

  private ServiceAlertsHelperV2 _serviceAlertsHelper = new ServiceAlertsHelperV2();

  HttpServletRequest _request;
  
  private HttpServletResponse _servletResponse;

  // See urlrewrite.xml as to how this is set. Which means this action doesn't
  // respect an HTTP Accept: header.
  private String _type = "xml";

  //private MonitoringActionSupport _monitoringActionSupport = new MonitoringActionSupport();

  public void setType(String type) {
    _type = type;
  }

  public DefaultHttpHeaders index() throws IOException {
    
    long currentTimestamp = getTime();
    
    processGoogleAnalytics();
    //_monitoringActionSupport.setupGoogleAnalytics(_request, _configurationService);
    
    _realtimeService.setTime(currentTimestamp);
    
    String detailLevelParam = _request.getParameter(VEHICLE_MONITORING_DETAIL_LEVEL);
  
  //get the detail level parameter or set it to default if not specified
    DetailLevel detailLevel;
    
    if(DetailLevel.contains(detailLevelParam)){
      detailLevel = DetailLevel.valueOf(detailLevelParam.toUpperCase());
    }
    else{
      detailLevel = DetailLevel.NORMAL;
    }
    
    // User Parameters
  String lineRef = _request.getParameter(LINE_REF);
  String vehicleRef = _request.getParameter(VEHICLE_REF);
  String directionId = _request.getParameter(DIRECTION_REF);
  String agencyId = _request.getParameter(OPERATOR_REF);
  String maxOnwardCallsParam = _request.getParameter(MAX_ONWARD_CALLS);
  String maxStopVisitsParam = _request.getParameter(MAX_STOP_VISITS);
  String minStopVisitsParam = _request.getParameter(MIN_STOP_VISITS);
    
  // Error Strings
  String routeIdsErrorString = "";
  
    /*
     * We need to support the user providing no agency id which means 'all agencies'.
    So, this array will hold a single agency if the user provides it or all
    agencies if the user provides none. We'll iterate over them later while 
    querying for vehicles and routes
    */
  
  List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
  
  List<String> agencyIds = processAgencyIds(agencyId);
    List<AgencyAndId> vehicleIds = processVehicleIds(vehicleRef, agencyIds);
    routeIdsErrorString =  processRouteIds(lineRef, routeIds, agencyIds);
    
    int maximumOnwardCalls = 0;
    
    if (detailLevel.equals(DetailLevel.CALLS)) {
    maximumOnwardCalls = SiriSupportV2.convertToNumeric(maxOnwardCallsParam, Integer.MAX_VALUE);
  }
    
    String gaLabel = null;
    
    // *** CASE 1: single vehicle, ignore any other filters
    if (vehicleIds.size() > 0) {
      
      gaLabel = vehicleRef;
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      try{
        for (AgencyAndId vehicleId : vehicleIds) {
          VehicleActivityStructure activity = _realtimeService.getVehicleActivityForVehicle(
              vehicleId.toString(), maximumOnwardCalls, detailLevel, currentTimestamp);
  
          if (activity != null) {
            activities.add(activity);
          }
        }
      }
      catch(Exception e){
        _log.info(e.getMessage(),e);
      }
      
      // No vehicle id validation, so we pass null for error
      _response = generateSiriResponse(activities, null, null, currentTimestamp);

      // *** CASE 2: by route, using direction id, if provided
    } else if (lineRef != null) {
      
      gaLabel = lineRef;
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      
      for (AgencyAndId routeId : routeIds) {
        
        List<VehicleActivityStructure> activitiesForRoute = _realtimeService.getVehicleActivityForRoute(
            routeId.toString(), directionId, maximumOnwardCalls, detailLevel, currentTimestamp);
        if (activitiesForRoute != null) {
          activities.addAll(activitiesForRoute);
        }
      }
      
      if (vehicleIds.size() > 0) {
        List<VehicleActivityStructure> filteredActivities = new ArrayList<VehicleActivityStructure>();

        for (VehicleActivityStructure activity : activities) {
          MonitoredVehicleJourneyStructure journey = activity.getMonitoredVehicleJourney();
          AgencyAndId thisVehicleId = AgencyAndIdLibrary.convertFromString(journey.getVehicleRef().getValue());

          // user filtering
          if (!vehicleIds.contains(thisVehicleId))
            continue;

          filteredActivities.add(activity);
        }

        activities = filteredActivities;
      }
      
      Exception error = null;
      if (lineRef != null && routeIds.size() == 0) {
        error = new Exception(routeIdsErrorString.trim());
      }

      _response = generateSiriResponse(activities, routeIds, error, currentTimestamp);
      
      // *** CASE 3: all vehicles
    } else {
      try {
      gaLabel = "All Vehicles";
      
      //int hashKey = _siriCacheService.hash(maximumOnwardCalls, agencyIds, _type);
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      //if (!_siriCacheService.containsKey(hashKey)) {
        for (String agency : agencyIds) {
          ListBean<VehicleStatusBean> vehicles = _transitDataService.getAllVehiclesForAgency(
              agency, currentTimestamp);

          for (VehicleStatusBean v : vehicles.getList()) {
            VehicleActivityStructure activity = _realtimeService.getVehicleActivityForVehicle(
                v.getVehicleId(), maximumOnwardCalls, detailLevel, currentTimestamp);

            if (activity != null) {
              activities.add(activity);
            }
          }
        }
        // There is no input (route id) to validate, so pass null error
        _response = generateSiriResponse(activities, null, null,
            currentTimestamp);
        //_siriCacheService.store(hashKey, getVehicleMonitoring());
      //} else {
      //  _cachedResponse = _siriCacheService.retrieve(hashKey);
      //}
      } catch (Exception e) {
        _log.error("vm all broke:", e);
        throw new RuntimeException(e);
      }
    }
    
    //if (_monitoringActionSupport.canReportToGoogleAnalytics(_configurationService)) {
    //  _monitoringActionSupport.reportToGoogleAnalytics(_request, "Vehicle Monitoring", gaLabel, _configurationService);
    //} 
    
    try {
      this._servletResponse.getWriter().write(getVehicleMonitoring());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Generate a siri response for a set of VehicleActivities
   * 
   * @param routeId
   */
  private Siri generateSiriResponse(List<VehicleActivityStructure> activities,
      List<AgencyAndId> routeIds, Exception error, long currentTimestamp) {
    
    VehicleMonitoringDeliveryStructure vehicleMonitoringDelivery = new VehicleMonitoringDeliveryStructure();
    vehicleMonitoringDelivery.setResponseTimestamp(DateUtil.toXmlGregorianCalendar(currentTimestamp));
    
    ServiceDelivery serviceDelivery = new ServiceDelivery();
    serviceDelivery.setResponseTimestamp(DateUtil.toXmlGregorianCalendar(currentTimestamp));
    serviceDelivery.getVehicleMonitoringDelivery().add(vehicleMonitoringDelivery);
    
    if (error != null) {
      ServiceDeliveryErrorConditionStructure errorConditionStructure = new ServiceDeliveryErrorConditionStructure();
      
      ErrorDescriptionStructure errorDescriptionStructure = new ErrorDescriptionStructure();
      errorDescriptionStructure.setValue(error.getMessage());
      
      OtherErrorStructure otherErrorStructure = new OtherErrorStructure();
      otherErrorStructure.setErrorText(error.getMessage());
      
      errorConditionStructure.setDescription(errorDescriptionStructure);
      errorConditionStructure.setOtherError(otherErrorStructure);
      
      vehicleMonitoringDelivery.setErrorCondition(errorConditionStructure);
    } else {
      Calendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTimeInMillis(currentTimestamp);
      gregorianCalendar.add(Calendar.MINUTE, 1);
      vehicleMonitoringDelivery.setValidUntil(DateUtil.toXmlGregorianCalendar(gregorianCalendar.getTimeInMillis()));

      vehicleMonitoringDelivery.getVehicleActivity().addAll(activities);

      _serviceAlertsHelper.addSituationExchangeToServiceDelivery(serviceDelivery,
          activities, _transitDataService, routeIds);
      _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(serviceDelivery, _realtimeService);
    }
    Siri siri = new Siri();
    siri.setServiceDelivery(serviceDelivery);

    return siri;
  }

  public String getVehicleMonitoring() {
    if (_cachedResponse != null) {
      // check the cache first
      return _cachedResponse;
    }
    
    try {
      if (_type.equals("xml")) {
        this._servletResponse.setContentType("application/xml");
        return _realtimeService.getSiriXmlSerializer().getXml(_response);
      } else {
        this._servletResponse.setContentType("application/json");
        return _realtimeService.getSiriJsonSerializer().getJson(_response,
            _request.getParameter("callback"));
      }
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this._request = request;
  }

  @Override
  public void setServletResponse(HttpServletResponse servletResponse) {
    this._servletResponse = servletResponse;
  }
  
  public HttpServletResponse getServletResponse(){
    return _servletResponse;
  }
  
  private void processGoogleAnalytics(){
    processGoogleAnalyticsPageView();
    processGoogleAnalyticsApiKeys();  
  }
  
  private void processGoogleAnalyticsPageView(){
    _gaService.post(new PageViewHit());
  }
  
  private void processGoogleAnalyticsApiKeys(){
    String apiKey = _request.getParameter("key"); 
    if(StringUtils.isBlank(apiKey))
      apiKey = "Key Information Unavailable";
    
    _gaService.post(new EventHit(GA_EVENT_CATEGORY, GA_EVENT_ACTION, apiKey, 1));
  }
}
