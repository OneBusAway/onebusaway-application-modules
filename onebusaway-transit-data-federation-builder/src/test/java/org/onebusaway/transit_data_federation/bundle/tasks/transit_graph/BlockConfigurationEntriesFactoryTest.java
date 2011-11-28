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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addServiceDates;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.bundle.tasks.ShapePointHelper;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class BlockConfigurationEntriesFactoryTest {

  private CalendarServiceImpl _calendarService;

  private BlockConfigurationEntriesFactory _factory;

  @Before
  public void before() {

    /****
     * Calendar Service
     ****/

    _calendarService = new CalendarServiceImpl();
    CalendarServiceData data = new CalendarServiceData();
    _calendarService.setData(data);

    addServiceDates(data, "sA", new ServiceDate(2010, 9, 10), new ServiceDate(
        2010, 9, 11));
    addServiceDates(data, "sB", new ServiceDate(2010, 9, 11), new ServiceDate(
        2010, 9, 12));

    /****
     * Service Id Cache
     ****/

    ServiceIdOverlapCache serviceIdOverlapCache = new ServiceIdOverlapCache();
    serviceIdOverlapCache.setCalendarService(_calendarService);

    /****
     * ShapePointsService
     ****/

    ShapePointHelper shapePointService = Mockito.mock(ShapePointHelper.class);

    ShapePointsFactory shapePointsFactory = new ShapePointsFactory();
    shapePointsFactory.addPoint(0, 0);

    Mockito.when(
        shapePointService.getShapePointsForShapeId((AgencyAndId) Mockito.any())).thenReturn(
        shapePointsFactory.create());

    /****
     * Factory
     ****/

    _factory = new BlockConfigurationEntriesFactory();
    _factory.setServiceIdOverlapCache(serviceIdOverlapCache);
    _factory.setShapePointHelper(shapePointService);
  }

  @Test
  public void test() {

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.1, -122.1);

    TripEntryImpl tripA = trip("tripA", "sA", 300.0);
    StopTimeEntryImpl st0 = stopTime(0, stopA, tripA, time(9, 00), time(9, 05),
        100.0);
    StopTimeEntryImpl st1 = stopTime(1, stopB, tripA, time(9, 30), time(9, 35),
        200.0);

    TripEntryImpl tripB = trip("tripB", "sA", 300.0);
    StopTimeEntryImpl st2 = stopTime(2, stopA, tripB, time(10, 00),
        time(10, 05), 100.0);
    StopTimeEntryImpl st3 = stopTime(3, stopB, tripB, time(10, 30),
        time(10, 35), 200.0);

    TripEntryImpl tripC = trip("tripC", "sB", 300.0);
    StopTimeEntryImpl st4 = stopTime(4, stopA, tripC, time(11, 00),
        time(11, 05), 100.0);
    StopTimeEntryImpl st5 = stopTime(5, stopB, tripC, time(11, 30),
        time(11, 35), 200.0);

    TripEntryImpl tripD = trip("tripD", "sB", 300.0);
    StopTimeEntryImpl st6 = stopTime(6, stopA, tripD, time(12, 00),
        time(12, 05), 100.0);
    StopTimeEntryImpl st7 = stopTime(7, stopB, tripD, time(12, 30),
        time(12, 35), 200.0);

    /****
     * Actual Test
     ****/

    BlockEntryImpl block = new BlockEntryImpl();
    List<TripEntryImpl> tripsInBlock = Arrays.asList(tripA, tripB, tripC, tripD);

    _factory.processBlockConfigurations(block, tripsInBlock);

    List<BlockConfigurationEntry> configurations = block.getConfigurations();
    assertEquals(3, configurations.size());

    /****
     * Order of the configurations matter. See
     * {@link BlockEntry#getConfigurations()} for details.
     ****/

    /****
     * Configuration A & B
     ****/

    BlockConfigurationEntry entry = configurations.get(0);
    assertSame(block, entry.getBlock());
    assertEquals(serviceIds(lsids("sA", "sB"), lsids()), entry.getServiceIds());
    assertEquals(1200.0, entry.getTotalBlockDistance(), 0.0);
    assertNull(entry.getFrequencies());

    List<BlockTripEntry> trips = entry.getTrips();
    assertEquals(4, trips.size());

    BlockTripEntry trip = trips.get(0);
    assertEquals(0, trip.getAccumulatedStopTimeIndex());
    assertEquals(0, trip.getAccumulatedSlackTime());
    assertEquals(0.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripA, trip.getTrip());
    assertNull(trip.getPreviousTrip());
    assertSame(trips.get(1), trip.getNextTrip());

    trip = trips.get(1);
    assertEquals(2, trip.getAccumulatedStopTimeIndex());
    assertEquals(10 * 60, trip.getAccumulatedSlackTime());
    assertEquals(300.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripB, trip.getTrip());
    assertSame(trips.get(0), trip.getPreviousTrip());
    assertSame(trips.get(2), trip.getNextTrip());

    trip = trips.get(2);
    assertEquals(4, trip.getAccumulatedStopTimeIndex());
    assertEquals(20 * 60, trip.getAccumulatedSlackTime());
    assertEquals(600.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripC, trip.getTrip());
    assertSame(trips.get(1), trip.getPreviousTrip());
    assertSame(trips.get(3), trip.getNextTrip());

    trip = trips.get(3);
    assertEquals(6, trip.getAccumulatedStopTimeIndex());
    assertEquals(30 * 60, trip.getAccumulatedSlackTime());
    assertEquals(900.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripD, trip.getTrip());
    assertSame(trips.get(2), trip.getPreviousTrip());
    assertNull(trip.getNextTrip());

    List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();
    assertEquals(8, stopTimes.size());

    BlockStopTimeEntry bst = stopTimes.get(0);
    assertEquals(0, bst.getAccumulatedSlackTime());
    assertEquals(0, bst.getBlockSequence());
    assertEquals(100.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st0, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(1);
    assertEquals(5 * 60, bst.getAccumulatedSlackTime());
    assertEquals(1, bst.getBlockSequence());
    assertEquals(200.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st1, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(2);
    assertEquals(10 * 60, bst.getAccumulatedSlackTime());
    assertEquals(2, bst.getBlockSequence());
    assertEquals(400.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st2, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());

    bst = stopTimes.get(3);
    assertEquals(15 * 60, bst.getAccumulatedSlackTime());
    assertEquals(3, bst.getBlockSequence());
    assertEquals(500.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st3, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());

    bst = stopTimes.get(4);
    assertEquals(20 * 60, bst.getAccumulatedSlackTime());
    assertEquals(4, bst.getBlockSequence());
    assertEquals(700.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st4, bst.getStopTime());
    assertSame(trips.get(2), bst.getTrip());

    bst = stopTimes.get(5);
    assertEquals(25 * 60, bst.getAccumulatedSlackTime());
    assertEquals(5, bst.getBlockSequence());
    assertEquals(800.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st5, bst.getStopTime());
    assertSame(trips.get(2), bst.getTrip());

    bst = stopTimes.get(6);
    assertEquals(30 * 60, bst.getAccumulatedSlackTime());
    assertEquals(6, bst.getBlockSequence());
    assertEquals(1000.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st6, bst.getStopTime());
    assertSame(trips.get(3), bst.getTrip());

    bst = stopTimes.get(7);
    assertEquals(35 * 60, bst.getAccumulatedSlackTime());
    assertEquals(7, bst.getBlockSequence());
    assertEquals(1100.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st7, bst.getStopTime());
    assertSame(trips.get(3), bst.getTrip());

    /****
     * Configuration A
     ****/

    entry = configurations.get(1);
    assertSame(block, entry.getBlock());
    assertEquals(serviceIds(lsids("sA"), lsids("sB")), entry.getServiceIds());
    assertEquals(600.0, entry.getTotalBlockDistance(), 0.0);
    assertNull(entry.getFrequencies());

    trips = entry.getTrips();
    assertEquals(2, trips.size());

    trip = trips.get(0);
    assertEquals(0, trip.getAccumulatedStopTimeIndex());
    assertEquals(0, trip.getAccumulatedSlackTime());
    assertEquals(0.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripA, trip.getTrip());
    assertNull(trip.getPreviousTrip());
    assertSame(trips.get(1), trip.getNextTrip());

    trip = trips.get(1);
    assertEquals(2, trip.getAccumulatedStopTimeIndex());
    assertEquals(10 * 60, trip.getAccumulatedSlackTime());
    assertEquals(300.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripB, trip.getTrip());
    assertSame(trips.get(0), trip.getPreviousTrip());
    assertNull(trip.getNextTrip());

    stopTimes = entry.getStopTimes();
    assertEquals(4, stopTimes.size());

    bst = stopTimes.get(0);
    assertEquals(0, bst.getAccumulatedSlackTime());
    assertEquals(0, bst.getBlockSequence());
    assertEquals(100.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st0, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(1);
    assertEquals(5 * 60, bst.getAccumulatedSlackTime());
    assertEquals(1, bst.getBlockSequence());
    assertEquals(200.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st1, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(2);
    assertEquals(10 * 60, bst.getAccumulatedSlackTime());
    assertEquals(2, bst.getBlockSequence());
    assertEquals(400.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st2, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());

    bst = stopTimes.get(3);
    assertEquals(15 * 60, bst.getAccumulatedSlackTime());
    assertEquals(3, bst.getBlockSequence());
    assertEquals(500.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st3, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());

    /****
     * Configuration B
     ****/

    entry = configurations.get(2);
    assertSame(block, entry.getBlock());
    assertEquals(serviceIds(lsids("sB"), lsids("sA")), entry.getServiceIds());
    assertEquals(600.0, entry.getTotalBlockDistance(), 0.0);
    assertNull(entry.getFrequencies());

    trips = entry.getTrips();
    assertEquals(2, trips.size());

    trip = trips.get(0);
    assertEquals(0, trip.getAccumulatedStopTimeIndex());
    assertEquals(0, trip.getAccumulatedSlackTime());
    assertEquals(0.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripC, trip.getTrip());
    assertNull(trip.getPreviousTrip());
    assertSame(trips.get(1), trip.getNextTrip());

    trip = trips.get(1);
    assertEquals(2, trip.getAccumulatedStopTimeIndex());
    assertEquals(10 * 60, trip.getAccumulatedSlackTime());
    assertEquals(300.0, trip.getDistanceAlongBlock(), 0.0);
    assertSame(tripD, trip.getTrip());
    assertSame(trips.get(0), trip.getPreviousTrip());
    assertNull(trip.getNextTrip());

    stopTimes = entry.getStopTimes();
    assertEquals(4, stopTimes.size());

    bst = stopTimes.get(0);
    assertEquals(0, bst.getAccumulatedSlackTime());
    assertEquals(0, bst.getBlockSequence());
    assertEquals(100.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st4, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(1);
    assertEquals(5 * 60, bst.getAccumulatedSlackTime());
    assertEquals(1, bst.getBlockSequence());
    assertEquals(200.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st5, bst.getStopTime());
    assertSame(trips.get(0), bst.getTrip());

    bst = stopTimes.get(2);
    assertEquals(10 * 60, bst.getAccumulatedSlackTime());
    assertEquals(2, bst.getBlockSequence());
    assertEquals(400.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st6, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());

    bst = stopTimes.get(3);
    assertEquals(15 * 60, bst.getAccumulatedSlackTime());
    assertEquals(3, bst.getBlockSequence());
    assertEquals(500.0, bst.getDistanceAlongBlock(), 0.0);
    assertSame(st7, bst.getStopTime());
    assertSame(trips.get(1), bst.getTrip());
  }
}
