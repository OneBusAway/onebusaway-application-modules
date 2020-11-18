/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Given an instantiated and running instance of a federated transit data
 * bundle, we pre-cache a number of expensive data operations so that the cache
 * will be hot for eventual deployment.
 * 
 * That includes caching {@link StopBean}, {@link RouteBean} and
 * {@link StopsForRouteBean} objects, which in turn cache a number of other
 * things.
 * 
 * @author bdferris
 * 
 */
public class PreCacheTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(PreCacheTask.class);

  private TransitDataService _service;

  private CacheManager _cacheManager;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }

  @Autowired
  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Override
  public void run() {

    // Clear all existing cache elements
    for (String cacheName : _cacheManager.getCacheNames()) {
      Cache cache = _cacheManager.getCache(cacheName);
      cache.removeAll();
    }

    try {
      List<AgencyWithCoverageBean> agenciesWithCoverage = _service.getAgenciesWithCoverage();

      for (AgencyWithCoverageBean agencyWithCoverage : agenciesWithCoverage) {

        AgencyBean agency = agencyWithCoverage.getAgency();
        System.out.println("agency=" + agency.getId());

        ListBean<String> stopIds = _service.getStopIdsForAgencyId(agency.getId());
        for (String stopId : stopIds.getList()) {
          System.out.println("  stop=" + stopId);
          _service.getStop(stopId);
        }

        ListBean<String> routeIds = _service.getRouteIdsForAgencyId(agency.getId());
        for (String routeId : routeIds.getList()) {
          System.out.println("  route=" + routeId);
          _service.getStopsForRoute(routeId);
        }
      }

      Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();
      for (TripEntry trip : _transitGraphDao.getAllTrips()) {
        AgencyAndId shapeId = trip.getShapeId();
        if (shapeId != null && shapeId.hasValues())
          shapeIds.add(shapeId);
      }

      for (AgencyAndId shapeId : shapeIds) {
        System.out.println("shape=" + shapeId);
        _service.getShapeForId(AgencyAndIdLibrary.convertToString(shapeId));
      }
    } catch (ServiceException ex) {
      _log.error("service exception", ex);
    }

  }
}
