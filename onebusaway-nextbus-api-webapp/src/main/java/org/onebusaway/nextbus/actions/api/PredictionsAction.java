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

import java.lang.reflect.Type;
import java.util.*;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.RouteStopId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.transiTime.Prediction;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.onebusaway.nextbus.model.transiTime.PredictionsDirection;
import org.onebusaway.nextbus.util.HttpUtil;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
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

  List<String> _agencies = new ArrayList<String>();
  List<AgencyAndId> _stopIds;
  List<AgencyAndId> _routeIds;

  Map<String, Set<RouteStopId>>  _agencyRouteIdStopIdMap = new HashMap<>();

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

  public Body<Predictions> getModel() {

    Body<Predictions> body = new Body<Predictions>();

    if (isValid(body) && hasServiceUrl(agencyId)) {

      List<Predictions> allPredictions = new ArrayList<>();

      try {

        for (Map.Entry<String, Set<RouteStopId>> entry : _agencyRouteIdStopIdMap.entrySet()) {

          String agencyId = entry.getKey();

          String serviceUrl = getServiceUrl(agencyId) + agencyId + PREDICTIONS_COMMAND + "?";

          String routeStop = "";

          Set<RouteStopId> routeStopIds = entry.getValue();

          for(RouteStopId routeStopId: routeStopIds){
            if (isValidRoute(routeStopId.getRouteId())) {
              routeStop += "rs=" + getIdNoAgency(routeStopId.getRouteId().toString()) + "|"
                      + getIdNoAgency(routeStopId.getStopId().toString()) + "&";
            }
          }

          String uri = serviceUrl + routeStop + "format=" + REQUEST_TYPE;
          _log.info(uri);


            int timeout = _configMapUtil.getConfig(agencyId).getHttpTimeoutSeconds();
            JsonArray predictionsJson = _httpUtil.getJsonObject(uri, timeout).getAsJsonArray(
                    "predictions");
            Type listType = new TypeToken<List<Predictions>>() {
            }.getType();

            List<Predictions> predictions = new Gson().fromJson(predictionsJson, listType);
            allPredictions.addAll(predictions);

        }
        modifyJSONObject(allPredictions);
        body.getResponse().addAll(allPredictions);
      }
      catch (Exception e) {
          body.getErrors().add(new BodyError("No valid results found."));
          _log.error(e.getMessage());
      }
    }

    return body;

  }

  private void modifyJSONObject(List<Predictions> predictions) {

    String agencyTitle = getCachedAgencyBean(agencyId).getName();

    for (Predictions prediction : predictions) {
      for (PredictionsDirection direction : prediction.getDest()) {
        for (Prediction dirPrediction : direction.getPred()) {
          dirPrediction.setDirTag(direction.getDir());
        }
      }

      prediction.setAgencyTitle(agencyTitle);
    }
  }

  private boolean isValid(Body body) {
    if (!isValidAgency(body, agencyId))
      return false;

    _agencies.add(agencyId);

    _stopIds = new ArrayList<>(_tdsMappingService.getStopIdsFromStopCode(getStopId()));

    if (!processStopIds(getStopId(), _stopIds, _agencies, body))
      return false;

    for(AgencyAndId stopId : _stopIds){

      StopBean stopBean = getCachedStopBean(stopId.toString());

      if (routeTag == null) {
        processRouteStopIdsNoRouteTag(stopId, stopBean);
      }
      else {
        if (!processRouteIds(getRouteTag(), _routeIds, _agencies, body))
          return false;

        boolean stopServesRoute = processRouteStopIdsWithRouteTag(stopId, stopBean);

        if (!stopServesRoute) {
          body.getErrors().add(
                  new BodyError(ErrorMsg.ROUTE_UNAVAILABLE.getDescription(),
                          agencyId, routeTag));
          return false;
        }
      }

    }
    return true;
  }

  private void processRouteStopIdsNoRouteTag(AgencyAndId stopId, StopBean stopBean){
    for (RouteBean routeBean : stopBean.getRoutes()) {
      String agencyId = routeBean.getAgency().getId();
      AgencyAndId routeId = AgencyAndId.convertFromString(routeBean.getId());
      if(_agencyRouteIdStopIdMap.get(agencyId) == null){
        _agencyRouteIdStopIdMap.put(agencyId, new HashSet<RouteStopId>());
      }
      Set<RouteStopId> routeStopIds = _agencyRouteIdStopIdMap.get(agencyId);
      routeStopIds.add(new RouteStopId(routeId, stopId));
    }
  }

  private boolean processRouteStopIdsWithRouteTag(AgencyAndId stopId, StopBean stopBean){
    boolean stopServesRoute = false;
    for (RouteBean routeBean : stopBean.getRoutes()) {
      String agencyId = routeBean.getAgency().getId();
      AgencyAndId routeId = AgencyAndId.convertFromString(routeBean.getId());
      if (_routeIds.contains(routeId)) {
        if(_agencyRouteIdStopIdMap.get(agencyId) == null){
          _agencyRouteIdStopIdMap.put(agencyId, new HashSet<RouteStopId>());
        }
        Set<RouteStopId> routeStopIds = _agencyRouteIdStopIdMap.get(agencyId);
        routeStopIds.add(new RouteStopId(routeId, stopId));

        stopServesRoute = true;
      }
    }
    return stopServesRoute;
  }

}
