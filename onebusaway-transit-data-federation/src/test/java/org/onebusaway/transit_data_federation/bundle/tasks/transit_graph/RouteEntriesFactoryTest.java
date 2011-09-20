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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.UniqueServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;

public class RouteEntriesFactoryTest {

  @Test
  public void testProcessRoutes() {

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);

    Agency agency = new Agency();
    agency.setId("A");

    Route routeA = new Route();
    routeA.setAgency(agency);
    routeA.setId(new AgencyAndId("A", "routeA"));

    Route routeB = new Route();
    routeB.setAgency(agency);
    routeB.setId(new AgencyAndId("A", "routeB"));

    Mockito.when(gtfsDao.getAllRoutes()).thenReturn(
        Arrays.asList(routeA, routeB));

    TransitGraphImpl graph = new TransitGraphImpl();

    RouteEntriesFactory factory = new RouteEntriesFactory();
    factory.setGtfsDao(gtfsDao);
    factory.setUniqueService(new UniqueServiceImpl());
    factory.processRoutes(graph);

    RouteEntryImpl routeEntryA = graph.getRouteForId(routeA.getId());

    RouteEntryImpl routeEntryB = graph.getRouteForId(routeB.getId());

    List<RouteEntry> routes = graph.getAllRoutes();
    assertEquals(2, routes.size());
    assertTrue(routes.contains(routeEntryA));
    assertTrue(routes.contains(routeEntryB));
  }
}
