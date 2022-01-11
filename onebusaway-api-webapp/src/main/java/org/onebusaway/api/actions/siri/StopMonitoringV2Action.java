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

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.DateUtil;

import uk.org.siri.siri_2.ErrorDescriptionStructure;
import uk.org.siri.siri_2.MonitoredStopVisitStructure;
import uk.org.siri.siri_2.OtherErrorStructure;
import uk.org.siri.siri_2.ServiceDelivery;
import uk.org.siri.siri_2.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri_2.Siri;
import uk.org.siri.siri_2.StopMonitoringDeliveryStructure;

public class StopMonitoringV2Action extends MonitoringActionV2Base
    implements ServletRequestAware, ServletResponseAware {

  private static final long serialVersionUID = 1L;
  
  public DefaultHttpHeaders index() throws IOException {
    long responseTimestamp = getTime();
    processGoogleAnalytics();

    _realtimeService.setTime(responseTimestamp);
    String detailLevelParam = _servletRequest.getParameter(STOP_MONITORING_DETAIL_LEVEL);
    
    //get the detail level parameter or set it to default if not specified
      DetailLevel detailLevel;
      
      if(DetailLevel.contains(detailLevelParam)){
        detailLevel = DetailLevel.valueOf(detailLevelParam.toUpperCase());
      }
      else{
        detailLevel = DetailLevel.NORMAL;
      }
      
    // User Parameters
    String lineRef = _servletRequest.getParameter(LINE_REF);
    String monitoringRef = _servletRequest.getParameter(MONITORING_REF);
    String directionId = _servletRequest.getParameter(DIRECTION_REF);
    String agencyId = _servletRequest.getParameter(OPERATOR_REF);
    String maxOnwardCallsParam = _servletRequest.getParameter(MAX_ONWARD_CALLS);
    String maxStopVisitsParam = _servletRequest.getParameter(MAX_STOP_VISITS);
    String minStopVisitsParam = _servletRequest.getParameter(MIN_STOP_VISITS);
    
    // Error Strings
    String routeIdsErrorString = "";
    String stopIdsErrorString = "";

    /* 
     * We need to support the user providing no agency id which means 'all
    agencies'. So, this array will hold a single agency if the user provides it or
    all agencies if the user provides none. We'll iterate over them later while
    querying for vehicles and routes
    */

    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    

    List<String> agencyIds = processAgencyIds(agencyId);
    
    stopIdsErrorString = processStopIds(monitoringRef, stopIds, agencyIds);
    routeIdsErrorString =  processRouteIds(lineRef, routeIds, agencyIds);
    
    int maximumOnwardCalls = 0;
    
    if (detailLevel.equals(DetailLevel.CALLS)) {
      maximumOnwardCalls = SiriSupportV2.convertToNumeric(maxOnwardCallsParam, Integer.MAX_VALUE);
    }

    // Google Analytics code used by nyc
    //if (_monitoringActionSupport
    //    .canReportToGoogleAnalytics(_configurationService)) {
    //  _monitoringActionSupport.reportToGoogleAnalytics(_request,
    //      "Stop Monitoring", StringUtils.join(stopIds, ","),
    //      _configurationService);
    //}   
    
    
    // Setup Filters
    Map<Filters, String> filters = new HashMap<Filters, String>();
    filters.put(Filters.DIRECTION_REF, directionId);
    filters.put(Filters.MAX_STOP_VISITS, maxStopVisitsParam);
    filters.put(Filters.MIN_STOP_VISITS, minStopVisitsParam);
    
    
    // Monitored Stop Visits
    List<MonitoredStopVisitStructure> visits = new ArrayList<MonitoredStopVisitStructure>();
    
    for (AgencyAndId stopId : stopIds) {

      if (!stopId.hasValues())
        continue;

      // Stop ids can only be valid here because we only added valid ones
      // to stopIds.
      List<MonitoredStopVisitStructure> visitsForStop = _realtimeService
          .getMonitoredStopVisitsForStop(stopId.toString(),
              maximumOnwardCalls, detailLevel, responseTimestamp, routeIds, filters);
      if (visitsForStop != null)
        visits.addAll(visitsForStop);
    }

    Exception error = null;
    if (stopIds.size() == 0
        || (lineRef != null && routeIds.size() == 0)) {
      String errorString = (stopIdsErrorString + " " + routeIdsErrorString)
          .trim();
      error = new Exception(errorString);
    }

    _siriResponse = generateSiriResponse(visits, stopIds, error,
        responseTimestamp);

    // use ApiActionSupport to set proper headers instead of writing directly to response
    return setOkResponseText(getSiri());
  }

  private Siri generateSiriResponse(List<MonitoredStopVisitStructure> visits,
      List<AgencyAndId> stopIds, Exception error, long responseTimestamp) {

    StopMonitoringDeliveryStructure stopMonitoringDelivery = new StopMonitoringDeliveryStructure();
    stopMonitoringDelivery.setResponseTimestamp(DateUtil
        .toXmlGregorianCalendar(responseTimestamp));

    ServiceDelivery serviceDelivery = new ServiceDelivery();
    serviceDelivery.setResponseTimestamp(DateUtil
        .toXmlGregorianCalendar(responseTimestamp));
    serviceDelivery.getStopMonitoringDelivery().add(stopMonitoringDelivery);

    if (error != null) {
      ServiceDeliveryErrorConditionStructure errorConditionStructure = new ServiceDeliveryErrorConditionStructure();

      ErrorDescriptionStructure errorDescriptionStructure = new ErrorDescriptionStructure();
      errorDescriptionStructure.setValue(error.getMessage());

      OtherErrorStructure otherErrorStructure = new OtherErrorStructure();
      otherErrorStructure.setErrorText(error.getMessage());

      errorConditionStructure.setDescription(errorDescriptionStructure);
      errorConditionStructure.setOtherError(otherErrorStructure);

      stopMonitoringDelivery.setErrorCondition(errorConditionStructure);
    } else {
      Calendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTimeInMillis(responseTimestamp);
      gregorianCalendar.add(Calendar.MINUTE, 1);
      stopMonitoringDelivery
          .setValidUntil(DateUtil
              .toXmlGregorianCalendar(gregorianCalendar
                  .getTimeInMillis()));

      stopMonitoringDelivery.getMonitoredStopVisit().addAll(visits);

      serviceDelivery.setResponseTimestamp(DateUtil
          .toXmlGregorianCalendar(responseTimestamp));

      _serviceAlertsHelper.addSituationExchangeToSiriForStops(
          serviceDelivery, visits, _transitDataService, stopIds);
      _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(
          serviceDelivery, _realtimeService);
    }

    Siri siri = new Siri();
    siri.setServiceDelivery(serviceDelivery);

    return siri;
  }
}
