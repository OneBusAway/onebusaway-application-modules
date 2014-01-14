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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.StopTimeEntriesFactory;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;

public class StopTimeEntriesFactoryTest {

  @Test
  public void testDuplicateRemoval() {

    TransitGraphImpl graph = new TransitGraphImpl();

    Stop stopA = new Stop();
    stopA.setId(aid("stopA"));
    graph.putStopEntry(stop("stopA", 47.672207391799056, -122.387855896286));

    Stop stopB = new Stop();
    stopB.setId(aid("stopB"));
    graph.putStopEntry(stop("stopB", 47.66852277218285, -122.3853882639923));

    Stop stopC = new Stop();
    stopC.setId(aid("stopC"));
    graph.putStopEntry(stop("stopC", 47.66847942216854, -122.37545336180114));

    graph.refreshStopMapping();

    Agency agency = new Agency();
    agency.setId("1");
    agency.setTimezone("America/Los_Angeles");

    Route route = new Route();
    route.setAgency(agency);

    Trip trip = new Trip();
    trip.setRoute(route);
    trip.setServiceId(aid("serviceId"));

    TripEntryImpl tripEntry = trip("trip");

    StopTime stA = new StopTime();
    stA.setId(100);
    stA.setArrivalTime(time(9, 00));
    stA.setDepartureTime(time(9, 05));
    stA.setStopSequence(100);
    stA.setStop(stopA);
    stA.setTrip(trip);

    StopTime stB = new StopTime();
    stB.setId(101);
    stB.setArrivalTime(time(9, 00));
    stB.setDepartureTime(time(9, 05));
    stB.setStopSequence(101);
    stB.setStop(stopB);
    stB.setTrip(trip);

    StopTime stC = new StopTime();
    stC.setId(102);
    stC.setArrivalTime(time(10, 00));
    stC.setDepartureTime(time(10, 05));
    stC.setStopSequence(102);
    stC.setStop(stopC);
    stC.setTrip(trip);

    StopTimeEntriesFactory factory = new StopTimeEntriesFactory();
    factory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    List<StopTime> stopTimes = Arrays.asList(stA, stB, stC);

    ShapePointsFactory shapePointsFactory = new ShapePointsFactory();
    shapePointsFactory.addPoint(47.673840100841396, -122.38756621771239);
    shapePointsFactory.addPoint(47.668667271970484, -122.38756621771239);
    shapePointsFactory.addPoint(47.66868172192725, -122.3661729186096);
    ShapePoints shapePoints = shapePointsFactory.create();

    List<StopTimeEntryImpl> entries = factory.processStopTimes(graph,
        stopTimes, tripEntry, shapePoints);

    assertEquals(stopTimes.size(), entries.size());

    StopTimeEntryImpl entry = entries.get(0);
    assertEquals(0, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 00), entry.getArrivalTime());
    assertEquals(time(9, 05), entry.getDepartureTime());
    assertEquals(0, entry.getSequence());
    assertEquals(181.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(5 * 60, entry.getSlackTime());

    entry = entries.get(1);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 28, 31), entry.getArrivalTime());
    assertEquals(time(9, 28, 31), entry.getDepartureTime());
    assertEquals(1, entry.getSequence());
    assertEquals(738.7, entry.getShapeDistTraveled(), 0.1);
    assertEquals(0, entry.getSlackTime());

    entry = entries.get(2);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(10, 00), entry.getArrivalTime());
    assertEquals(time(10, 05), entry.getDepartureTime());
    assertEquals(2, entry.getSequence());
    assertEquals(1484.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(5 * 60, entry.getSlackTime());
  }
  
  @Test
  public void testThreeInARowDuplicateRemoval() {

    TransitGraphImpl graph = new TransitGraphImpl();

    Stop stopA = new Stop();
    stopA.setId(aid("stopA"));
    graph.putStopEntry(stop("stopA", 47.672207391799056, -122.387855896286));

    Stop stopB = new Stop();
    stopB.setId(aid("stopB"));
    graph.putStopEntry(stop("stopB", 47.66852277218285, -122.3853882639923));

    Stop stopC = new Stop();
    stopC.setId(aid("stopC"));
    graph.putStopEntry(stop("stopC", 47.66847942216854, -122.37545336180114));

    Stop stopD = new Stop();
    stopD.setId(aid("stopD"));
    graph.putStopEntry(stop("stopD", 47.66947942216854, -122.37545336180114));

    graph.refreshStopMapping();

    Agency agency = new Agency();
    agency.setId("1");
    agency.setTimezone("America/Los_Angeles");

    Route route = new Route();
    route.setAgency(agency);

    Trip trip = new Trip();
    trip.setRoute(route);
    trip.setServiceId(aid("serviceId"));

    TripEntryImpl tripEntry = trip("trip");

    StopTime stA = new StopTime();
    stA.setId(100);
    stA.setArrivalTime(time(9, 00));
    stA.setDepartureTime(time(9, 05));
    stA.setStopSequence(100);
    stA.setStop(stopA);
    stA.setTrip(trip);

    StopTime stB = new StopTime();
    stB.setId(101);
    stB.setArrivalTime(time(9, 00));
    stB.setDepartureTime(time(9, 05));
    stB.setStopSequence(101);
    stB.setStop(stopB);
    stB.setTrip(trip);

    StopTime stC = new StopTime();
    stC.setId(102);
    stC.setArrivalTime(time(9, 00));
    stC.setDepartureTime(time(9, 05));
    stC.setStopSequence(102);
    stC.setStop(stopC);
    stC.setTrip(trip);

    StopTime stD = new StopTime();
    stD.setId(103);
    stD.setArrivalTime(time(11, 00));
    stD.setDepartureTime(time(11, 05));
    stD.setStopSequence(103);
    stD.setStop(stopD);
    stD.setTrip(trip);

    StopTimeEntriesFactory factory = new StopTimeEntriesFactory();
    factory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    List<StopTime> stopTimes = Arrays.asList(stA, stB, stC, stD);

    ShapePointsFactory shapePointsFactory = new ShapePointsFactory();
    shapePointsFactory.addPoint(47.673840100841396, -122.38756621771239);
    shapePointsFactory.addPoint(47.668667271970484, -122.38756621771239);
    shapePointsFactory.addPoint(47.66868172192725, -122.3661729186096);
    shapePointsFactory.addPoint(47.66947942216854, -122.37545336180114);
    ShapePoints shapePoints = shapePointsFactory.create();

    List<StopTimeEntryImpl> entries = factory.processStopTimes(graph,
        stopTimes, tripEntry, shapePoints);

    assertEquals(stopTimes.size(), entries.size());

    StopTimeEntryImpl entry = entries.get(0);
    assertEquals(0, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 00), entry.getArrivalTime());
    assertEquals(time(9, 05), entry.getDepartureTime());
    assertEquals(0, entry.getSequence());
    assertEquals(181.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(5 * 60, entry.getSlackTime());

    entry = entries.get(1);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 28, 45), entry.getArrivalTime());
    assertEquals(time(9, 28, 45), entry.getDepartureTime());
    assertEquals(1, entry.getSequence());
    assertEquals(738.7, entry.getShapeDistTraveled(), 0.1);
    assertEquals(0, entry.getSlackTime());

    entry = entries.get(2);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(10, 00, 34), entry.getArrivalTime());
    assertEquals(time(10, 00, 34), entry.getDepartureTime());
    assertEquals(2, entry.getSequence());
    assertEquals(1484.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(0, entry.getSlackTime());

    entry = entries.get(3);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(11, 00), entry.getArrivalTime());
    assertEquals(time(11, 05), entry.getDepartureTime());
    assertEquals(3, entry.getSequence());
    assertEquals(2877.69, entry.getShapeDistTraveled(), 0.1);
    assertEquals(60 * 5, entry.getSlackTime());

  }
  
  @Test
  public void test() {

    TransitGraphImpl graph = new TransitGraphImpl();

    Stop stopA = new Stop();
    stopA.setId(aid("stopA"));
    graph.putStopEntry(stop("stopA", 47.672207391799056, -122.387855896286));

    Stop stopB = new Stop();
    stopB.setId(aid("stopB"));
    graph.putStopEntry(stop("stopB", 47.66852277218285, -122.3853882639923));

    Stop stopC = new Stop();
    stopC.setId(aid("stopC"));
    graph.putStopEntry(stop("stopC", 47.66847942216854, -122.37545336180114));

    graph.refreshStopMapping();

    Agency agency = new Agency();
    agency.setId("1");
    agency.setTimezone("America/Los_Angeles");

    Route route = new Route();
    route.setAgency(agency);

    Trip trip = new Trip();
    trip.setRoute(route);
    trip.setServiceId(aid("serviceId"));

    TripEntryImpl tripEntry = trip("trip");

    StopTime stA = new StopTime();
    stA.setId(100);
    stA.setArrivalTime(time(9, 00));
    stA.setDepartureTime(time(9, 05));
    stA.setStopSequence(100);
    stA.setStop(stopA);
    stA.setTrip(trip);

    StopTime stB = new StopTime();
    stB.setId(101);
    stB.setStopSequence(101);
    stB.setStop(stopB);
    stB.setTrip(trip);

    StopTime stC = new StopTime();
    stC.setId(102);
    stC.setArrivalTime(time(10, 00));
    stC.setDepartureTime(time(10, 05));
    stC.setStopSequence(102);
    stC.setStop(stopC);
    stC.setTrip(trip);

    StopTimeEntriesFactory factory = new StopTimeEntriesFactory();
    factory.setDistanceAlongShapeLibrary(new DistanceAlongShapeLibrary());

    List<StopTime> stopTimes = Arrays.asList(stA, stB, stC);

    ShapePointsFactory shapePointsFactory = new ShapePointsFactory();
    shapePointsFactory.addPoint(47.673840100841396, -122.38756621771239);
    shapePointsFactory.addPoint(47.668667271970484, -122.38756621771239);
    shapePointsFactory.addPoint(47.66868172192725, -122.3661729186096);
    ShapePoints shapePoints = shapePointsFactory.create();

    List<StopTimeEntryImpl> entries = factory.processStopTimes(graph,
        stopTimes, tripEntry, shapePoints);

    assertEquals(stopTimes.size(), entries.size());

    StopTimeEntryImpl entry = entries.get(0);
    assertEquals(0, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 00), entry.getArrivalTime());
    assertEquals(time(9, 05), entry.getDepartureTime());
    assertEquals(0, entry.getSequence());
    assertEquals(181.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(5 * 60, entry.getSlackTime());

    entry = entries.get(1);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(9, 28, 31), entry.getArrivalTime());
    assertEquals(time(9, 28, 31), entry.getDepartureTime());
    assertEquals(1, entry.getSequence());
    assertEquals(738.7, entry.getShapeDistTraveled(), 0.1);
    assertEquals(0, entry.getSlackTime());

    entry = entries.get(2);
    assertEquals(5 * 60, entry.getAccumulatedSlackTime());
    assertEquals(time(10, 00), entry.getArrivalTime());
    assertEquals(time(10, 05), entry.getDepartureTime());
    assertEquals(2, entry.getSequence());
    assertEquals(1484.5, entry.getShapeDistTraveled(), 0.1);
    assertEquals(5 * 60, entry.getSlackTime());
  }
}
