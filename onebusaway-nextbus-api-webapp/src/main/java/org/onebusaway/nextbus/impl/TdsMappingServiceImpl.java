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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.service.TdsMappingService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TdsMappingServiceImpl implements TdsMappingService {

  @Autowired
  private ThreadPoolTaskScheduler _taskScheduler;

  @Autowired
  private TransitDataService _transitDataService;

  private ConcurrentHashMap<String, String> routeShortNameToRouteIdMap = new ConcurrentHashMap<String, String>(
      200);

  private ConcurrentHashMap<String, String> stopCodeToStopIdMap = new ConcurrentHashMap<String, String>(
      200);

  private static Logger _log = LoggerFactory.getLogger(TdsMappingServiceImpl.class);

  @PostConstruct
  public void setup() {
    final RefreshDataTask refreshDataTask = new RefreshDataTask();
    _taskScheduler.scheduleWithFixedDelay(refreshDataTask, 60 * 60 * 1000); // every
                                                                            // hour
  }

  public ConcurrentHashMap<String, String> getRouteShortNameToRouteIdMap() {
    return routeShortNameToRouteIdMap;
  }

  public ConcurrentHashMap<String, String> getStopCodeToStopIdMap() {
    return routeShortNameToRouteIdMap;
  }

  public String getRouteIdFromShortName(String shortName) {
    if (shortName != null
        && routeShortNameToRouteIdMap.containsKey(shortName.toUpperCase()))
      return routeShortNameToRouteIdMap.get(shortName.toUpperCase());
    return shortName;
  }

  public String getStopIdFromStopCode(String code) {
    if (code != null
        && stopCodeToStopIdMap.containsKey(code.toUpperCase()))
      return stopCodeToStopIdMap.get(code.toUpperCase());
    return code;
  }

  private class RefreshDataTask implements Runnable {

    @Override
    public void run() {
      try {
        for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
          String agencyId = agency.getAgency().getId();

          // Routes Mapping
          ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agencyId);
          for (RouteBean route : routes.getList()) {
            routeShortNameToRouteIdMap.put(route.getShortName().toUpperCase(),
                route.getId());
          }
          _log.info("mapped routes short names to route ids");

          // Stops Mapping
          SearchQueryBean query = new SearchQueryBean();
          query.setBounds(getAgencyBounds(agency));
          query.setMaxCount(Integer.MAX_VALUE);

          StopsBean stops = _transitDataService.getStops(query);
          List<StopBean> stopsList = stops.getStops();

          for (StopBean stop : stopsList) {
            stopCodeToStopIdMap.put(stop.getCode().toUpperCase(), stop.getId());
          }
          
          _log.info("mapped stop codes to stop ids");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private CoordinateBounds getAgencyBounds(AgencyWithCoverageBean agency) {
      CoordinateBounds bounds = new CoordinateBounds();

      double lat = agency.getLat();
      double lon = agency.getLon();
      double latSpan = agency.getLatSpan() / 2;
      double lonSpan = agency.getLonSpan() / 2;
      bounds.addPoint(lat - latSpan, lon - lonSpan);
      bounds.addPoint(lat + latSpan, lon + lonSpan);

      return bounds;
    }
  }

}
