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
package org.onebusaway.nextbus.impl;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;

import org.onebusaway.nextbus.service.RouteCacheService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class RouteCacheServiceImpl implements RouteCacheService {

  @Autowired
  private ThreadPoolTaskScheduler _taskScheduler;

  @Autowired
  private TransitDataService _transitDataService;

  private ConcurrentHashMap<String, String> routeShortNameToRouteIdMap = new ConcurrentHashMap<String, String>(
      200);

  private static Logger _log = LoggerFactory.getLogger(RouteCacheServiceImpl.class);

  @PostConstruct
  public void setup() {
    final RefreshDataTask refreshDataTask = new RefreshDataTask();
    _taskScheduler.scheduleWithFixedDelay(refreshDataTask, 60 * 60 * 1000); // every hour
  }
  
  public ConcurrentHashMap<String, String> getRouteShortNameToRouteIdMap(){
    return routeShortNameToRouteIdMap;
  }
  
  public String getRouteShortNameFromId(String id){
    if(routeShortNameToRouteIdMap.containsKey(id))
      return routeShortNameToRouteIdMap.get(id);
    return id;
  }
  
  private class RefreshDataTask implements Runnable {

    @Override
    public void run() {
      try {
        for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
          String agencyId = agency.getAgency().getId();
          ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agencyId);
          for (RouteBean route : routes.getList()) {
            routeShortNameToRouteIdMap.put(route.getShortName().toUpperCase(),
                route.getId());
          }
          _log.info("processed routes");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
