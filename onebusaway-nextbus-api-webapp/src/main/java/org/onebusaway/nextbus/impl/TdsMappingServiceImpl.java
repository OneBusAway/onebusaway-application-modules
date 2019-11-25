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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.impl.util.ConfigurationMapUtil;
import org.onebusaway.transit_data.model.*;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.nextbus.service.TdsMappingService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TdsMappingServiceImpl implements TdsMappingService {

  @Autowired
  private ThreadPoolTaskScheduler _taskScheduler;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  ConfigurationMapUtil _configurationMapUtil;

  private ConcurrentHashMap<String, String> routeShortNameToRouteIdMap = new ConcurrentHashMap<String, String>(
      2000);

  private ConcurrentHashMap<String, Set<AgencyAndId>> stopCodeToStopIdMap = new ConcurrentHashMap<String, Set<AgencyAndId>>(
      20000);

  private static Logger _log = LoggerFactory.getLogger(TdsMappingServiceImpl.class);

  @PostConstruct
  public void setup() {
    final RefreshDataTask refreshDataTask = new RefreshDataTask();
    _taskScheduler.scheduleWithFixedDelay(refreshDataTask, 60 * 60 * 1000); // every hour
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

  public Set<AgencyAndId> getStopIdsFromStopCode(String code) {
    if (code != null
        && stopCodeToStopIdMap.containsKey(code.toUpperCase()))
      return stopCodeToStopIdMap.get(code.toUpperCase());

    Set<AgencyAndId> codeSet = new HashSet<>();
    String agencyId = _configurationMapUtil.getDefaultAgencyId();
    codeSet.add(new AgencyAndId(agencyId, code));

    return codeSet;
  }

  private class RefreshDataTask implements Runnable {

    @Override
    public void run() {
      try {
        String defaultAgencyId = _configurationMapUtil.getDefaultAgencyId();
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
          List<StopBean> stopsList  = _transitDataService.getAllRevenueStops(agency);

          Map<String, List<AgencyAndId>> stopIdsMap = getStopIdToConsolidatedStopIdsMap();

          for (StopBean stop : stopsList) {
            Set<AgencyAndId> stopIds = stopCodeToStopIdMap.get(stop.getCode().toUpperCase());
            if(stopIds == null){
              stopIds = new HashSet<>();
              stopCodeToStopIdMap.put(stop.getCode().toUpperCase(), stopIds);
            }
            stopIds.add(AgencyAndId.convertFromString(stop.getId()));
            List<AgencyAndId> consolidatedStopIds = stopIdsMap.get(stop.getId());
            if(consolidatedStopIds !=null) {
              stopIds.addAll(consolidatedStopIds);
            }
          }
          _log.info("mapped stop codes to stop ids");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private Map<String, List<AgencyAndId>> getStopIdToConsolidatedStopIdsMap(){
      ListBean<ConsolidatedStopMapBean> conslidatedStopsBean = _transitDataService.getAllConsolidatedStops();
      Map<String, List<AgencyAndId>> consolidatedStopIds = new HashMap<>();
      List<ConsolidatedStopMapBean> consolidatedStops =  conslidatedStopsBean.getList();
      for(ConsolidatedStopMapBean csmb: consolidatedStops){
        consolidatedStopIds.put(csmb.getConsolidatedStopId().toString(), csmb.getHiddenStopIds());
        _log.info("Stop Id: " + csmb.getConsolidatedStopId() + ", Consolidated Stop Ids:" + csmb.getHiddenStopIds());
      }
      return consolidatedStopIds;
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
