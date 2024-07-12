/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.RouteStop;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.transit_graph.CanonicalRoutesEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteShapeDirectionKey;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteStopCollectionEntry;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.onebusaway.transit_data_federation.impl.CanonicalRoutesServiceImpl.CANONICAL_TYPE;
import static org.onebusaway.transit_data_federation.services.CanonicalRoutesService.DIRECTION_TYPE;
import static org.onebusaway.transit_data_federation.services.CanonicalRoutesService.HEURISTIC_TYPE;

/**
 * verify ideal route behaviour.
 */
public class CanonicalRoutesServiceImplTest {

  private static final AgencyAndId NO_SUCH_ROUTE_ID = new AgencyAndId("1","missing");
  private static final AgencyAndId CANONICAL_ONLY_ROUTE_ID = new AgencyAndId("1","canon");

  private static final AgencyAndId MERGED_ROUTE_ID = new AgencyAndId("1","merged");

  private static final AgencyAndId BUNDLE_ONLY_ROUTE_ID = new AgencyAndId("1","bundle");

  private static final AgencyServiceInterval serviceInterval = null;

  private static final String SHAPE_1 = "abcde";
  private static final String SHAPE_2 = "fghijk";
  private static final String SHAPE_3 = "lmnop";
  private CanonicalRoutesServiceImpl service;
  @Before
  public void setUp() throws Exception {
    TransitDataService tds = Mockito.mock(TransitDataService.class);
    service = new CanonicalRoutesServiceImpl();
    service.setTransitDataService(tds);

    Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops = new HashMap<>();
    Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape = new HashMap<>();
    Map<AgencyAndId, Map<String, String>> routeIdToDirectionAndShape = new HashMap<>();

    CanonicalRoutesEntryImpl entry = new CanonicalRoutesEntryImpl();
    addRouteStops(routeIdToRouteStops, CANONICAL_ONLY_ROUTE_ID);
    addRouteStops(routeIdToRouteStops, MERGED_ROUTE_ID);
    addRouteShapes(routeShapeKeyToEncodedShape, routeIdToDirectionAndShape,  CANONICAL_ONLY_ROUTE_ID, "0", SHAPE_1);
    addRouteShapes(routeShapeKeyToEncodedShape, routeIdToDirectionAndShape, MERGED_ROUTE_ID, "0", SHAPE_2);
    entry.setRouteShapeKeyToEncodedShape(routeShapeKeyToEncodedShape);
    entry.setRouteIdToDirectionAndShape(routeIdToDirectionAndShape);
    entry.setRouteIdToRouteStops(routeIdToRouteStops);

    service.setData(entry);
  }

  private void addRouteShapes(Map<RouteShapeDirectionKey, String> routeShapeKeyToEncodedShape,
                              Map<AgencyAndId, Map<String, String>> routeIdToDirectionAndShape,
                              AgencyAndId routeId, String directionId, String shape) {
    RouteShapeDirectionKey key = new RouteShapeDirectionKey(routeId, directionId, CANONICAL_TYPE);
    routeShapeKeyToEncodedShape.put(key, shape);
    if (!routeIdToDirectionAndShape.containsKey(routeId)) {
      routeIdToDirectionAndShape.put(routeId, new HashMap<>());
    }
    routeIdToDirectionAndShape.get(routeId).put(directionId, shape);

  }


  @Test
  public void getCanonicalStopNoResults() {
    ListBean<RouteGroupingBean> canonicalOrMergedRoutes = service.getCanonicalOrMergedRoute(serviceInterval, NO_SUCH_ROUTE_ID);
    assertNotNull(canonicalOrMergedRoutes);
    assertTrue(canonicalOrMergedRoutes.getList().isEmpty());

  }

  @Test
  public void getCanonicalStopIdsAndShapes() {
    ListBean<RouteGroupingBean> canonicalOrMergedRoutes = service.getCanonicalOrMergedRoute(serviceInterval, CANONICAL_ONLY_ROUTE_ID);
    assertNotNull(canonicalOrMergedRoutes);
    assertFalse(canonicalOrMergedRoutes.getList().isEmpty());
    // now verify the fields are as we expect!
    assertEquals(1, canonicalOrMergedRoutes.getList().size());
    // our overall result bean
    RouteGroupingBean bean = canonicalOrMergedRoutes.getList().get(0);
    assertEquals(CANONICAL_ONLY_ROUTE_ID, bean.getRouteId());
    assertNotNull(bean.getStopGroupings());
    assertEquals(1, bean.getStopGroupings().size());

    // represents a type + route list, likely with multiple directions
    StopGroupingBean stopGroupingBean = bean.getStopGroupings().get(0);
    assertEquals(CANONICAL_TYPE, stopGroupingBean.getType());
    // in this example we only have one as the TDS didn't return "direction"
    assertEquals(1, stopGroupingBean.getStopGroups().size());

    // the details of the type + route
    StopGroupBean stopGroupBean = stopGroupingBean.getStopGroups().get(0);
    assertEquals(3, stopGroupBean.getStopIds().size());
    assertEquals("1_stopA", stopGroupBean.getStopIds().get(0));
    assertEquals("1_stopB", stopGroupBean.getStopIds().get(1));
    assertEquals("1_stopC", stopGroupBean.getStopIds().get(2));
    // implied GTFS direction id
    assertEquals("0", stopGroupBean.getId());

    assertNotNull(stopGroupBean.getName());
    assertEquals("canon name", stopGroupBean.getName().getName());

    assertEquals(1, bean.getStopGroupings().get(0).getStopGroups().size());
    assertEquals(1, bean.getStopGroupings().get(0).getStopGroups().get(0).getPolylines().size());

    assertEquals(SHAPE_1, bean.getStopGroupings().get(0).getStopGroups().get(0).getPolylines().get(0).getPoints());

  }

  @Test
  public void getMergedStopIds() {
    Mockito.when(service._transitDataService.getStopsForRoute
            (Mockito.eq(AgencyAndIdLibrary.convertToString(MERGED_ROUTE_ID))))
            .thenReturn(createGtfsResults());

    ListBean<RouteGroupingBean> canonicalOrMergedRoutes = service.getCanonicalOrMergedRoute(serviceInterval, MERGED_ROUTE_ID);
    assertNotNull(canonicalOrMergedRoutes);
    assertFalse(canonicalOrMergedRoutes.getList().isEmpty());

    // only 1 at this level
    assertEquals(1, canonicalOrMergedRoutes.getList().size());
    RouteGroupingBean bean = canonicalOrMergedRoutes.getList().get(0);

    assertEquals("1_merged", bean.getRouteId().toString());
    // expecting one direction and one canonical
    assertEquals(2, bean.getStopGroupings().size());

    boolean foundCanonical0 = false;
    boolean foundDirection0 = false;
    boolean foundDirection1 = false;

    for (StopGroupingBean stopGrouping : bean.getStopGroupings()) {
      if ("direction".equals(stopGrouping.getType())) {
        assertEquals(2, stopGrouping.getStopGroups().size());
        for (StopGroupBean sg : stopGrouping.getStopGroups()) {
          if ("0".equals(sg.getId())) {
            foundDirection0 = true;
          } else if ("1".equals(sg.getId())) {
            foundDirection1 = true;
          } else {
            fail("unexpected direction " + sg.getId());
          }
        }

      } else if ("canonical".equals(stopGrouping.getType())) {
        assertEquals(1, stopGrouping.getStopGroups().size());
        StopGroupBean sg = stopGrouping.getStopGroups().get(0);
        assertEquals("0", sg.getId());
        foundCanonical0 = true;

      } else {
        fail("unexpected type " + stopGrouping.getType());
      }
    }
    assertTrue(foundCanonical0);
    assertTrue(foundDirection0);
    assertTrue(foundDirection1);

    assertEquals(1, bean.getStopGroupings().get(0).getStopGroups().size());
    assertEquals(1, bean.getStopGroupings().get(0).getStopGroups().get(0).getPolylines().size());

    assertEquals(SHAPE_2, bean.getStopGroupings().get(0).getStopGroups().get(0).getPolylines().get(0).getPoints());

  }

  @Test
  public void testBundleOnlyStopIds() {
    Mockito.when(service._transitDataService.getStopsForRoute
                    (Mockito.eq(AgencyAndIdLibrary.convertToString(BUNDLE_ONLY_ROUTE_ID))))
            .thenReturn(createGtfsResults());

    Mockito.when(service._transitDataService.getStopsForRoute
                    (Mockito.eq(AgencyAndIdLibrary.convertToString(BUNDLE_ONLY_ROUTE_ID))))
            .thenReturn(createGtfsResults());

    ListBean<RouteGroupingBean> canonicalOrMergedRoutes = service.getCanonicalOrMergedRoute(serviceInterval, BUNDLE_ONLY_ROUTE_ID);
    assertNotNull(canonicalOrMergedRoutes);
    assertFalse(canonicalOrMergedRoutes.getList().isEmpty());

    // canonical should be generated on the fly if not present
    assertEquals(1, canonicalOrMergedRoutes.getList().size());
    RouteGroupingBean bean = canonicalOrMergedRoutes.getList().get(0);

    assertEquals("1_bundle", bean.getRouteId().toString());
    // expecting one direction and one canonical
    // canonical was generated via heuristic
    assertEquals(2, bean.getStopGroupings().size());

    assertEquals(HEURISTIC_TYPE, bean.getStopGroupings().get(0).getType());
    assertEquals(DIRECTION_TYPE, bean.getStopGroupings().get(1).getType());
  }

  private StopsForRouteBean createGtfsResults() {
    StopsForRouteBean bean = new StopsForRouteBean();
    bean.setStopGroupings(new ArrayList<>());
    bean.getStopGroupings().add(createStopGrouping("gtfs route1"));
    bean.setPolylines(createPolyLines(SHAPE_3));
    return bean;
  }

  private List<EncodedPolylineBean> createPolyLines(String shape) {
    List<EncodedPolylineBean> beans = new ArrayList<>();
    EncodedPolylineBean bean = new EncodedPolylineBean();
    bean.setPoints(shape);
    bean.setLength(shape.length());
    bean.setLevels("1");
    beans.add(bean);
    return beans;
  }

  private StopGroupingBean createStopGrouping(String name) {
    StopGroupingBean bean = new StopGroupingBean();
    bean.setStopGroups(new ArrayList<>());
    bean.setType("direction");
    bean.setOrdered(true);
    bean.getStopGroups().add(createStopGroup(name, "0"));
    bean.getStopGroups().add(createStopGroup(name, "1"));
    return bean;
  }

  private StopGroupBean createStopGroup(String name, String directionId) {
    StopGroupBean bean = new StopGroupBean();
    bean.setId(directionId);
    bean.setName(new NameBean("name", name));
    bean.setStopIds(new ArrayList<>());
    bean.getStopIds().add("1_stopA");
    bean.getStopIds().add("1_stopB");
    bean.getStopIds().add("1_stopC");
    return bean;
  }

  private void addRouteStops(Map<AgencyAndId, List<RouteStopCollectionEntry>> routeIdToRouteStops, AgencyAndId routeId) {
    assertNotNull(routeId);

    if (!routeIdToRouteStops.containsKey(routeId)) {
      routeIdToRouteStops.put(routeId, new ArrayList<>());
    }

    RouteStopCollectionEntry rsc = new RouteStopCollectionEntry();
    rsc.setRouteId(routeId);
    rsc.setId("0");
    rsc.setName(routeId.getId() + " name");

    RouteStop rs = new RouteStop();
    rs.setStopId(new AgencyAndId("1", "stopA").toString());
    rs.setRouteId(routeId.toString());
    rs.setStopSequence(1);
    rsc.add(rs);
    rs = new RouteStop();
    rs.setStopId(new AgencyAndId("1", "stopB").toString());
    rs.setRouteId(routeId.toString());
    rs.setStopSequence(2);
    rsc.add(rs);
    rs = new RouteStop();
    rs.setStopId(new AgencyAndId("1", "stopC").toString());
    rs.setRouteId(routeId.toString());
    rs.setStopSequence(3);
    rsc.add(rs);
    routeIdToRouteStops.get(routeId).add(rsc);
  }

}