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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.ShapePointHelper;
import org.onebusaway.transit_data_federation.bundle.tasks.UniqueServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class TripEntriesFactoryTest {

  @Test
  public void test() {

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);

    Agency agency = new Agency();
    agency.setId("1");
    agency.setTimezone("America/Los_Angeles");
    // gtfsDao.saveEntity(agency);

    Route route = new Route();
    route.setId(new AgencyAndId("1", "routeA"));
    route.setAgency(agency);
    Mockito.when(gtfsDao.getAllRoutes()).thenReturn(Arrays.asList(route));

    AgencyAndId shapeId = new AgencyAndId("1", "shapeId");

    Trip trip = new Trip();
    trip.setId(new AgencyAndId("1", "tripA"));
    trip.setRoute(route);
    trip.setServiceId(new AgencyAndId("1", "serviceId"));
    trip.setShapeId(shapeId);
    Mockito.when(gtfsDao.getTripsForRoute(route)).thenReturn(
        Arrays.asList(trip));

    Stop stopA = new Stop();
    stopA.setId(aid("stopA"));

    StopTime stA = new StopTime();
    stA.setId(100);
    stA.setArrivalTime(time(9, 00));
    stA.setDepartureTime(time(9, 05));
    stA.setStopSequence(100);
    stA.setStop(stopA);
    stA.setTrip(trip);

    Stop stopB = new Stop();
    stopB.setId(aid("stopB"));

    StopTime stB = new StopTime();
    stB.setId(101);
    stB.setArrivalTime(time(10, 00));
    stB.setDepartureTime(time(10, 05));
    stB.setStopSequence(102);
    stB.setStop(stopB);
    stB.setTrip(trip);

    Mockito.when(gtfsDao.getStopTimesForTrip(trip)).thenReturn(
        Arrays.asList(stA, stB));

    TransitGraphImpl graph = new TransitGraphImpl();

    graph.putStopEntry(stop("stopA", 47.672207391799056, -122.387855896286));

    graph.putStopEntry(stop("stopB", 47.66852277218285, -122.3853882639923));

    RouteEntryImpl routeEntry = route("routeA");
    graph.putRouteEntry(routeEntry);

    graph.initialize();

    ShapePointsFactory shapePointsFactory = new ShapePointsFactory();
    shapePointsFactory.addPoint(47.673840100841396, -122.38756621771239);
    shapePointsFactory.addPoint(47.668667271970484, -122.38756621771239);
    shapePointsFactory.addPoint(47.66868172192725, -122.3661729186096);
    ShapePoints shapePoints = shapePointsFactory.create();

    ShapePointHelper shapePointHelper = Mockito.mock(ShapePointHelper.class);
    Mockito.when(shapePointHelper.getShapePointsForShapeId(shapeId)).thenReturn(
        shapePoints);

    TripEntriesFactory factory = new TripEntriesFactory();
    factory.setGtfsDao(gtfsDao);
    factory.setShapePointHelper(shapePointHelper);
    factory.setUniqueService(new UniqueServiceImpl());

    StopTimeEntriesFactory stopTimeEntriesFactory = new StopTimeEntriesFactory();
    stopTimeEntriesFactory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    factory.setStopTimeEntriesFactory(stopTimeEntriesFactory);

    factory.processTrips(graph);

    TripEntryImpl entry = graph.getTripEntryForId(trip.getId());
    assertEquals(trip.getId(), entry.getId());
    assertEquals(route.getId(), entry.getRoute().getId());
    assertEquals(lsid("serviceId"), entry.getServiceId());
    assertEquals(trip.getShapeId(), entry.getShapeId());
    assertEquals(2177.1, entry.getTotalTripDistance(), 0.1);

    List<StopTimeEntry> stopTimes = entry.getStopTimes();
    assertEquals(2, stopTimes.size());

    for (StopTimeEntry stopTime : stopTimes) {
      assertSame(entry, stopTime.getTrip());
    }
  }
}
