/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.siri.siri.ErrorDescriptionStructure;
import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri.OtherErrorStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.StopMonitoringDeliveryStructure;

public class StopMonitoringAction extends MonitoringActionV1Base {

  private static Logger _log = LoggerFactory.getLogger(StopMonitoringAction.class);

  private static final long serialVersionUID = 1L;
  
  private static final int V3 = 3;

  public StopMonitoringAction() {
    super(V3);
  }


  public DefaultHttpHeaders index() throws IOException {

    processGoogleAnalytics();

    long responseTimestamp = getTime();

    _realtimeService.setTime(responseTimestamp);

    String directionId = _servletRequest.getParameter(DIRECTION_REF);

    // We need to support the user providing no agency id which means 'all agencies'.
    // So, this array will hold a single agency if the user provides it or all
    // agencies if the user provides none. We'll iterate over them later while 
    // querying for vehicles and routes
    List<String> agencyIds = new ArrayList<String>();

    // Try to get the agency id passed by the user
    String agencyId = _servletRequest.getParameter(OPERATOR_REF);

    if (agencyId != null) {
      // The user provided an agency id so, use it
      agencyIds.add(agencyId);
    } else {
      // They did not provide an agency id, so interpret that an any/all agencies.
      Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
      agencyIds.addAll(agencies.keySet());
    }

    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    String stopIdsErrorString = "";
    if (_servletRequest.getParameter(MONITORING_REF) != null) {
      try {
        // If the user included an agency id as part of the stop id, ignore any OperatorRef arg
        // or lack of OperatorRef arg and just use the included one.
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(_servletRequest.getParameter(MONITORING_REF));
        if (isValidStop(stopId)) {
          stopIds.add(stopId);
        } else {
          stopIdsErrorString += "No such stop: " + stopId.toString() + ".";
        }
      } catch (Exception e) {
        // The user didn't provide an agency id in the MonitoringRef, so use our list of operator refs
        for (String agency : agencyIds) {
          AgencyAndId stopId = new AgencyAndId(agency, _servletRequest.getParameter(MONITORING_REF));
          if (isValidStop(stopId)) {
            stopIds.add(stopId);
          } else {
            stopIdsErrorString += "No such stop: " + stopId.toString() + ". ";
          }
        }
        stopIdsErrorString = stopIdsErrorString.trim();
      }
      if (stopIds.size() > 0) stopIdsErrorString = "";
    } else {
      stopIdsErrorString = "You must provide a MonitoringRef.";
    }

    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();
    String routeIdsErrorString = "";
    if (_servletRequest.getParameter(LINE_REF) != null) {
      try {
        // Same as above for stop id
        AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(_servletRequest.getParameter(LINE_REF));
        if (isValidRoute(routeId)) {
          routeIds.add(routeId);
        } else {
          routeIdsErrorString += "No such route: " + routeId.toString() + ".";
        }
      } catch (Exception e) {
        // Same as above for stop id
        for (String agency : agencyIds) {
          AgencyAndId routeId = new AgencyAndId(agency, _servletRequest.getParameter(LINE_REF));
          if (isValidRoute(routeId)) {
            routeIds.add(routeId);
          } else {
            routeIdsErrorString += "No such route: " + routeId.toString() + ". ";
          }
        }
        routeIdsErrorString = routeIdsErrorString.trim();
      }
      if (routeIds.size() > 0) routeIdsErrorString = "";
    }

    String detailLevel = _servletRequest.getParameter(STOP_MONITORING_DETAIL_LEVEL);

    int maximumOnwardCalls = 0;
    if (detailLevel != null && detailLevel.equals("calls")) {
      maximumOnwardCalls = Integer.MAX_VALUE;

      try {
        maximumOnwardCalls = Integer.parseInt(_servletRequest.getParameter(MAX_ONWARD_CALLS));
      } catch (NumberFormatException e) {
        maximumOnwardCalls = Integer.MAX_VALUE;
      }
    }

    int maximumStopVisits = Integer.MAX_VALUE;
    try {
      maximumStopVisits = Integer.parseInt(_servletRequest.getParameter(MAX_STOP_VISITS));
    } catch (NumberFormatException e) {
      maximumStopVisits = Integer.MAX_VALUE;
    }

    Integer minimumStopVisitsPerLine = null;
    try {
      minimumStopVisitsPerLine = Integer.parseInt(_servletRequest.getParameter(MIN_STOP_VISITS));
    } catch (NumberFormatException e) {
      minimumStopVisitsPerLine = null;
    }

    // Monitored Stop Visits
    List<MonitoredStopVisitStructure> visits = new ArrayList<MonitoredStopVisitStructure>();
    Map<String, MonitoredStopVisitStructure> visitsMap = new HashMap<String, MonitoredStopVisitStructure>();

    for (AgencyAndId stopId : stopIds) {

      if (!stopId.hasValues()) continue;

      // Stop ids can only be valid here because we only added valid ones to stopIds.
      List<MonitoredStopVisitStructure> visitsForStop = _realtimeService.getMonitoredStopVisitsForStop(stopId.toString(), maximumOnwardCalls, responseTimestamp);
      if (visitsForStop != null) visits.addAll(visitsForStop);
    }

    List<MonitoredStopVisitStructure> filteredVisits = new ArrayList<MonitoredStopVisitStructure>();

    Map<AgencyAndId, Integer> visitCountByLine = new HashMap<AgencyAndId, Integer>();
    int visitCount = 0;

    for (MonitoredStopVisitStructure visit : visits) {
      MonitoredVehicleJourneyStructure journey = visit.getMonitoredVehicleJourney();

      AgencyAndId thisRouteId = AgencyAndIdLibrary.convertFromString(journey.getLineRef().getValue());
      String thisDirectionId = journey.getDirectionRef().getValue();

      // user filtering
      if (routeIds.size() > 0 && !routeIds.contains(thisRouteId))
        continue;

      if (thisDirectionId != null) {
        if (directionId != null && !thisDirectionId.equals(directionId))
          continue;
      }

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
      if (visit.getMonitoredVehicleJourney() == null ||
              visit.getMonitoredVehicleJourney().getVehicleRef() == null ||
              StringUtils.isBlank(visit.getMonitoredVehicleJourney().getVehicleRef().getValue())) {
        continue;
      } else {
        String visitKey = visit.getMonitoredVehicleJourney().getVehicleRef().getValue();
        if (visitsMap.containsKey(visit.getMonitoredVehicleJourney().getVehicleRef().getValue())) {
          if (visit.getMonitoredVehicleJourney().getProgressStatus() == null) {
            visitsMap.remove(visitKey);
            visitsMap.put(visitKey, visit);
          }
          continue;
        } else {
          visitsMap.put(visit.getMonitoredVehicleJourney().getVehicleRef().getValue(), visit);
        }
      }

      filteredVisits.add(visit);

      visitCount++;
      visitCountForThisLine++;
      visitCountByLine.put(thisRouteId, visitCountForThisLine);
    }
    visits = filteredVisits;

    Exception error = null;
    if (stopIds.size() == 0 || (_servletRequest.getParameter(LINE_REF) != null && routeIds.size() == 0)) {
      String errorString = (stopIdsErrorString + " " + routeIdsErrorString).trim();
      error = new Exception(errorString);
    }

    _siriResponse = generateSiriResponse(visits, stopIds, error, responseTimestamp);

    // use ApiActionSupport to set proper headers instead of writing directly to response
    return setOkResponseText(getSiri());
  }
  
  private boolean isValidRoute(AgencyAndId routeId) {
    if (routeId != null && routeId.hasValues() && this._transitDataService.getRouteForId(routeId.toString()) != null) {
      return true;
    }
    return false;
  }
  
  private boolean isValidStop(AgencyAndId stopId) {
    try {
      StopBean stopBean = _transitDataService.getStop(stopId.toString());
      if (stopBean != null) return true;
    } catch (Exception e) {
      // This means the stop id is not valid.
    }
    return false;
  }
  
  private Siri generateSiriResponse(List<MonitoredStopVisitStructure> visits, List<AgencyAndId> stopIds, Exception error, long responseTimestamp) {

      StopMonitoringDeliveryStructure stopMonitoringDelivery = new StopMonitoringDeliveryStructure();
      stopMonitoringDelivery.setResponseTimestamp(new Date(responseTimestamp));

      ServiceDelivery serviceDelivery = new ServiceDelivery();
      serviceDelivery.setResponseTimestamp(new Date(responseTimestamp));
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
        stopMonitoringDelivery.setValidUntil(gregorianCalendar.getTime());

        stopMonitoringDelivery.getMonitoredStopVisit().addAll(visits);

        serviceDelivery.setResponseTimestamp(new Date(responseTimestamp));

        _serviceAlertsHelper.addSituationExchangeToSiriForStops(serviceDelivery, visits, _transitDataService, stopIds);
        _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(serviceDelivery, _realtimeService);
      }

      Siri siri = new Siri();
      siri.setServiceDelivery(serviceDelivery);

      return siri;
  }

}
