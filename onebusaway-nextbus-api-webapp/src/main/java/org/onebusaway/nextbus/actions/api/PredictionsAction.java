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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.JsonObject;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.exceptions.*;
import org.onebusaway.nextbus.impl.util.ConfigurationUtil;
import org.onebusaway.nextbus.model.RouteStopId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.transiTime.Prediction;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.onebusaway.nextbus.model.transiTime.PredictionsDirection;
import org.onebusaway.nextbus.service.TdsMappingService;
import org.onebusaway.nextbus.service.cache.TdsCacheService;
import org.onebusaway.nextbus.service.validators.AgencyValidator;
import org.onebusaway.nextbus.service.validators.RouteValidator;
import org.onebusaway.nextbus.service.validators.StopValidator;
import org.onebusaway.nextbus.util.HttpUtil;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class PredictionsAction extends NextBusApiBase implements
    ModelDriven<Body<Predictions>> {

  private static Logger _log = LoggerFactory.getLogger(PredictionsAction.class);

  @Autowired
  private HttpUtil _httpUtil;

  @Autowired
  private AgencyValidator _agencyValidator;

  @Autowired
  private StopValidator _stopValidator;

  @Autowired
  private RouteValidator _routeValidator;

  @Autowired
  private TdsMappingService _tdsMappingService;

  @Autowired
  private TdsCacheService _tdsCacheService;

  // Next Bus API vars
  private String agencyId;
  private String stopId;
  private String routeTag;

  // agency param
  public String getA() {
    return agencyId;
  }
  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  // short form of stopId param
  public String getS() {
    return stopId;
  }
  public void setS(String stopId) {
    this.stopId = stopId;
  }

  // long form of stopId param
  public String getStopId() { return stopId; }
  public void setStopId(String stopId) { this.stopId = stopId; }

  // short form of routeTag param
  public String getR() {
    return routeTag;
  }
  public void setR(String routeTag) {
    this.routeTag = _tdsMappingService.getRouteIdFromShortName(routeTag);
  }

  // long form of routeTag param
  public String getRouteTag() {
    return routeTag;
  }
  public void setRouteTag(String routeTag) {
    this.routeTag = _tdsMappingService.getRouteIdFromShortName(routeTag);
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }
  
  public String execute() {
	  return SUCCESS;
  }


  public Body getPredictions(){
    Body body = new Body();

    String agencyId = getA();
    String route = getR();
    String stop = getS();

    Set<AgencyAndId> stopIds;
    Set<AgencyAndId> routeIds = new HashSet<>();
    Map<String, Set<RouteStopId>> agencyRouteIdStopIdMap = new HashMap<>();
    List<Predictions> allPredictions = new ArrayList<>();

    try {
      // Agencies
      _agencyValidator.validate(agencyId);
      if(!hasServiceUrl(agencyId)){
        throw new AgencyInvalidException(agencyId);
      }

      // Stops
      AgencyAndId stopCode = getStopAndId(agencyId, stop);
      stopIds = _tdsMappingService.getStopIdsFromStopCode(stopCode.getId());
      _stopValidator.validate(stopCode, stopIds);

      // Routes
      if(route != null) {
        AgencyAndId routeId = getRouteAndId(agencyId, route);
        _routeValidator.validate(routeId);
        routeIds.add(routeId);
      }
      processRoutes(agencyId, route, stopIds, routeIds, agencyRouteIdStopIdMap);

      for (Map.Entry<String, Set<RouteStopId>> entry : agencyRouteIdStopIdMap.entrySet()) {
        String uri = buildPredictionsUrl(_configMapUtil.getConfig(agencyId), entry.getKey(), entry.getValue());
        allPredictions.addAll(getRemotePredictions(uri));
      }

      modifyJSONObject(allPredictions);
      body.getResponse().addAll(allPredictions);

    } catch (AgencyNullException ane) {
      body.getErrors().add(new BodyError(ane.getMessage()));
    } catch (AgencyInvalidException aie) {
      body.getErrors().add(new BodyError(aie.getMessage(), aie.getAgencyId()));
    } catch (StopNullException sne) {
      body.getErrors().add(new BodyError(sne.getMessage()));
    } catch (StopInvalidException sie) {
      body.getErrors().add(new BodyError(sie.getMessage(), sie.getStop()));
    } catch (RouteNullException rne) {
      body.getErrors().add(new BodyError(rne.getMessage(), rne.getAgencyId()));
    } catch (RouteUnavailableException rue) {
      body.getErrors().add(new BodyError(rue.getMessage(), rue.getAgency(), rue.getRoute()));
    } catch (Exception e) {
      body.getErrors().add(new BodyError(ErrorMsg.DEFAULT_ERROR.getDescription()));
      _log.error(e.getMessage(), e);
    }
    return body;
  }

  private AgencyAndId getStopAndId(String agencyId, String stop){
    try {
      return AgencyAndIdLibrary.convertFromString(stop);
    } catch (IllegalStateException ise){
      return new AgencyAndId(agencyId, stop);
    } catch (Exception e){
      return null;
    }
  }

  private AgencyAndId getRouteAndId(String agencyId, String route){
    try {
      return AgencyAndIdLibrary.convertFromString(route);
    } catch (IllegalStateException ise){
      return new AgencyAndId(agencyId, route);
    } catch (Exception e){
      return null;
    }
  }

  private void processRoutes(String agencyId, String route, Set<AgencyAndId> stopIds, Set<AgencyAndId> routeIds,
                             Map<String, Set<RouteStopId>> agencyRouteIdStopIdMap) throws RouteUnavailableException {

    boolean noRouteSet = routeIds.size() == 0;

    for(AgencyAndId stopId : stopIds){
      StopBean stopBean = _tdsCacheService.getCachedStopBean(stopId.toString());

      for (RouteBean routeBean : stopBean.getRoutes()) {
        AgencyAndId routeId = AgencyAndId.convertFromString(routeBean.getId());
        boolean stopRouteAgenciesMatch = routeId.getAgencyId().equals(stopId.getAgencyId()) && agencyId.equals(routeId.getAgencyId());
        if((noRouteSet || routeIds.contains(routeId)) && stopRouteAgenciesMatch){
          processAgencyRouteStop(agencyRouteIdStopIdMap, routeId, stopId);
        }
      }
    }

    if(!noRouteSet && agencyRouteIdStopIdMap.size() == 0){
      throw new RouteUnavailableException(agencyId, route);
    }
  }

  private void processAgencyRouteStop(Map<String, Set<RouteStopId>> agencyRouteIdStopIdMap,
                                      AgencyAndId routeId, AgencyAndId stopId){
    if(agencyRouteIdStopIdMap.get(routeId.getAgencyId()) == null){
      agencyRouteIdStopIdMap.put(routeId.getAgencyId(), new HashSet<RouteStopId>());
    }
    Set<RouteStopId> routeStopIds = agencyRouteIdStopIdMap.get(routeId.getAgencyId());
    routeStopIds.add(new RouteStopId(routeId, stopId));
  }

  private String buildPredictionsUrl(ConfigurationUtil config, String agencyId, Set<RouteStopId> routeStopIds){
      String serviceUrl = getServiceUrl(agencyId, PREDICTIONS_COMMAND) + "?";
      StringBuilder routeStop = new StringBuilder();

      toString(routeStop, config, routeStopIds);


      String uri = serviceUrl + routeStop.toString() + "format=" + REQUEST_TYPE;
      _log.info(uri);

      return uri;
  }

  private void toString(StringBuilder sb, ConfigurationUtil config, Set<RouteStopId> routeStopIds) {
    if (config.getBaseUrlOverride() == null) {
      // format I: Transitime/TTC
      // http://localhost:8080/api/v1/key/prod3273b0/agency/1/command/predictions?rs=X2|5988&format=json
      for(RouteStopId routeStopId: routeStopIds) {
        sb.append(toString(routeStopId));
      }
    } else {
      // format II.a: External API
      // https://localhost/real-time/wmata/predictions?stop=5988&format=json
      sb.append("stop=");
      sb.append(routeStopIds.iterator().next().getStopId().getId());
      sb.append("&");

      // format II.b: External API
      // https://localhost/real-time/wmata/predictions?stop=5988&route=X2&stop=5988&format=json
      // TODO:  not supported
    }

  }

  private String toString(RouteStopId routeStopId) {
    StringBuilder routeStop = new StringBuilder();
    routeStop.append("rs=");
    routeStop.append(routeStopId.getRouteId().getId());
    routeStop.append("|");
    routeStop.append(routeStopId.getStopId().getId());
    routeStop.append("&");
    return routeStop.toString();
  }

  private List<Predictions> getRemotePredictions(String uri) throws IOException {
    int timeout = _configMapUtil.getConfig(agencyId).getHttpTimeoutSeconds();
    Map<String, String> headersMap = _configMapUtil.getConfig(agencyId).getHeadersMap();
    JsonArray predictionsJson = null;
    JsonObject jsonObject = _httpUtil.getJsonObject(uri, timeout, headersMap);
    if (jsonObject.has("predictions")) {
      predictionsJson = jsonObject.getAsJsonArray("predictions");
    } else if (jsonObject.has("pred")) {
      predictionsJson = jsonObject.getAsJsonArray("pred");
    }
    Type listType = new TypeToken<List<Predictions>>() {
    }.getType();

    List<Predictions> predictions = new Gson().fromJson(predictionsJson, listType);
    if (predictions == null) {
      _log.error("unexpected result did not yield predictions for {}: {}", uri, predictionsJson);
      return new ArrayList<>();
    }
    return predictions;
  }

  private void modifyJSONObject(List<Predictions> predictions) {
    String agencyTitle = _tdsCacheService.getCachedAgencyBean(agencyId).getName();

    for (Predictions prediction : predictions) {
        for (PredictionsDirection direction : prediction.getDest()) {
            for (Prediction dirPrediction : direction.getPred()) {
                dirPrediction.setDirTag(direction.getDir());
            }
        }

        prediction.setAgencyTitle(agencyTitle);
    }
  }


  public Body<Predictions> getModel() {
    Body<Predictions> body = getPredictions();
    return body;
  }

}
