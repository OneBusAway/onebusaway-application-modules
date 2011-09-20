/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.impl.transit_graph.AgencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteCollectionEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Compute {@link RouteCollectionEntry} objects from {@link RouteEntry}
 * instances. Recall that route collections are virtual route objects that
 * bundle potentially multiple underlying {@link RouteEntry} objects with the
 * same short name, depending on the setting of
 * {@link #setGroupRoutesByShortName(boolean)}.
 * 
 * This addresses the fact that some agencies break up what riders would
 * consider a single route into multiple route entries according to their own
 * organizational methods (different direction, different levels of service,
 * express vs local, etc).
 * 
 * @author bdferris
 */
@Component
public class RouteCollectionEntriesFactory {

  private GtfsRelationalDao _gtfsDao;

  private UniqueService _uniqueService;

  private boolean _groupRoutesByShortName = false;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  public void setGroupRoutesByShortName(boolean groupRoutesByShortName) {
    _groupRoutesByShortName = groupRoutesByShortName;
  }

  public void processRouteCollections(TransitGraphImpl graph) {

    if (_groupRoutesByShortName) {
      createRouteShortNameRouteCollectionMapping(graph);
    } else {
      createOneToOneRouteCollectionMapping(graph);
    }

    groupRouteCollectionsByAgencyId(graph);
    graph.refreshRouteCollectionMapping();
  }

  private void groupRouteCollectionsByAgencyId(TransitGraphImpl graph) {

    Map<String, ArrayList<RouteCollectionEntry>> entriesByAgencyId = new FactoryMap<String, ArrayList<RouteCollectionEntry>>(
        new ArrayList<RouteCollectionEntry>());
    for (RouteCollectionEntry entry : graph.getAllRouteCollections()) {
      String agencyId = entry.getId().getAgencyId();
      entriesByAgencyId.get(agencyId).add(entry);
    }

    for (Map.Entry<String, ArrayList<RouteCollectionEntry>> entry : entriesByAgencyId.entrySet()) {
      String agencyId = entry.getKey();
      ArrayList<RouteCollectionEntry> routeCollections = entry.getValue();
      routeCollections.trimToSize();
      AgencyEntryImpl agencyEntry = graph.getAgencyForId(agencyId);
      agencyEntry.setRouteCollections(routeCollections);
    }
  }

  private void createOneToOneRouteCollectionMapping(TransitGraphImpl graph) {
    for (RouteEntryImpl routeEntry : graph.getRoutes()) {
      RouteCollectionEntryImpl routeCollectionEntry = new RouteCollectionEntryImpl();
      routeCollectionEntry.setId(routeEntry.getId());
      ArrayList<RouteEntry> routes = new ArrayList<RouteEntry>();
      routes.add(routeEntry);
      routes.trimToSize();
      routeCollectionEntry.setChildren(routes);
      graph.putRouteCollectionEntry(routeCollectionEntry);
      routeEntry.setParent(routeCollectionEntry);
    }
  }

  private void createRouteShortNameRouteCollectionMapping(TransitGraphImpl graph) {

    Map<AgencyAndId, List<RouteEntryImpl>> routesByKey = new HashMap<AgencyAndId, List<RouteEntryImpl>>();

    for (RouteEntryImpl routeEntry : graph.getRoutes()) {
      Route route = _gtfsDao.getRouteForId(routeEntry.getId());
      AgencyAndId key = getRouteCollectionIdForRoute(route);
      List<RouteEntryImpl> forKey = routesByKey.get(key);
      if (forKey == null) {
        forKey = new ArrayList<RouteEntryImpl>();
        routesByKey.put(key, forKey);
      }
      forKey.add(routeEntry);
    }

    for (Map.Entry<AgencyAndId, List<RouteEntryImpl>> entry : routesByKey.entrySet()) {
      AgencyAndId key = entry.getKey();
      List<RouteEntryImpl> routesForKey = entry.getValue();

      ArrayList<RouteEntry> children = new ArrayList<RouteEntry>();
      children.addAll(routesForKey);
      children.trimToSize();

      key = _uniqueService.unique(key);

      RouteCollectionEntryImpl routeCollectionEntry = new RouteCollectionEntryImpl();
      routeCollectionEntry.setId(key);
      routeCollectionEntry.setChildren(children);
      graph.putRouteCollectionEntry(routeCollectionEntry);

      for (RouteEntryImpl route : routesForKey)
        route.setParent(routeCollectionEntry);
    }
  }

  private AgencyAndId getRouteCollectionIdForRoute(Route route) {
    String id = trim(route.getShortName());
    // If no short name is supplied, we go back to using the original route id
    if (id == null || id.length() == 0)
      id = route.getId().getId();
    id = id.replace('/', '_');
    id = id.replace('\\', '_');
    return new AgencyAndId(route.getId().getAgencyId(), id);
  }

  private String trim(String value) {
    if (value == null)
      return value;
    return value.trim();
  }
}
