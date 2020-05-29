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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.util.ConfigurationMapUtil;
import org.onebusaway.nextbus.impl.util.ConfigurationUtil;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.service.cache.CacheService;
import org.onebusaway.nextbus.service.TdsMappingService;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;


public class NextBusApiBase {

  @Autowired
  protected TransitDataService _transitDataService;

  @Autowired
  protected TdsMappingService _tdsMappingService;

  @Autowired
  protected ConfigurationMapUtil _configMapUtil;

  @Autowired
  private CacheService _cache;
  
  public static final String SUCCESS = "success";

  public static final String PREDICTIONS_COMMAND = "/command/predictions";

  public static final String SCHEDULE_COMMAND = "/command/scheduleHorizStops";

  public static final String REQUEST_TYPE = "json";

  public static final long MAX_HEADER_AGE = 90 * 1000;

  // CACHE

  public AgencyBean getCachedAgencyBean(String id) {
    if (_configMapUtil.getConfig(id) != null) {
      AgencyBean bean = _cache.getAgency(id);
      if (bean == null) {
        bean = _transitDataService.getAgency(id);
        if (bean != null)
          _cache.putAgency(id, bean);
      }
      return bean;
    }
    return null;
  }

  public StopBean getCachedStopBean(String id) {
    StopBean stop = _cache.getStop(id);
    if (stop == null) {
      if (_cache.isInvalidStop(id)) {
        return null;
      }

      try {
        stop = _transitDataService.getStop(id);
      } catch (Throwable t) {
        _cache.setInvalidStop(id);
      }
      if (stop != null) {
        _cache.putStop(id, stop);
      } else {
        _cache.setInvalidStop(id);
      }
    }
    return stop;
  }

  // VALIDATION

  protected boolean isValidAgency(Body body, String agencyId) {
    if (agencyId == null) {
      body.getErrors().add(new BodyError(ErrorMsg.AGENCY_NULL.getDescription()));
      return false;
    }
    if (!isValidAgency(agencyId)) {
      body.getErrors().add(
          new BodyError(ErrorMsg.AGENCY_INVALID.getDescription(), agencyId));
      return false;
    }
    return true;
  }

  protected boolean isValidAgency(String agencyId) {
    try {
      return getCachedAgencyBean(agencyId) != null;
    } catch (Exception e) {
      // This means the agency id is not valid.
    }
    return false;
  }

  protected boolean isValidRoute(AgencyAndId routeId) {
    if (routeId != null && routeId.hasValues()) {
      if (!isValidAgency(routeId.getAgencyId())) {
        return false;
      }
      Boolean result = _cache.getRoute(routeId.toString());
      if (result != null) {
        return result;
      }
      if (this._transitDataService.getRouteForId(routeId.toString()) != null) {
        _cache.putRoute(routeId.toString(), Boolean.TRUE);
        return true;
      }
      _cache.putRoute(routeId.toString(), Boolean.FALSE);
    }
    return false;
  }

  protected boolean isValidStop(AgencyAndId stopId) {
    try {
      return (getCachedStopBean(stopId.toString()) != null);

    } catch (Exception e) {
      // This means the stop id is not valid.
    }
    return false;
  }

  // PROCESSSING METHODS
  
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

  protected <E> boolean processRouteIds(String routeVal,
      List<AgencyAndId> routeIds, List<String> agencyIds, Body<E> body,
      boolean handleErrors) {
    if (routeVal != null) {
      try {
        AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(routeVal);
        if (this.isValidRoute(routeId))
          routeIds.add(routeId);
      } catch (IllegalStateException e) {
        for (String agency : agencyIds) {
          AgencyAndId routeId = new AgencyAndId(agency, routeVal);
          if (this.isValidRoute(routeId)) {
            routeIds.add(routeId);
          }
        }
      }

      if (handleErrors && routeIds.size() == 0) {
        String agencyId = agencyIds.get(0);
        body.getErrors().add(
            new BodyError(ErrorMsg.ROUTE_UNAVAILABLE.getDescription(),
                agencyId, routeVal));
        return false;
      }
    } else {
      if (handleErrors) {
        body.getErrors().add(
            new BodyError(ErrorMsg.ROUTE_NULL.getDescription(),
                agencyIds.get(0)));
      }
      return false;
    }
    return true;
  }

  protected <E> boolean processRouteIds(String routeVal,
      List<AgencyAndId> routeIds, List<String> agencyIds, Body<E> body) {
    return processRouteIds(routeVal, routeIds, agencyIds, body, true);
  }

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
              new BodyError(ErrorMsg.STOP_INVALID.getDescription(),
                  stopId.toString()));
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
              new BodyError(ErrorMsg.STOP_INVALID.getDescription(), stopIdVal));
          return false;
        }
      }
      return true;

    } else {
      body.getErrors().add(new BodyError(ErrorMsg.STOP_S_NULL.getDescription()));
      return false;
    }
  }

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

  // HELPER METHODS
  
  protected String getMappedAgency(String agencyId) {
    if (!_configMapUtil.containsId(agencyId)) return agencyId;
    if (_configMapUtil.getConfig(agencyId).getAgencyMapper().containsKey(agencyId.toUpperCase()))
      return _configMapUtil.getConfig(agencyId).getAgencyMapper().get(agencyId.toUpperCase());
    return agencyId;
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

  protected String getIdNoAgency(String id) {
    String[] agencyAndId = id.split("_");
    if (agencyAndId != null && agencyAndId.length == 2) {
      return agencyAndId[1];
    }
    return id;
  }

  public boolean hasServiceUrl(String gtfsAgencyId) {
    return _configMapUtil.getConfig(gtfsAgencyId) != null;
  }

  public String getServiceUrl(String gtfsAgencyId) {
    ConfigurationUtil configUtil = _configMapUtil.getConfig(gtfsAgencyId);
    if (configUtil == null) {
      return null;
    }

    String host = configUtil.getTransiTimeHost();
    String port = configUtil.getTransiTimePort();
    String apiKey = configUtil.getTransiTimeKey();
    String serviceUrl = "http://" + host + ":" + port + "/api/v1/key/" + apiKey
        + "/agency/";
    return serviceUrl;
  }

  protected boolean isTimely(long timestamp) {
    if (System.currentTimeMillis() - timestamp > MAX_HEADER_AGE)
      return false;
    return true;
  }



}
