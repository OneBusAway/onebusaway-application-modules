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

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.api.model.StopOnRoute;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParentPackage("json-default")
@Result(type="json", params={"callbackParameter", "callback"})
public class StopsOnRouteAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private TransitDataService _transitDataService;

  private List<StopOnRoute> _stops = new ArrayList<StopOnRoute>();

  private String _routeId = null;

  public void setRouteId(String routeId) {
    _routeId = routeId;
  }

  @Override
  public String execute() {    
    if(_routeId == null) {
      return SUCCESS;
    }
    
    StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(_routeId);

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


