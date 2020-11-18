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
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockTripIndices;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.impl.StopSequenceCollectionServiceImpl;
import org.onebusaway.transit_data_federation.impl.StopSequencesServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteCollectionEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class RouteBeanServiceImplTest {

  private RouteBeanServiceImpl _service;

  private TransitGraphDao _transitGraphDao;

  private AgencyBeanService _agencyBeanService;

  private RouteService _routeService;

  private ShapeBeanService _shapeBeanService;

  private StopSequencesServiceImpl _stopSequencesService;

  private StopBeanService _stopBeanService;

  private StopSequenceCollectionServiceImpl _stopSequenceBlocksService;

  private BlockIndexService _blockIndexService;

  private NarrativeService _narrativeService;

  @Before
  public void setup() {
    _service = new RouteBeanServiceImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_transitGraphDao);

    _agencyBeanService = Mockito.mock(AgencyBeanService.class);
    _service.setAgencyBeanService(_agencyBeanService);

    _routeService = Mockito.mock(RouteService.class);
    _service.setRouteService(_routeService);

    _shapeBeanService = Mockito.mock(ShapeBeanService.class);
    _service.setShapeBeanService(_shapeBeanService);

    _stopBeanService = Mockito.mock(StopBeanService.class);
    _service.setStopBeanService(_stopBeanService);

    _stopSequencesService = new StopSequencesServiceImpl();
    _service.setStopSequencesLibrary(_stopSequencesService);

    _stopSequenceBlocksService = new StopSequenceCollectionServiceImpl();
    _service.setStopSequencesBlocksService(_stopSequenceBlocksService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);
    _stopSequenceBlocksService.setNarrativeService(_narrativeService);

    _blockIndexService = Mockito.mock(BlockIndexService.class);
    _service.setBlockIndexService(_blockIndexService);
  }

  @Test
  public void testGetRouteForId() {

    AgencyAndId routeId = new AgencyAndId("1", "route");

    RouteCollectionNarrative.Builder routeBuilder = RouteCollectionNarrative.builder();
    routeBuilder.setColor("blue");
    routeBuilder.setDescription("route desc");
    routeBuilder.setLongName("route long name");
    routeBuilder.setShortName("route short name");
    routeBuilder.setTextColor("red");
    routeBuilder.setType(3);
    routeBuilder.setUrl("http://wwww.route.com");
    RouteCollectionNarrative route = routeBuilder.create();

    AgencyBean agency = new AgencyBean();
    Mockito.when(_agencyBeanService.getAgencyForId("1")).thenReturn(agency);

    Mockito.when(_narrativeService.getRouteCollectionForId(routeId)).thenReturn(
        route);
    RouteBean bean = _service.getRouteForId(routeId);

    assertEquals(route.getColor(), bean.getColor());
    assertEquals(route.getDescription(), bean.getDescription());
    assertEquals(AgencyAndIdLibrary.convertToString(routeId), bean.getId());
    assertEquals(route.getLongName(), bean.getLongName());
    assertEquals(route.getShortName(), bean.getShortName());
    assertEquals(route.getTextColor(), bean.getTextColor());
    assertEquals(route.getType(), bean.getType());
    assertEquals(route.getUrl(), bean.getUrl());
  }

  @Test
  public void testGetStopsForRoute() {

    AgencyAndId routeId = new AgencyAndId("1", "route");

    RouteEntryImpl route = new RouteEntryImpl();
    route.setId(new AgencyAndId("1", "raw_route"));
    List<RouteEntry> routes = Arrays.asList((RouteEntry) route);

    RouteCollectionEntryImpl routeCollection = new RouteCollectionEntryImpl();
    routeCollection.setId(routeId);
    routeCollection.setChildren(routes);
    route.setParent(routeCollection);

    Mockito.when(_transitGraphDao.getRouteCollectionForId(routeId)).thenReturn(
        routeCollection);

    RouteCollectionNarrative.Builder rcNarrative = RouteCollectionNarrative.builder();
    Mockito.when(_narrativeService.getRouteCollectionForId(routeId)).thenReturn(
        rcNarrative.create());

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.1, -122.1);
    StopEntryImpl stopC = stop("stopC", 47.2, -122.2);

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "sidA");
    TripEntryImpl tripB = trip("tripB", "sidA");

    tripA.setRoute(route);
    tripA.setDirectionId("0");
    tripB.setRoute(route);
    tripB.setDirectionId("1");

    route.setTrips(Arrays.asList((TripEntry) tripA, tripB));

    TripNarrative.Builder tnA = TripNarrative.builder();
    tnA.setTripHeadsign("Destination A");
    Mockito.when(_narrativeService.getTripForId(tripA.getId())).thenReturn(
        tnA.create());

    TripNarrative.Builder tnB = TripNarrative.builder();
    tnB.setTripHeadsign("Destination B");
    Mockito.when(_narrativeService.getTripForId(tripB.getId())).thenReturn(
        tnB.create());

    stopTime(0, stopA, tripA, time(9, 00), time(9, 00), 0);
    stopTime(1, stopB, tripA, time(9, 30), time(9, 30), 100);
    stopTime(2, stopC, tripA, time(10, 00), time(10, 00), 200);
    stopTime(3, stopC, tripB, time(11, 30), time(11, 30), 0);
    stopTime(4, stopA, tripB, time(12, 30), time(12, 30), 200);

    linkBlockTrips(blockA, tripA, tripB);

    List<BlockTripIndex> blockIndices = blockTripIndices(blockA);
    Mockito.when(
        _blockIndexService.getBlockTripIndicesForRouteCollectionId(routeId)).thenReturn(
        blockIndices);

    StopBean stopBeanA = getStopBean(stopA);
    StopBean stopBeanB = getStopBean(stopB);
    StopBean stopBeanC = getStopBean(stopC);

    List<AgencyAndId> stopIds = Arrays.asList(stopA.getId(), stopB.getId(),
        stopC.getId());
    Mockito.when(_routeService.getStopsForRouteCollection(routeId)).thenReturn(
        stopIds);

    Mockito.when(_stopBeanService.getStopForId(stopA.getId(), null)).thenReturn(
        stopBeanA);
    Mockito.when(_stopBeanService.getStopForId(stopB.getId(), null)).thenReturn(
        stopBeanB);
    Mockito.when(_stopBeanService.getStopForId(stopC.getId(), null)).thenReturn(
        stopBeanC);

    AgencyAndId shapeId = new AgencyAndId("1", "shapeId");

    Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();
    shapeIds.add(shapeId);
    tripA.setShapeId(shapeId);

    EncodedPolylineBean polyline = new EncodedPolylineBean();
    Mockito.when(_shapeBeanService.getMergedPolylinesForShapeIds(shapeIds)).thenReturn(
        Arrays.asList(polyline));

    // Setup complete

    StopsForRouteBean stopsForRoute = _service.getStopsForRoute(routeId);

    List<StopBean> stops = stopsForRoute.getStops();
    assertEquals(3, stops.size());
    assertSame(stopBeanA, stops.get(0));
    assertSame(stopBeanB, stops.get(1));
    assertSame(stopBeanC, stops.get(2));

    List<EncodedPolylineBean> polylines = stopsForRoute.getPolylines();
    assertEquals(1, polylines.size());
    assertSame(polyline, polylines.get(0));

    List<StopGroupingBean> groupings = stopsForRoute.getStopGroupings();
    assertEquals(1, groupings.size());
    StopGroupingBean grouping = groupings.get(0);
    assertEquals("direction", grouping.getType());

    List<StopGroupBean> groups = grouping.getStopGroups();
    assertEquals(2, groups.size());

    StopGroupBean groupA = groups.get(0);
    StopGroupBean groupB = groups.get(1);

    NameBean nameA = groupA.getName();
    assertEquals("destination", nameA.getType());
    assertEquals("Destination A", nameA.getName());

    List<String> stopIdsA = groupA.getStopIds();
    assertEquals(3, stopIdsA.size());
    assertEquals(ids(stopA.getId(), stopB.getId(), stopC.getId()), stopIdsA);

    NameBean nameB = groupB.getName();
    assertEquals("destination", nameB.getType());
    assertEquals("Destination B", nameB.getName());

    List<String> stopIdsB = groupB.getStopIds();
    assertEquals(2, stopIdsB.size());
    assertEquals(ids(stopC.getId(), stopA.getId()), stopIdsB);

  }

  private StopBean getStopBean(StopEntryImpl stopEntry) {
    StopBean stop = new StopBean();
    stop.setId(AgencyAndIdLibrary.convertToString(stopEntry.getId()));
    return stop;
  }

  private List<String> ids(AgencyAndId... ids) {
    List<String> stringIds = new ArrayList<String>();
    for (AgencyAndId id : ids)
      stringIds.add(AgencyAndIdLibrary.convertToString(id));
    return stringIds;
  }
}
