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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.onebusaway.api.actions.siri.impl.ServiceAlertsHelperV2;
import org.onebusaway.api.actions.siri.service.RealtimeServiceV2;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.org.siri.siri_2.Siri;

/**
 * Base action for Siri Monitoring V2 calls.
 */
public class MonitoringActionV2Base extends SiriAction {

  private static Logger _log = LoggerFactory.getLogger(MonitoringActionV2Base.class);

  // Errors
  public static final String ERROR_REQUIRED_PARAMS = "You must provide a valid Circle, BoundingBox or LineRef value. ";
  public static final String ERROR_NON_NUMERIC = "One or more coordinate values contain a non-numeric value. ";

  @Autowired
  protected RealtimeServiceV2 _realtimeService;

  // note this is a v2 helper
  protected ServiceAlertsHelperV2 _serviceAlertsHelper = new ServiceAlertsHelperV2();
  // note this is v2 siri
  protected Siri _siriResponse;


  public MonitoringActionV2Base() {
    super(V3);
  }

  protected String getSiri() {
    try {
      if (getType().equals("xml")) {
        return _realtimeService.getSiriXmlSerializer()
                .getXml(_siriResponse);
      } else {
        return _realtimeService.getSiriJsonSerializer().getJson(
                _siriResponse, /*_servletRequest.getParameter("callback")*/null);
        // callback happens at a lower level
      }
    } catch (Exception e) {
      _log.error("Siri v2 serialization failed: ", e,e);
      return e.toString();
    }
  }

  protected boolean isValidRoute(AgencyAndId routeId) {
    if (routeId != null
        && routeId.hasValues()
        && _transitDataService.getRouteForId(routeId.toString()) != null) {
      return true;
    }
    return false;
  }

  protected boolean isValidStop(AgencyAndId stopId) {
    try {
      StopBean stopBean = _transitDataService
          .getStop(stopId.toString());
      if (stopBean != null)
        return true;
    } catch (Exception e) {
      // This means the stop id is not valid.
    }
    return false;
  }

  protected List<String> processAgencyIds(String agencyId){
    List<String> agencyIds = new ArrayList<String>();
    
    // Try to get the agency id passed by the user
    if (agencyId != null) {
      // The user provided an agency id so, use it
      agencyIds.add(agencyId);
    } else {
      // They did not provide an agency id, so interpret that as any/all
      // agencies.
      Map<String,List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();

      agencyIds.addAll(agencies.keySet());
    }
    
    return agencyIds;
  }
  
  protected List<AgencyAndId> processVehicleIds(String vehicleRef, List<String> agencyIds){
    List<AgencyAndId> vehicleIds = new ArrayList<AgencyAndId>();
      if (vehicleRef != null) {
        try {
          // If the user included an agency id as part of the vehicle id, ignore any OperatorRef arg
          // or lack of OperatorRef arg and just use the included one.
          AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(vehicleRef);
          vehicleIds.add(vehicleId);
        } catch (Exception e) {
          // The user didn't provide an agency id in the VehicleRef, so use our list of operator refs
          for (String agency : agencyIds) {
            AgencyAndId vehicleId = new AgencyAndId(agency, vehicleRef);
            vehicleIds.add(vehicleId);
          }
        }
      }
    
    return vehicleIds;
  }
  
  protected String processRouteIds(String lineRef, List<AgencyAndId> routeIds, List<String> agencyIds) {
    String routeIdsErrorString = "";
    if (lineRef != null) {
      try {
        AgencyAndId routeId = AgencyAndIdLibrary
            .convertFromString(lineRef);
        if (this.isValidRoute(routeId)) {
          routeIds.add(routeId);
        } else {
          routeIdsErrorString += "No such route: "
              + routeId.toString() + ".";
        }
      } catch (Exception e) {
        for (String agency : agencyIds) {
          AgencyAndId routeId = new AgencyAndId(agency, lineRef);
          if (this.isValidRoute(routeId)) {
            routeIds.add(routeId);
          } else {
            routeIdsErrorString += "No such route: "
                + routeId.toString() + ". ";
          }
        }
        routeIdsErrorString = routeIdsErrorString.trim();
      }
      if (routeIds.size() > 0) {

        routeIdsErrorString = "";
      }
    }
    
    return routeIdsErrorString;
  }

  protected String processStopIds(String monitoringRef, List<AgencyAndId> stopIds, List<String> agencyIds){
    
    String stopIdsErrorString = "";
    if (monitoringRef != null) {
      try {
        // If the user included an agency id as part of the stop id, ignore any OperatorRef arg
        // or lack of OperatorRef arg and just use the included one.
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(monitoringRef);
        if (isValidStop(stopId)) {
          stopIds.add(stopId);
        } else {
          stopIdsErrorString += "No such stop: " + stopId.toString() + ". ";
        }
      } catch (Exception e) {
        // The user didn't provide an agency id in the MonitoringRef, so use our list of operator refs
        for (String agency : agencyIds) {
          AgencyAndId stopId = new AgencyAndId(agency, monitoringRef);
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
      stopIdsErrorString = "You must provide a MonitoringRef. ";
    }
    
    return stopIdsErrorString;
}

  protected boolean isValidBoundsDistance(CoordinateBounds bounds, double maxRadius){
    if(bounds != null){
     CoordinateBounds maxBounds = SphericalGeometryLibrary.bounds(
         bounds.getMinLat(), bounds.getMinLon(), maxRadius + 1);
     double maxLatSpan = (maxBounds.getMaxLat() - maxBounds.getMinLat());
     double maxLonSpan = (maxBounds.getMaxLon() - maxBounds.getMinLon());
     double latSpan = (bounds.getMaxLat() - bounds.getMinLat());
     double lonSpan = (bounds.getMaxLon() - bounds.getMinLon());
     if (latSpan < maxLatSpan && lonSpan < maxLonSpan) {
       return true;
     }
    }
    return false;
  }

  protected boolean isValidBoundBoxDistance(CoordinateBounds bounds, double maxDistance){
    if(bounds != null){
      Double distance = SphericalGeometryLibrary.distanceFaster(bounds.getMinLat(), bounds.getMinLon(), bounds.getMaxLat(), bounds.getMaxLon());
      if (distance != null && distance <= maxDistance) {
         return true;   
       }
    }
    return false;
  }
  
  protected CoordinateBounds getBoxBounds(String boundingCoordinates) throws NumberFormatException{
    CoordinateBounds bounds = null;
    if (boundingCoordinates != null) {
      String[] coordinates = boundingCoordinates.split(",");
      if (coordinates.length >= 4) {
        CoordinateBounds userBounds = new CoordinateBounds(
            Double.parseDouble(coordinates[0]), 
            Double.parseDouble(coordinates[1]),
            Double.parseDouble(coordinates[2]),
            Double.parseDouble(coordinates[3]));
        bounds = userBounds;
      }
    }
    return bounds;
  }

  protected CoordinateBounds getCircleBounds(String boundingCoordinates) throws NumberFormatException{
    CoordinateBounds bounds = null;
    if (boundingCoordinates != null) {
      String[] coordinates = boundingCoordinates.split(",");
      if(coordinates.length == 3){  
        bounds = SphericalGeometryLibrary.bounds(
            Double.parseDouble(coordinates[0]), 
            Double.parseDouble(coordinates[1]), 
            Double.parseDouble(coordinates[2]));  
      }
    }
    return bounds;
  }
}
