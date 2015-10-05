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
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class PredictionsAction extends NextBusApiBase implements
    ModelDriven<Body<List<Predictions>>> {

  private static Logger _log = LoggerFactory.getLogger(PredictionsAction.class);

  private String agencyId;

  private String stopId;

  private String routeTag;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getRouteTag() {
    return routeTag;
  }

  public void setRouteTag(String routeTag) {
    this.routeTag = _routeCacheService.getRouteShortNameFromId(routeTag);
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  public Body<List<Predictions>> getModel() {

    Body<List<Predictions>> body = new Body<List<Predictions>>();
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();

    if (isValid(body, stopIds, routeIds)) {

      String serviceUrl = getServiceUrl() + agencyId + PREDICTIONS_COMMAND
          + "?";
      
      String routeStop="";
      
      for(AgencyAndId routeId : routeIds){
        routeStop += "rs=" + getIdNoAgency(routeId.toString()) + "|" + stopId + "&";
      }
      String uri = serviceUrl + routeStop + "format=" + REQUEST_TYPE;

      try {
        
        JsonArray predictionsJson = getJsonObject(uri).getAsJsonArray(
            "predictions");
        Type listType = new TypeToken<List<Predictions>>() {
        }.getType();
        List<List<Predictions>> predictions = new Gson().fromJson(
            predictionsJson, listType);

        body.getResponse().addAll(predictions);
      } catch (Exception e) {

        _log.error(e.getMessage());
      }
    }

    return body;

  }

  private boolean isValid(Body body, List<AgencyAndId> stopIds,
      List<AgencyAndId> routeIds) {
    if (!isValidAgency(body, agencyId))
      return false;

    List<String> agencies = new ArrayList<String>();
    agencies.add(agencyId);

    if (!processStopIds(stopId, stopIds, agencies, body))
      return false;

    if (routeTag == null){
      StopBean stopBean = _transitDataService.getStop(stopIds.get(0).toString());
      for (RouteBean routeBean : stopBean.getRoutes()) {
        routeIds.add(AgencyAndId.convertFromString(routeBean.getId()));
      }
    }
    else if(!processRouteIds(routeTag, routeIds, agencies, body)) { 
      return false;
    }

    return true;
  }
}
