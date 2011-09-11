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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.UniqueServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.AgencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;

public class RouteCollectionEntriesFactoryTest {

  @Test
  public void testProcessRouteCollections() {

    TransitGraphImpl graph = new TransitGraphImpl();

    AgencyEntryImpl agency = new AgencyEntryImpl();
    agency.setId("A");
    graph.putAgencyEntry(agency);
    graph.refreshAgencyMapping();

    RouteEntryImpl routeA = new RouteEntryImpl();
    routeA.setId(new AgencyAndId("A", "routeA"));
    graph.putRouteEntry(routeA);

    RouteEntryImpl routeB = new RouteEntryImpl();
    routeB.setId(new AgencyAndId("A", "routeB"));
    graph.putRouteEntry(routeB);

    RouteCollectionEntriesFactory factory = new RouteCollectionEntriesFactory();
    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    factory.setGtfsDao(gtfsDao);
    factory.setUniqueService(new UniqueServiceImpl());
    factory.processRouteCollections(graph);

    RouteCollectionEntry routeEntryA = graph.getRouteCollectionForId(routeA.getId());
    assertEquals(routeA.getId(), routeEntryA.getId());
    List<RouteEntry> routes = routeEntryA.getChildren();
    assertEquals(1, routes.size());
    assertTrue(routes.contains(routeA));

    RouteCollectionEntry routeEntryB = graph.getRouteCollectionForId(routeB.getId());
    assertEquals(routeB.getId(), routeEntryB.getId());
    routes = routeEntryB.getChildren();
    assertEquals(1, routes.size());
    assertTrue(routes.contains(routeB));

    List<RouteCollectionEntry> routeCollections = graph.getAllRouteCollections();
    assertEquals(2, routeCollections.size());
    assertTrue(routeCollections.contains(routeEntryA));
    assertTrue(routeCollections.contains(routeEntryB));

    routeCollections = agency.getRouteCollections();
    assertEquals(2, routeCollections.size());
    assertTrue(routeCollections.contains(routeEntryA));
    assertTrue(routeCollections.contains(routeEntryB));
  }

  @Test
  public void testGroupRoutesByShortName() {

    TransitGraphImpl graph = new TransitGraphImpl();

    AgencyEntryImpl agency = new AgencyEntryImpl();
    agency.setId("A");
    graph.putAgencyEntry(agency);
    graph.refreshAgencyMapping();

    RouteEntryImpl routeA = new RouteEntryImpl();
    routeA.setId(new AgencyAndId("A", "routeA"));
    graph.putRouteEntry(routeA);

    RouteEntryImpl routeB = new RouteEntryImpl();
    routeB.setId(new AgencyAndId("A", "routeB"));
    graph.putRouteEntry(routeB);

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);

    Route rA = new Route();
    rA.setId(routeA.getId());
    rA.setShortName("10");
    Mockito.when(gtfsDao.getRouteForId(routeA.getId())).thenReturn(rA);

    Route rB = new Route();
    rB.setId(routeB.getId());
    rB.setShortName("10");
    Mockito.when(gtfsDao.getRouteForId(routeB.getId())).thenReturn(rB);

    RouteCollectionEntriesFactory factory = new RouteCollectionEntriesFactory();
    factory.setGroupRoutesByShortName(true);

    factory.setGtfsDao(gtfsDao);
    factory.setUniqueService(new UniqueServiceImpl());
    factory.processRouteCollections(graph);

    AgencyAndId id = new AgencyAndId("A", "10");
    RouteCollectionEntry routeCollectionEntry = graph.getRouteCollectionForId(id);
    assertEquals(id, routeCollectionEntry.getId());
    List<RouteEntry> routes = routeCollectionEntry.getChildren();
    assertEquals(2, routes.size());
    assertTrue(routes.contains(routeA));
    assertTrue(routes.contains(routeB));

    List<RouteCollectionEntry> routeCollections = graph.getAllRouteCollections();
    assertEquals(1, routeCollections.size());
    assertTrue(routeCollections.contains(routeCollectionEntry));

    routeCollections = agency.getRouteCollections();
    assertEquals(1, routeCollections.size());
    assertTrue(routeCollections.contains(routeCollectionEntry));
  }
}
