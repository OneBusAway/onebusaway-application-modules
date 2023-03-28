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
import org.onebusaway.api.actions.siri.impl.ServiceAlertsHelperV2;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2;
import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.impl.DateUtil;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri_2.ErrorDescriptionStructure;
import uk.org.siri.siri_2.MonitoredStopVisitStructure;
import uk.org.siri.siri_2.OtherErrorStructure;
import uk.org.siri.siri_2.ServiceDelivery;
import uk.org.siri.siri_2.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri_2.Siri;
import uk.org.siri.siri_2.StopMonitoringDeliveryStructure;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.PageViewHit;

public class StopMonitoringV2Action  extends MonitoringActionBase
    implements ServletRequestAware, ServletResponseAware {

  private static final long serialVersionUID = 1L;

  @Autowired
  private GoogleAnalyticsServiceImpl _gaService;


  private Siri _response;

  private ServiceAlertsHelperV2 _serviceAlertsHelper = new ServiceAlertsHelperV2();

  private HttpServletRequest _request;

  private HttpServletResponse _servletResponse;

  // See urlrewrite.xml as to how this is set. Which means this action doesn't
  // respect an HTTP Accept: header.
  private String _type = "xml";
/*
  private MonitoringActionSupport _monitoringActionSupport = new MonitoringActionSupport();
*/
  public void setType(String type) {
    _type = type;
  }
/*
  @Override
  public String execute() {
  */
  public DefaultHttpHeaders index() throws IOException {
    long responseTimestamp = getTime();
    //_monitoringActionSupport.setupGoogleAnalytics(_request,
    //   _configurationService);
    processGoogleAnalytics();

    _realtimeService.setTime(responseTimestamp);
    String detailLevelParam = _request.getParameter(STOP_MONITORING_DETAIL_LEVEL);

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
    String monitoringRef = _request.getParameter(MONITORING_REF);
    String directionId = _request.getParameter(DIRECTION_REF);
    String agencyId = _request.getParameter(OPERATOR_REF);
    String maxOnwardCallsParam = _request.getParameter(MAX_ONWARD_CALLS);
    String maxStopVisitsParam = _request.getParameter(MAX_STOP_VISITS);
    String minStopVisitsParam = _request.getParameter(MIN_STOP_VISITS);

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

    _response = generateSiriResponse(visits, stopIds, error,
        responseTimestamp);

    try {
      this._servletResponse.getWriter().write(getStopMonitoring());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
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

  public String getStopMonitoring() {
    try {
      if (_type.equals("xml")) {
        this._servletResponse.setContentType("application/xml; charset=UTF-8");
        this._servletResponse.setCharacterEncoding("UTF-8");
        return _realtimeService.getSiriXmlSerializer()
            .getXml(_response);
      } else {
        this._servletResponse.setContentType("application/json; charset=UTF-8");
        this._servletResponse.setCharacterEncoding("UTF-8");
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
