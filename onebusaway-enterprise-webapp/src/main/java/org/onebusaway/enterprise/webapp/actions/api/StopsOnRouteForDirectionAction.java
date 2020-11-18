/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.api.model.StopOnRoute;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import org.onebusaway.util.AgencyAndIdLibrary;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})
public class StopsOnRouteForDirectionAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private ConfigurationService _configService;

  private List<StopOnRoute> _stops = new ArrayList<StopOnRoute>();

  private String _routeId = null;

  private String _directionId = null;

  public void setRouteId(String routeId) {
    _routeId = routeId;
  }

  public void setDirectionId(String directionId) {
    _directionId = directionId;
  }

  @Override
  public String execute() {    
    if(_routeId == null) {
      return SUCCESS;
    }

    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    StopsForRouteBean stopsForRoute;
    if (serviceDateFilterOn) {
      stopsForRoute = _transitDataService.getStopsForRouteForServiceDate(_routeId, new ServiceDate(new Date(SystemTime.currentTimeMillis())));
    }
    else {
      stopsForRoute = _transitDataService.getStopsForRoute(_routeId);
    }

    // create stop ID->stop bean map
    Map<String, StopBean> stopIdToStopBeanMap = new HashMap<String, StopBean>();
    for(StopBean stopBean : stopsForRoute.getStops()) {
      stopIdToStopBeanMap.put(stopBean.getId(), stopBean);
    }   
    
    // break up stops into destinations
    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGroupingBean : stopGroupings) {
      for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
        NameBean name = stopGroupBean.getName();
        String type = name.getType();

        if (!type.equals("destination") || !stopGroupBean.getId().equals(_directionId))
          continue;
        
        if(!stopGroupBean.getStopIds().isEmpty()) {
          for(String stopId : stopGroupBean.getStopIds()) {
            String agencyId = AgencyAndIdLibrary.convertFromString(_routeId).getAgencyId();
            if (_transitDataService.stopHasRevenueServiceOnRoute(agencyId, stopId,
                    stopsForRoute.getRoute().getId(), stopGroupBean.getId())) {
              _stops.add(new StopOnRoute(stopIdToStopBeanMap.get(stopId)));
            }
          }
        }
      }
    }
    
    return SUCCESS;
  }   

  /** 
   * VIEW METHODS
   */
  public List<StopOnRoute> getStops() {
    return _stops;
  }

}


