/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.util.ConfigurationUtil;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.service.RouteCacheService;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NextBusApiBase {
  @Autowired
  protected TransitDataService _transitDataService;
  
  @Autowired
  protected RouteCacheService _routeCacheService;

  @Autowired
  protected ConfigurationUtil _configUtil;

  public static final String PREDICTIONS_COMMAND = "/command/predictions";

  public static final String SCHEDULE_COMMAND = "/command/predictions";

  public static final String REQUEST_TYPE = "json";

  // AGENCIES

  protected String getMappedAgency(String agencyId) {
    if (_configUtil.getAgencyMapper().containsKey(agencyId.toUpperCase()))
      return _configUtil.getAgencyMapper().get(agencyId.toUpperCase());
    return agencyId;
  }

  protected String getIdNoAgency(String id) {
    String[] agencyAndId = id.split("_");
    if (agencyAndId != null && agencyAndId.length == 2) {
      return agencyAndId[1];
    }
    return id;

  }

  protected List<String> getAgencies(String agencyIdVal) {
    String agencyId = agencyIdVal;
    List<String> agencyIds = new ArrayList<String>();
    if (agencyId != null) {
      // The user provided an agancy id so, use it
      agencyIds.add(agencyId);
    } else {
      // They did not provide an agency id, so interpret that an any/all
      // agencies.
      Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
      agencyIds.addAll(agencies.keySet());
    }
    return agencyIds;
  }

  protected boolean isValidAgency(Body body, String agencyId) {
    if (agencyId == null) {
      body.getErrors().add(
          new BodyError(
              "agency parameter \"a\" must be specified in query string"));
      return false;
    }
    if (_transitDataService.getAgency(agencyId) == null) {
      body.getErrors().add(
          new BodyError("Agency parameter \"a=" + agencyId + " is not valid."));
      return false;
    }
    return true;
  }

  protected List<String> processAgencyIds(String agencyId) {
    List<String> agencyIds = new ArrayList<String>();

    // Try to get the agency id passed by the user
    if (agencyId != null) {
      // The user provided an agancy id so, use it
      agencyIds.add(agencyId);
    } else {
      // They did not provide an agency id, so interpret that an any/all
      // agencies.
      Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
      agencyIds.addAll(agencies.keySet());
    }

    return agencyIds;
  }

  // ROUTES

  protected boolean isValidRoute(AgencyAndId routeId) {
    if (routeId != null && routeId.hasValues()
        && this._transitDataService.getRouteForId(routeId.toString()) != null) {
      return true;
    }
    return false;

  }

  protected boolean isValidRoute(Body body, AgencyAndId routeId) {
    if (!isValidRoute(routeId)) {
      String routeIdStr = null;
      if(routeId != null)
        routeIdStr = routeId.toString();
      body.getErrors().add(
          new BodyError("Could not get route \"" + routeId
              + "\". One of the tags could be bad."));
      return false;
    }
    return true;
  }

  protected boolean isValidRoute(Body body, String agencyId, String routeVal) {

    if (routeVal == null) {
      body.getErrors().add(
          new BodyError("For agency a=" + agencyId
              + " route r=null is invalid or temporarily unavailable."));
      return false;
    }

    try {
      AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(routeVal);
      if (isValidRoute(routeId)) {
        return true;
      }
    } catch (IllegalStateException e) {
      AgencyAndId routeId = new AgencyAndId(agencyId, routeVal);
      if (this.isValidRoute(body, routeId)) {
        return true;
      }
    }
    return false;
  }

  protected <E> boolean processRouteIds(String routeVal,
      List<AgencyAndId> routeIds, List<String> agencyIds, Body<E> body,
      boolean handleErrors) {
    if (routeVal != null) {
      try {
        AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(routeVal);
        if (this.isValidRoute(body, routeId)) {
          routeIds.add(routeId);
        }
      } catch (IllegalStateException e) {
        for (String agency : agencyIds) {
          AgencyAndId routeId = new AgencyAndId(agency, routeVal);
          if (this.isValidRoute(routeId)) {
            routeIds.add(routeId);
          }
        }
        if (handleErrors && routeIds.size() == 0) {
          body.getErrors().add(
              new BodyError("Could not get route \"" + routeVal
                  + "\". One of the tags could be bad."));
        }
      }
      return true;
    } else {
      if (handleErrors) {
        body.getErrors().add(
            new BodyError("For agency a=" + agencyIds.get(0)
                + " route r=null is invalid or temporarily unavailable."));
      }
      return false;
    }
  }

  protected <E> boolean processRouteIds(String routeVal,
      List<AgencyAndId> routeIds, List<String> agencyIds, Body<E> body) {
    return processRouteIds(routeVal, routeIds, agencyIds, body, true);
  }

  // STOPS

  protected <E> boolean processStopIds(String stopIdVal,
      List<AgencyAndId> stopIds, List<String> agencyIds, Body<E> body) {

    if (stopIdVal != null) {
      try {
        // If the user included an agency id as part of the stop id,
        // ignore any OperatorRef arg
        // or lack of OperatorRef arg and just use the included one.
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopIdVal);
        if (isValidStop(stopId)) {
          stopIds.add(stopId);
        } else {
          body.getErrors().add(
              new BodyError("stopId \"" + stopId.toString()
                  + "\" is not a valid stop id integer"));
          return false;
        }
      } catch (Exception e) {
        // The user didn't provide an agency id in the MonitoringRef, so
        // use our list of operator refs
        for (String agency : agencyIds) {
          AgencyAndId stopId = new AgencyAndId(agency, stopIdVal);
          if (isValidStop(stopId)) {
            stopIds.add(stopId);
          }
        }
        if (stopIds.size() == 0) {
          body.getErrors().add(
              new BodyError("stopId \"" + stopIdVal + "\" is not a valid stop id integer"));
          return false;
        }
      }
      return true;

    } else {
      body.getErrors().add(
          new BodyError(
              "stop parameter \"s\" must be specified in query string"));
      return false;
    }
  }

  protected boolean isValidStop(AgencyAndId stopId) {
    try {
      StopBean stopBean = _transitDataService.getStop(stopId.toString());
      if (stopBean != null)
        return true;
    } catch (Exception e) {
      // This means the stop id is not valid.
    }
    return false;
  }

  // VEHICLES

  protected List<AgencyAndId> processVehicleIds(String vehicleRef,
      List<String> agencyIds) {
    List<AgencyAndId> vehicleIds = new ArrayList<AgencyAndId>();
    if (vehicleRef != null) {
      try {
        // If the user included an agency id as part of the vehicle id,
        // ignore any OperatorRef arg
        // or lack of OperatorRef arg and just use the included one.
        AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(vehicleRef);
        vehicleIds.add(vehicleId);
      } catch (Exception e) {
        // The user didn't provide an agency id in the VehicleRef, so
        // use our list of operator refs
        for (String agency : agencyIds) {
          AgencyAndId vehicleId = new AgencyAndId(agency, vehicleRef);
          vehicleIds.add(vehicleId);
        }
      }
    }

    return vehicleIds;
  }

  // HTTP

  protected String getServiceUrl() {
    String host = _configUtil.getTransiTimeHost();
    String port = _configUtil.getTransiTimePort();
    String apiKey = _configUtil.getTransiTimeKey();
    String serviceUrl = "http://" + host + ":" + port + "/api/v1/key/" + apiKey
        + "/agency/";
    return serviceUrl;
  }

  protected JsonObject getJsonObject(String uri) throws Exception {
    URL url = new URL(uri);
    HttpURLConnection request = (HttpURLConnection) url.openConnection();
    request.connect();

    // Convert to a JSON object to print data
    JsonParser jp = new JsonParser(); // from gson
    JsonElement root = jp.parse(new InputStreamReader(
        (InputStream) request.getContent())); // Convert the input stream to a
    // json element
    JsonObject rootobj = root.getAsJsonObject(); // May be an array, may be
    // an
    // object.
    return rootobj;
  }

  protected String callURL(String myURL) {
    System.out.println("Requeted URL:" + myURL);
    StringBuilder sb = new StringBuilder();
    URLConnection urlConn = null;
    InputStreamReader in = null;
    try {
      URL url = new URL(myURL);
      urlConn = url.openConnection();
      if (urlConn != null)
        urlConn.setReadTimeout(60 * 1000);
      if (urlConn != null && urlConn.getInputStream() != null) {
        in = new InputStreamReader(urlConn.getInputStream(),
            Charset.defaultCharset());
        BufferedReader bufferedReader = new BufferedReader(in);
        if (bufferedReader != null) {
          int cp;
          while ((cp = bufferedReader.read()) != -1) {
            sb.append((char) cp);
          }
          bufferedReader.close();
        }
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("Exception while calling URL:" + myURL, e);
    }

    return sb.toString();
  }
  

}
