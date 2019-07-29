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
import org.onebusaway.nextbus.model.transiTime.ScheduleRoute;
import org.onebusaway.nextbus.model.transiTime.ScheduleTableRow;
import org.onebusaway.nextbus.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opensymphony.xwork2.ModelDriven;

public class ScheduleHorizAction extends NextBusApiBase implements
    ModelDriven<Body<List<ScheduleRoute>>> {

  private static Logger _log = LoggerFactory.getLogger(ScheduleHorizAction.class);

  @Autowired
  private HttpUtil _httpUtil;

  private String agencyId;

  private String stopId;

  private String routeId;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public String getR() {
    return routeId;
  }

  public void setR(String routeId) {
    this.routeId = _tdsMappingService.getRouteIdFromShortName(routeId);
  }

  public DefaultHttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  public Body<List<ScheduleRoute>> getModel() {

    Body<List<ScheduleRoute>> body = new Body<List<ScheduleRoute>>();
    List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();

    if (isValid(body, routeIds) && hasServiceUrl(agencyId)) {

      String serviceUrl = getServiceUrl(agencyId) + agencyId + SCHEDULE_COMMAND + "?";
      String route = "r=" + getIdNoAgency(routeId);
      String uri = serviceUrl + route + "&format=" + REQUEST_TYPE;

      try {
        int timeout = _configMapUtil.getConfig(agencyId).getHttpTimeoutSeconds();
        JsonArray scheduleJson = _httpUtil.getJsonObject(uri, timeout).getAsJsonArray(
            "schedule");
        Type listType = new TypeToken<List<ScheduleRoute>>() {
        }.getType();
        List<ScheduleRoute> schedules = new Gson().fromJson(scheduleJson,
            listType);
        
        modifyJSONObject(schedules);

        body.getResponse().add(schedules);
      } catch (Exception e) {
        _log.error(e.getMessage());
      }
    }

    return body;

  }

  private void modifyJSONObject(List<ScheduleRoute> schedules) {

	    String agencyTitle = getCachedAgencyBean(agencyId).getName();
	    
	    for (ScheduleRoute scheduleRoute : schedules) {
	      scheduleRoute.setRouteName(scheduleRoute.getRouteId() + " " + scheduleRoute.getRouteName());
        
        //Stop Times
        for(ScheduleTableRow tableRow : scheduleRoute.getTimesForTrip()){
          
          for(int i = 0; i < tableRow.getTime().size(); i++){
            tableRow.getTime().get(i).setTag(scheduleRoute.getStop().get(i).getStopId());
          }
        }
	    }
	  }
  

  private boolean isValid(Body body, List<AgencyAndId> routeIds) {
    if (!isValidAgency(body, agencyId))
      return false;

    List<String> agencies = new ArrayList<String>();
    agencies.add(agencyId);

    if (!processRouteIds(routeId, routeIds, agencies, body))
      return false;

    return true;
  }
}
