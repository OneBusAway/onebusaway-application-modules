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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

public class GenerateRouteCollectionsTaskTest {

  @Test
  public void test() {

    GenerateRouteCollectionsTask task = new GenerateRouteCollectionsTask();

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    task.setGtfsDao(gtfsDao);

    TransitDataFederationMutableDao transitDao = Mockito.mock(TransitDataFederationMutableDao.class);
    task.setTransitDataFederationMutableDao(transitDao);

    Route routeA = route("a", "A", "Route A");
    Route routeB = route("b1", "B", "Route B Local");
    Route routeC = route("b2", "B", "Route B Express");

    List<Route> routes = Arrays.asList(routeA, routeB, routeC);
    Mockito.when(gtfsDao.getAllRoutes()).thenReturn(routes);

    Mockito.when(gtfsDao.getTripsForRoute(routeA)).thenReturn(trips(routeA, 5));
    Mockito.when(gtfsDao.getTripsForRoute(routeB)).thenReturn(trips(routeB, 10));
    Mockito.when(gtfsDao.getTripsForRoute(routeC)).thenReturn(trips(routeC, 6));

    RefreshService refreshService = Mockito.mock(RefreshService.class);
    task.setRefreshService(refreshService);

    task.run();

    ArgumentCaptor<RouteCollection> captor = ArgumentCaptor.forClass(RouteCollection.class);
    Mockito.verify(transitDao, Mockito.times(2)).save(captor.capture());

    List<RouteCollection> values = captor.getAllValues();
    assertEquals(2, values.size());

    RouteCollection rcA = values.get(0);
    RouteCollection rcB = values.get(1);

    if (rcA.getId().equals(new AgencyAndId("agency", "B"))) {
      RouteCollection temp = rcA;
      rcA = rcB;
      rcB = temp;
    }

    assertEquals(new AgencyAndId("agency", "A"), rcA.getId());
    assertEquals("A", rcA.getShortName());
    assertEquals("Route A", rcA.getLongName());

    assertEquals(new AgencyAndId("agency", "B"), rcB.getId());
    assertEquals("B", rcB.getShortName());
    assertEquals("Route B Local", rcB.getLongName());

    Mockito.verify(refreshService).refresh(
        RefreshableResources.ROUTE_COLLECTIONS_DATA);
  }

  private Route route(String id, String shortName, String longName) {
    Route route = new Route();
    route.setId(new AgencyAndId("agency", id));
    route.setShortName(shortName);
    route.setLongName(longName);
    return route;
  }

  private List<Trip> trips(Route route, int count) {

    AgencyAndId routeId = route.getId();
    List<Trip> trips = new ArrayList<Trip>();

    for (int i = 0; i < count; i++) {
      Trip trip = new Trip();
      trip.setId(new AgencyAndId(routeId.getAgencyId(), route.getId() + "-" + i));
      trip.setRoute(route);
      trips.add(trip);
    }

    return trips;
  }
}
