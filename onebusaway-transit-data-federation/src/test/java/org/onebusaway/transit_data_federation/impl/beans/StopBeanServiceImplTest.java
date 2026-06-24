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
package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class StopBeanServiceImplTest {

  private StopBeanServiceImpl _service;

  private TransitGraphDao _transitGraphDao;

  private NarrativeService _narrativeService;

  private RouteService _routeService;

  private RouteBeanService _routeBeanService;

  @Before
  public void setup() {
    _service = new StopBeanServiceImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_transitGraphDao);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

    _routeService = Mockito.mock(RouteService.class);
    _service.setRouteService(_routeService);

    _routeBeanService = Mockito.mock(RouteBeanService.class);
    _service.setRouteBeanService(_routeBeanService);
  }

  @Test
  public void testGetStopForId() {

    AgencyAndId stopId = new AgencyAndId("29", "1109");

    StopEntryImpl stopEntry = new StopEntryImpl(stopId, 47.1, -122.1);
    Mockito.when(_transitGraphDao.getStopEntryForId(stopId)).thenReturn(
        stopEntry);

    StopNarrative.Builder builder = StopNarrative.builder();
    builder.setCode("1109-b");
    builder.setDescription("stop description");
    builder.setLocationType(0);
    builder.setName("stop name");
    builder.setUrl("http://some/url");
    builder.setDirection("N");

    StopNarrative stop = builder.create();

    Mockito.when(_narrativeService.getStopForId(stopId)).thenReturn(stop);

    AgencyAndId routeId = new AgencyAndId("1", "route");

    Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
    routeIds.add(routeId);

    Mockito.when(_routeService.getRouteCollectionIdsForStop(stopId)).thenReturn(
        routeIds);

    RouteBean.Builder routeBuilder = RouteBean.builder();
    routeBuilder.setId(AgencyAndIdLibrary.convertToString(routeId));
    RouteBean route = routeBuilder.create();
    Mockito.when(_routeBeanService.getRouteForId(routeId)).thenReturn(route);

    StopBean stopBean = _service.getStopForId(stopId, null);

    assertNotNull(stopBean);
    assertEquals(stopId.toString(), stopBean.getId());
    assertEquals(stop.getName(), stopBean.getName());
    assertEquals(stopEntry.getStopLat(), stopBean.getLat(), 0.0);
    assertEquals(stopEntry.getStopLon(), stopBean.getLon(), 0.0);
    assertEquals(stop.getCode(), stopBean.getCode());
    assertEquals(stop.getLocationType(), stopBean.getLocationType());

    List<RouteBean> routes = stopBean.getRoutes();
    assertEquals(1, routes.size());

    assertSame(route, routes.get(0));
  }

  /**
   * A stop's route-collection index can reference a collection that no longer
   * resolves to a route bean (e.g. a corrupt service-date entry in the bundle).
   * {@link RouteBeanService#getRouteForId} then returns null. We must not let a
   * null {@link RouteBean} leak into the stop's routes list: in production a
   * single such orphan route survives the sort (a lone element is never
   * compared) and later NPEs in bean serialization, blanking the whole API
   * response with a literal "null".
   */
  @Test
  public void testGetStopForId_skipsUnresolvableRoute() {

    AgencyAndId stopId = new AgencyAndId("29", "1109");

    StopEntryImpl stopEntry = new StopEntryImpl(stopId, 47.1, -122.1);
    Mockito.when(_transitGraphDao.getStopEntryForId(stopId)).thenReturn(
        stopEntry);

    StopNarrative.Builder builder = StopNarrative.builder();
    builder.setName("stop name");
    StopNarrative stop = builder.create();
    Mockito.when(_narrativeService.getStopForId(stopId)).thenReturn(stop);

    AgencyAndId orphanRouteId = new AgencyAndId("1", "orphan");

    Set<AgencyAndId> routeIds = new HashSet<AgencyAndId>();
    routeIds.add(orphanRouteId);
    Mockito.when(_routeService.getRouteCollectionIdsForStop(stopId)).thenReturn(
        routeIds);

    // the corrupt collection id resolves to null
    Mockito.when(_routeBeanService.getRouteForId(orphanRouteId)).thenReturn(null);

    StopBean stopBean = _service.getStopForId(stopId, null);

    assertNotNull(stopBean);
    List<RouteBean> routes = stopBean.getRoutes();
    assertTrue("unresolvable route must not leak into routes list", routes.isEmpty());
  }

  /**
   * The static-routes path resolves route beans the same way as the primary
   * routes path, so an unresolvable static route must likewise be dropped
   * rather than leak a null into the stop's static routes list. See issue #461.
   */
  @Test
  public void testGetStopForId_skipsUnresolvableStaticRoute() {

    AgencyAndId stopId = new AgencyAndId("29", "1109");

    StopEntryImpl stopEntry = new StopEntryImpl(stopId, 47.1, -122.1);
    Mockito.when(_transitGraphDao.getStopEntryForId(stopId)).thenReturn(
        stopEntry);

    StopNarrative.Builder builder = StopNarrative.builder();
    builder.setName("stop name");
    Mockito.when(_narrativeService.getStopForId(stopId)).thenReturn(builder.create());

    // no active routes, so only the static-route path populates the bean
    Mockito.when(_routeService.getRouteCollectionIdsForStop(stopId)).thenReturn(
        new HashSet<AgencyAndId>());

    AgencyAndId orphanStaticRouteId = new AgencyAndId("1", "orphan");
    Mockito.when(_narrativeService.getStaticRoutes(stopId)).thenReturn(
        Collections.singletonList(orphanStaticRouteId));
    // the corrupt static route resolves to null
    Mockito.when(_routeBeanService.getRouteForId(orphanStaticRouteId)).thenReturn(null);

    StopBean stopBean = _service.getStopForId(stopId, null);

    assertNotNull(stopBean);
    assertTrue("unresolvable static route must not leak into static routes list",
        stopBean.getStaticRoutes().isEmpty());
  }
}
