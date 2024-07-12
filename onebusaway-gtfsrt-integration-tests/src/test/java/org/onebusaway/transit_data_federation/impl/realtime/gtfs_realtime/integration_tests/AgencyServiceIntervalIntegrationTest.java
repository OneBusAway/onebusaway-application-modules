/**
 * Copyright (C) 2024 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.integration_tests;

import org.junit.Test;
import org.junit.Ignore;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.ServiceIntervalHelper;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Allow for agency specific overrides that change the service window of API results.
 */
public class AgencyServiceIntervalIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {
  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_r_gtfs";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  @Ignore("Broken upstream in CamSys repo; probably since about Nov 16 2023 - https://github.com/camsys/onebusaway-application-modules/commits/unified/?after=87a68db9060d67121fcf912359d18f1e4498bb0d+209")
  public void testServiceWindows() throws Exception {

    // we load real-time to confirm these queries handle both static and real-time appropriately
    loadRealtime("org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_r_gtfs/202401120646-nqrw.pb");

    // choose an API
    // * stops for location
    // Stop R33 services D/N/R/W in totality but mainly serves the R, especially at lunchtime
    Map overrides = new HashMap<String, Integer>();
    overrides.put("MTASBWY", 60); // override in minutes
    ServiceDate baseServiceDate = new ServiceDate(2024, 1, 5);
    long referenceTime= baseServiceDate.getAsDate().getTime() + hourToMillis(12);

    AgencyServiceInterval serviceInterval = new AgencyServiceInterval(referenceTime, overrides);
    StopsBean stops = searchForStopsByLatLon("MTASBWY_R33S", serviceInterval);

    expectRoutesForStops(stops, "R33S", "R");

    // * a/d for location
    StopsWithArrivalsAndDeparturesBean adBean = searchForADStopsByLatLon("MTASBWY_R33S", serviceInterval);

    for (StopBean stop : adBean.getStops()) {
      expectRoutesForStop(stop, "R33S", "R");
    }
    for (StopBean nearbyStop : adBean.getNearbyStops()) {
      expectRoutesForStop(nearbyStop, "R33S", "R");
      expectRoutesForStop(nearbyStop, "R33N", "R");
    }


    // * a/d for stop
    StopWithArrivalsAndDeparturesBean stopBean = searchForAdStopByStop("MTASBWY_R33S", serviceInterval);
    StopBean stop = stopBean.getStop();
    expectRoutesForStop(stop, "R33S", "R");
    for (StopBean nearbyStop : stopBean.getNearbyStops()) {
      expectRoutesForStop(nearbyStop, "R33S", "R");
      expectRoutesForStop(nearbyStop, "R33N", "R");
    }


    // * route-details
    ListBean<RouteGroupingBean> beans = searchForRouteByRouteId("MTASBWY_R", serviceInterval);
    for (RouteGroupingBean routeGroupingBean : beans.getList()) {
      for (StopBean routeStop : routeGroupingBean.getStops()) {
        if (routeStop.getId().contains("R33S")) {
          expectRoutesForStop(routeStop, "R33S", "R");
        } else if (routeStop.getId().contains("R33N")) {
          expectRoutesForStop(routeStop, "R33N", "R");
        }
      }
    }
  }

  private ListBean<RouteGroupingBean> searchForRouteByRouteId(String routeId, AgencyServiceInterval serviceInterval) {
    TransitDataService tds = getBundleLoader().getApplicationContext().getBean(TransitDataService.class);
    return tds.getCanonicalRoute(serviceInterval, AgencyAndIdLibrary.convertFromString(routeId));
  }

  private StopWithArrivalsAndDeparturesBean searchForAdStopByStop(String stopId, AgencyServiceInterval serviceInterval) {
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    TransitDataService tds = getBundleLoader().getApplicationContext().getBean(TransitDataService.class);
    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    return tds.getStopWithArrivalsAndDepartures(stopId, query, serviceInterval);
  }

  private StopsWithArrivalsAndDeparturesBean searchForADStopsByLatLon(String stopId, AgencyServiceInterval serviceInterval) {
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    TransitDataService tds = getBundleLoader().getApplicationContext().getBean(TransitDataService.class);
    List<String> stopIds = new ArrayList<>();
    stopIds.add(stopId);
    StopEntry stopEntryForId = graph.getStopEntryForId(AgencyAndId.convertFromString(stopId));
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(stopEntryForId.getStopLat(), stopEntryForId.getStopLon(), 5);
    ArrivalsAndDeparturesQueryBean adQuery = new ArrivalsAndDeparturesQueryBean();
    adQuery.setIncludeInputIdsInNearby(true);
    adQuery.setMaxCount(100);
    adQuery.setBounds(bounds);
    return tds.getStopsWithArrivalsAndDepartures(stopIds, adQuery, serviceInterval);
  }

  private void expectRoutesForStops(StopsBean stops, String stopId, String routeIdStrs) {
    boolean found = false;
    for (StopBean stop : stops.getStops()) {
       found |= expectRoutesForStop(stop, stopId, routeIdStrs);
    }
    assertTrue(found);
  }

  private boolean expectRoutesForStop(StopBean stop, String stopId, String routeIdStrs) {
    String[] routeIdArray = routeIdStrs.split(",");
    Set<String> expectedRouteIds = new HashSet<>();
    expectedRouteIds.addAll(Arrays.asList(routeIdArray));

    boolean found = false;
    if (stop.getId().contains(stopId)) {
      found = true;
      List<RouteBean> routes = stop.getRoutes();
      // R (D/N don't serve at lunch) (W is rare, should not show up in this query)
      if (routes.size() != routeIdArray.length) {
        System.err.println("not expecting this!");
      }
      assertEquals(routeIdArray.length, routes.size());
      for (RouteBean route : routes) {
        assertTrue(expectedRouteIds.contains(route.getShortName()));
      }
    }
    return found;
  }

  private long hourToMillis(int i) {
    return i * 60 * 60 * 1000;
  }

  private StopsBean searchForStopsByLatLon(String stopId, AgencyServiceInterval serviceInterval) {
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry stopEntryForId = graph.getStopEntryForId(AgencyAndId.convertFromString(stopId));
    SearchQueryBean searchQuery = new SearchQueryBean();
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(stopEntryForId.getStopLat(), stopEntryForId.getStopLon(), 5);
    searchQuery.setBounds(bounds);
    searchQuery.setMaxCount(100);
    searchQuery.setType(SearchQueryBean.EQueryType.BOUNDS);
    searchQuery.setServiceInterval(serviceInterval);

    TransitDataService tds = getBundleLoader().getApplicationContext().getBean(TransitDataService.class);
    assertNotNull(tds);

    StopsBean stops = tds.getStops(searchQuery);
    return stops;
  }
}
