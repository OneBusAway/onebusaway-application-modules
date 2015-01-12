/*
 * Copyright 2010, OpenPlans Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.api.actions.api.where.siri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.presentation.impl.service_alerts.ServiceAlertsHelper;
import org.onebusaway.presentation.services.cachecontrol.CacheService;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri.ErrorDescriptionStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.OtherErrorStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.VehicleActivityStructure;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;

public class VehicleMonitoringAction extends ApiActionSupport
    implements ServletRequestAware, ServletResponseAware {

  private static final long serialVersionUID = 1L;
  protected static Logger _log = LoggerFactory.getLogger(VehicleMonitoringAction.class);
  
  private static final int V1 = 1;

  private static final int V2 = 2;
  
  private static final int V3 = 3;

@Autowired
  public TransitDataService _transitDataService;

  @Autowired
  private RealtimeService _realtimeService;
  
  private Siri _response;
  
  private String _cachedResponse = null;

  private ServiceAlertsHelper _serviceAlertsHelper = new ServiceAlertsHelper();

  HttpServletRequest _request;
  
  private HttpServletResponse _servletResponse;

  // See urlrewrite.xml as to how this is set. Which means this action doesn't
  // respect an HTTP Accept: header.
  private String _type = "xml";

  @Autowired
  private CacheService<Integer, String> _cacheService;
    
  public VehicleMonitoringAction() {
	    super(V3);
	  }

  public void setType(String type) {
    _type = type;
  }

  //@Override
  public String index() {

    long currentTimestamp = getTime();
    
    _realtimeService.setTime(currentTimestamp);
    
    String directionId = _request.getParameter("DirectionRef");
    
    // We need to support the user providing no agency id which means 'all agencies'.
    // So, this array will hold a single agency if the user provides it or all
    // agencies if the user provides none. We'll iterate over them later while 
    // querying for vehicles and routes
    List<String> agencyIds = new ArrayList<String>();

    String agencyId = _request.getParameter("OperatorRef");
    System.out.println("OperatorRef: " + agencyId);
    
    if (agencyId != null) {
      agencyIds.add(agencyId);
    } else {
      Map<String,List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
      agencyIds.addAll(agencies.keySet());
    }

    List<AgencyAndId> vehicleIds = new ArrayList<AgencyAndId>();
    if (_request.getParameter("VehicleRef") != null) {
      try {
        // If the user included an agency id as part of the vehicle id, ignore any OperatorRef arg
        // or lack of OperatorRef arg and just use the included one.
        AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(_request.getParameter("VehicleRef"));
        vehicleIds.add(vehicleId);
      } catch (Exception e) {
        // The user didn't provide an agency id in the VehicleRef, so use our list of operator refs
        for (String agency : agencyIds) {
          AgencyAndId vehicleId = new AgencyAndId(agency, _request.getParameter("VehicleRef"));
          vehicleIds.add(vehicleId);
        }
      }
    }
    
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    String routeIdErrorString = "";
    if (_request.getParameter("LineRef") != null) {
      try {
        // Same as above for vehicle id
        AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(_request.getParameter("LineRef"));
        if (isValidRoute(routeId)) {
          routeIds.add(routeId);
        } else {
          routeIdErrorString += "No such route: " + routeId.toString() + ".";
        }
      } catch (Exception e) {
        // Same as above for vehicle id
        for (String agency : agencyIds) {
          AgencyAndId routeId = new AgencyAndId(agency, _request.getParameter("LineRef"));
          if (isValidRoute(routeId)) {
            routeIds.add(routeId);
          } else {
            routeIdErrorString += "No such route: " + routeId.toString() + ". ";
          }
        }
      }
    }

    String detailLevel = _request.getParameter("VehicleMonitoringDetailLevel");

    int maximumOnwardCalls = 0;
    if (detailLevel != null && detailLevel.equals("calls")) {
      maximumOnwardCalls = Integer.MAX_VALUE;

      try {
        maximumOnwardCalls = Integer.parseInt(_request.getParameter("MaximumNumberOfCallsOnwards"));
      } catch (NumberFormatException e) {
        maximumOnwardCalls = Integer.MAX_VALUE;
      }
    }
    
    String gaLabel = null;
    
    // *** CASE 1: single vehicle, ignore any other filters
    if (vehicleIds.size() > 0) {
      
      gaLabel = _request.getParameter("VehicleRef");
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      
      for (AgencyAndId vehicleId : vehicleIds) {
        VehicleActivityStructure activity = _realtimeService.getVehicleActivityForVehicle(
            vehicleId.toString(), maximumOnwardCalls, currentTimestamp);

        if (activity != null) {
          activities.add(activity);
        }
      }
      
      // No vehicle id validation, so we pass null for error
      _response = generateSiriResponse(activities, null, null, currentTimestamp);

      // *** CASE 2: by route, using direction id, if provided
    } else if (_request.getParameter("LineRef") != null) {
      
      gaLabel = _request.getParameter("LineRef");
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      
      for (AgencyAndId routeId : routeIds) {
        
        List<VehicleActivityStructure> activitiesForRoute = _realtimeService.getVehicleActivityForRoute(
            routeId.toString(), directionId, maximumOnwardCalls, currentTimestamp);
        if (activitiesForRoute != null) {
          System.out.println("Size of vehicle activity for route: " + activitiesForRoute.size());
          activities.addAll(activitiesForRoute);
        }
        else System.out.println("No vehicle activity reported for route.");
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
      if (_request.getParameter("LineRef") != null && routeIds.size() == 0) {
        error = new Exception(routeIdErrorString.trim());
      }

      _response = generateSiriResponse(activities, routeIds, error, currentTimestamp);
      
      // *** CASE 3: all vehicles
    } else {
      try {
      gaLabel = "All Vehicles";
      
      int hashKey = _cacheService.hash(maximumOnwardCalls, agencyIds, _type);
      
      List<VehicleActivityStructure> activities = new ArrayList<VehicleActivityStructure>();
      if (!_cacheService.containsKey(hashKey)) {
        for (String agency : agencyIds) {
          ListBean<VehicleStatusBean> vehicles = _transitDataService.getAllVehiclesForAgency(
              agency, currentTimestamp);

          for (VehicleStatusBean v : vehicles.getList()) {
            VehicleActivityStructure activity = _realtimeService.getVehicleActivityForVehicle(
                v.getVehicleId(), maximumOnwardCalls, currentTimestamp);

            if (activity != null) {
              activities.add(activity);
            }
          }
        }
        // There is no input (route id) to validate, so pass null error
        _response = generateSiriResponse(activities, null, null,
            currentTimestamp);
        _cacheService.store(hashKey, getVehicleMonitoring());
      } else {
        _cachedResponse = _cacheService.retrieve(hashKey);
      }
      } catch (Exception e) {
        _log.error("vm all broke:", e);
        throw new RuntimeException(e);
      }
    }
        
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
    vehicleMonitoringDelivery.setResponseTimestamp(new Date(currentTimestamp));
    
    ServiceDelivery serviceDelivery = new ServiceDelivery();
    serviceDelivery.setResponseTimestamp(new Date(currentTimestamp));
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
      vehicleMonitoringDelivery.setValidUntil(gregorianCalendar.getTime());

      vehicleMonitoringDelivery.getVehicleActivity().addAll(activities);

      _serviceAlertsHelper.addSituationExchangeToServiceDelivery(serviceDelivery,
          activities, _transitDataService, routeIds);
      _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(serviceDelivery, _realtimeService);
    }
    Siri siri = new Siri();
    siri.setServiceDelivery(serviceDelivery);

    return siri;
  }
  
  private boolean isValidRoute(AgencyAndId routeId) {
    if (routeId != null && routeId.hasValues() && this._transitDataService.getRouteForId(routeId.toString()) != null) {
      return true;
    }
    return false;
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
  
}