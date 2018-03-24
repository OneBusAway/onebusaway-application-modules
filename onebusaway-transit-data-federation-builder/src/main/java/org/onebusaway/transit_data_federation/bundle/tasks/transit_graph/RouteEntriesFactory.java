/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.Collection;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouteEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(RouteEntriesFactory.class);

  private UniqueService _uniqueService;

  private GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  public void processRoutes(TransitGraphImpl graph) {

    Collection<Route> routes = _gtfsDao.getAllRoutes(); 
    int numRoutes = routes.size();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(numRoutes);
    int routeIndex = 0;

    for (Route route : routes) {
    	if (routeIndex % logInterval == 0 ){
    		_log.info("route processed: " + routeIndex + "/" + numRoutes);
    	}
      routeIndex++;
      processRoute(graph, route);
    }

    graph.refreshRouteMapping();
  }

  private RouteEntryImpl processRoute(TransitGraphImpl graph, Route route) {
    RouteEntryImpl routeEntry = new RouteEntryImpl();
    routeEntry.setId(unique(route.getId()));
    routeEntry.setType(route.getType());
    graph.putRouteEntry(routeEntry);
    return routeEntry;
  }

  private <T> T unique(T value) {
    return _uniqueService.unique(value);
  }
}
