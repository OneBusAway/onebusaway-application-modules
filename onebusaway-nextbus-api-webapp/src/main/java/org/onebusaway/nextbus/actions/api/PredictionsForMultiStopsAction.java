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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.transiTime.Prediction;
import org.onebusaway.nextbus.model.transiTime.Predictions;
import org.onebusaway.nextbus.model.transiTime.PredictionsDirection;
import org.onebusaway.nextbus.util.HttpUtil;
import org.onebusaway.nextbus.util.HttpUtilImpl;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class PredictionsForMultiStopsAction extends NextBusApiBase implements
    ModelDriven<Body<Predictions>> {

  @Autowired
  private HttpUtil _httpUtil;

  private String agencyId;

  private Set<String> stops;

  private Set<String> mappedStops = new HashSet<String>();

  private String routeTag;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public Set<String> getStops() {
    return stops;
  }

  public void setStops(Set<String> stops) {
    this.stops = stops;
  }

  public String getRouteTag() {
    return routeTag;
  }

  public void setRouteTag(String routeTag) {
    this.routeTag = _tdsMappingService.getRouteIdFromShortName(routeTag);
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  public Body<Predictions> getModel() {

    Body<Predictions> body = new Body<Predictions>();

    if (isValid(body)) {
      String serviceUrl = getServiceUrl() + agencyId + PREDICTIONS_COMMAND
          + "?";
      String routeStopIds = getStopParams();
      String uri = serviceUrl + routeStopIds + "format=" + REQUEST_TYPE;

      try {
        int timeout = _configUtil.getHttpTimeoutSeconds();
        JsonArray predictionsJson = _httpUtil.getJsonObject(uri, timeout).getAsJsonArray(
            "predictions");
        Type listType = new TypeToken<List<Predictions>>() {
        }.getType();

        List<Predictions> predictions = new Gson().fromJson(predictionsJson,
            listType);

        modifyJSONObject(predictions);

        body.getResponse().addAll(predictions);
      } catch (Exception e) {

        body.getErrors().add(new BodyError("No valid results found."));
      }
    }

    return body;

  }

  private void modifyJSONObject(List<Predictions> predictions) {
    for (Predictions prediction : predictions) {
      for (PredictionsDirection direction : prediction.getDest()) {
        for (Prediction dirPrediction : direction.getPred()) {
          dirPrediction.setDirTag(direction.getDir());
        }
      }
    }
  }

  private String getStopIdParams() {
    StringBuilder sb = new StringBuilder();
    for (String stopId : stops) {
      StopBean stopBean = _transitDataService.getStop(stopId);
      for (RouteBean routeBean : stopBean.getRoutes()) {
        sb.append("rs=");
        sb.append(routeBean.getId());
        sb.append("|");
        sb.append(stopId);
        sb.append("&");
      }
    }
    return sb.toString();
  }

  private String getStopParams() {
    StringBuilder sb = new StringBuilder();
    for (String stop : mappedStops) {
      sb.append("rs=");
      sb.append(stop);
      sb.append("&");
    }
    return sb.toString();
  }

  private boolean isValid(Body<Predictions> body) {

    if (!isValidAgency(body, agencyId)) {
      return false;
    }

    if (stops == null) {
      body.getErrors().add(
          new BodyError(ErrorMsg.STOP_STOPS_NULL.getDescription()));
      return false;
    }

    for (String stop : stops) {
      String[] stopArray = stop.split("\\|");
      if (stopArray.length < 2) {
        String error = "The stop "
            + stop
            + " is invalid because it did not contain a route, optional dir, and stop tag";
        body.getErrors().add(new BodyError(error));
        return false;
      }

      try {
        String stopId = _tdsMappingService.getStopIdFromStopCode(stopArray[1]);

        StopBean stopBean;

        try {
          stopBean = _transitDataService.getStop(stopId);
        } catch (ServiceException se) {
          // The user didn't provide an agency id in stopId, so use provided
          // agency
          String stopIdVal = new AgencyAndId(agencyId, stopId).toString();
          stopBean = _transitDataService.getStop(stopIdVal);
        }

        boolean routeExists = false;
        for (RouteBean routeBean : stopBean.getRoutes()) {
          if (routeBean.getId().equals(
              _tdsMappingService.getRouteIdFromShortName(stopArray[0]))) {
            routeExists = true;
            break;
          }
        }
        if (!routeExists) {
          String error = "For agency=" + getA() + " route r=" + stopArray[0]
              + " is not currently available. It might be initializing still.";
          body.getErrors().add(new BodyError(error));
          return false;
        }
        String routeTag = getIdNoAgency(_tdsMappingService.getRouteIdFromShortName(stopArray[0]));
        String routeStop = getIdNoAgency(_tdsMappingService.getStopIdFromStopCode(stopArray[1]));

        mappedStops.add(routeTag + "|" + routeStop);

      } catch (ServiceException se) {
        String error = "For agency=" + getA() + " stop s=" + stopArray[1]
            + " is on none of the directions for r=" + stopArray[0]
            + " so cannot determine which stop to provide data for.";

        body.getErrors().add(new BodyError(error));
        return false;
      }
    }
    return true;
  }
}
