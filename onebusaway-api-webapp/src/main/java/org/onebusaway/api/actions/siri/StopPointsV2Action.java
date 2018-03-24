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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
//import org.onebusaway.nyc.siri.support.SiriUpcomingServiceExtension;
import org.onebusaway.presentation.impl.DateUtil;
import org.onebusaway.transit_data_federation.siri.SiriUpcomingServiceExtension;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.PageViewHit;

import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.ErrorDescriptionStructure;
import uk.org.siri.siri_2.ExtensionsStructure;
import uk.org.siri.siri_2.OtherErrorStructure;
import uk.org.siri.siri_2.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri_2.Siri;
import uk.org.siri.siri_2.StopPointsDeliveryStructure;

public class StopPointsV2Action extends MonitoringActionBase 
    implements ServletRequestAware, ServletResponseAware {

  private static final long serialVersionUID = 1L;
  
  @Autowired
  private GoogleAnalyticsServiceImpl _gaService;
  
  private Siri _response;

  private HttpServletRequest _request;

  private HttpServletResponse _servletResponse;

  // See urlrewrite.xml as to how this is set. Which means this action doesn't
  // respect an HTTP Accept: header.
  private String _type = "xml";

  public void setType(String type) {
    _type = type;
  }

  public DefaultHttpHeaders index() throws IOException {  

    long responseTimestamp = getTime();
    processGoogleAnalytics();

    _realtimeService.setTime(responseTimestamp);
    
    boolean useLineRefOnly = false;
    Boolean upcomingServiceAllStops = null;
    
    CoordinateBounds bounds = null;
    boolean validBoundDistance = true;
    
    // User Parameters
    String boundingBox = _request.getParameter(BOUNDING_BOX);
    String circle = _request.getParameter(CIRCLE);
    String lineRef = _request.getParameter(LINE_REF);
    String directionId = _request.getParameter(DIRECTION_REF);
    String agencyId = _request.getParameter(OPERATOR_REF);
    String hasUpcomingScheduledService = _request.getParameter(UPCOMING_SCHEDULED_SERVICE);
    String detailLevelParam = _request.getParameter(STOP_POINTS_DETAIL_LEVEL);
    
    
    //get the detail level parameter or set it to default if not specified
      DetailLevel detailLevel;
      
      if(DetailLevel.contains(detailLevelParam)){
        detailLevel = DetailLevel.valueOf(detailLevelParam.toUpperCase());
      }
      else{
        detailLevel = DetailLevel.NORMAL;
      }
    
    
    // Error Strings
    String routeIdsErrorString = "";
    String boundsErrorString = "";
    
    /* 
     * We need to support the user providing no agency id which means 'all
    agencies'. So, this array will hold a single agency if the user provides it or
    all agencies if the user provides none. We'll iterate over them later while
    querying for vehicles and routes
    */
  
    List<String> agencyIds = processAgencyIds(agencyId);
    
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    
    routeIdsErrorString =  processRouteIds(lineRef, routeIds, agencyIds);

    // Calculate Bounds 
    try{
      if(StringUtils.isNotBlank(circle)){
        bounds = getCircleBounds(circle); 
        
        if(bounds != null && !isValidBoundsDistance(bounds, MAX_BOUNDS_RADIUS)){
          boundsErrorString += "Provided values exceed allowed search radius of " + MAX_BOUNDS_RADIUS + "m ";
          validBoundDistance = false;
        }
      }
      else if(StringUtils.isNotBlank(boundingBox)){
        bounds = getBoxBounds(boundingBox);
        
        if(bounds != null && !isValidBoundBoxDistance(bounds, MAX_BOUNDS_DISTANCE)){
          boundsErrorString += "Provided values exceed allowed search distance of " + MAX_BOUNDS_DISTANCE + "m ";
          validBoundDistance = false;
        }
      }
    }
    catch (NumberFormatException nfe){
      boundsErrorString += ERROR_NON_NUMERIC;
    }

    // Check for case where only LineRef was provided
    if (bounds == null) {
      if (routeIds.size() > 0) {
        useLineRefOnly = true;
      } else {
        boundsErrorString += ERROR_REQUIRED_PARAMS;
      }
    }

    // Setup Filters
    Map<Filters, String> filters = new HashMap<Filters, String>();
    filters.put(Filters.DIRECTION_REF, directionId);
    filters.put(Filters.UPCOMING_SCHEDULED_SERVICE,hasUpcomingScheduledService);

    // Annotated Stop Points
    List<AnnotatedStopPointStructure> stopPoints = new ArrayList<AnnotatedStopPointStructure>();
    Map<Boolean, List<AnnotatedStopPointStructure>> stopPointsMap;

    // Error Handler
    Exception error = null;
    if ((bounds == null && !useLineRefOnly) || 
      (_request.getParameter(LINE_REF) != null && routeIds.size() == 0) ||
      !validBoundDistance) {
      String errorString = (boundsErrorString + " " + routeIdsErrorString).trim();
      error = new Exception(errorString);
    }
    else{
    
      if (useLineRefOnly) {
        stopPointsMap = _realtimeService.getAnnotatedStopPointStructures(agencyIds,
            routeIds, detailLevel, responseTimestamp, filters);
      } else {
        stopPointsMap = _realtimeService.getAnnotatedStopPointStructures(bounds, agencyIds,
            routeIds, detailLevel, responseTimestamp, filters);     
      }
      
      for (Map.Entry<Boolean, List<AnnotatedStopPointStructure>> entry : stopPointsMap.entrySet()) {
        if(entry.getValue().size() > 0)
          upcomingServiceAllStops= entry.getKey();
        stopPoints.addAll(entry.getValue());
      }
    }
    
    
    _response = generateSiriResponse(stopPoints, upcomingServiceAllStops, error, responseTimestamp);

    try {
      this._servletResponse.getWriter().write(getStopPoints());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private Siri generateSiriResponse(
      List<AnnotatedStopPointStructure> stopPoints, Boolean hasUpcomingScheduledService, Exception error,
      long responseTimestamp) {

    StopPointsDeliveryStructure stopPointsDelivery = new StopPointsDeliveryStructure();
    
    stopPointsDelivery.setResponseTimestamp(DateUtil
        .toXmlGregorianCalendar(responseTimestamp));

    if (error != null) {
      ServiceDeliveryErrorConditionStructure errorConditionStructure = new ServiceDeliveryErrorConditionStructure();

      ErrorDescriptionStructure errorDescriptionStructure = new ErrorDescriptionStructure();
      errorDescriptionStructure.setValue(error.getMessage());

      OtherErrorStructure otherErrorStructure = new OtherErrorStructure();
      otherErrorStructure.setErrorText(error.getMessage());

      errorConditionStructure.setDescription(errorDescriptionStructure);
      errorConditionStructure.setOtherError(otherErrorStructure);

      stopPointsDelivery.setErrorCondition(errorConditionStructure);
    } else {
      Calendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTimeInMillis(responseTimestamp);
      gregorianCalendar.add(Calendar.MINUTE, 1);
      stopPointsDelivery
          .setValidUntil(DateUtil
              .toXmlGregorianCalendar(gregorianCalendar
                  .getTimeInMillis()));
      
      stopPointsDelivery.getAnnotatedStopPointRef().addAll(stopPoints);
      
      if(hasUpcomingScheduledService != null){
        // siri extensions
        ExtensionsStructure upcomingServiceExtensions = new ExtensionsStructure();
        SiriUpcomingServiceExtension upcomingService = new SiriUpcomingServiceExtension();
        upcomingService.setUpcomingScheduledService(hasUpcomingScheduledService);
        upcomingServiceExtensions.setAny(upcomingService);
        stopPointsDelivery.setExtensions(upcomingServiceExtensions);
      }
      
      stopPointsDelivery.setResponseTimestamp(DateUtil
          .toXmlGregorianCalendar(responseTimestamp));

      // TODO - LCARABALLO Do I still need serviceAlertsHelper?
      /*
       * _serviceAlertsHelper.addSituationExchangeToSiriForStops(
       * serviceDelivery, visits, _nycTransitDataService, stopIds);
       * _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(
       * serviceDelivery, _realtimeService);
       */
    }

    Siri siri = new Siri();
    siri.setStopPointsDelivery(stopPointsDelivery);

    return siri;
  }

  public String getStopPoints() {
    try {
      if (_type.equals("xml")) {
        this._servletResponse.setContentType("application/xml");
        return _realtimeService.getSiriXmlSerializer()
            .getXml(_response);
      } else {
        this._servletResponse.setContentType("application/json");
        return _realtimeService.getSiriJsonSerializer().getJson(
            _response, _request.getParameter("callback"));
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

  public HttpServletResponse getServletResponse() {
    return _servletResponse;
  }
  
  private void processGoogleAnalytics(){
    if (_gaService == null) {
      return;
    }
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
