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
package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class BlockLocationServiceImplTest {

  private BlockLocationServiceImpl _service;

  private TransitGraphDao _transitGraphDao;

  private ScheduledBlockLocationService _blockLocationService;

  private BlockCalendarService _blockCalendarService;

  @Before
  public void setup() {

    _service = new BlockLocationServiceImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_transitGraphDao);

    _blockLocationService = Mockito.mock(ScheduledBlockLocationService.class);
    _service.setScheduledBlockLocationService(_blockLocationService);

    _service.setVehicleLocationRecordCache(new VehicleLocationRecordCacheImpl());

    _blockCalendarService = Mockito.mock(BlockCalendarService.class);
    _service.setBlockCalendarService(_blockCalendarService);
  }

  @Test
  public void testApplyRealtimeData() {

  }

  @Test
  public void testWithShapeInfo() {

    StopEntryImpl stopA = stop("a", 47.5, -122.5);
    StopEntryImpl stopB = stop("b", 47.6, -122.4);
    StopEntryImpl stopC = stop("c", 47.5, -122.3);

    BlockEntryImpl block = block("block");

    TripEntryImpl tripA = trip("tripA", "serviceId");
    TripEntryImpl tripB = trip("tripB", "serviceId");

    stopTime(0, stopA, tripA, 30, 90, 0, 50.0);
    stopTime(1, stopB, tripA, 120, 120, 100);
    stopTime(2, stopC, tripA, 180, 210, 200);

    stopTime(3, stopC, tripB, 240, 240, 300);
    stopTime(4, stopB, tripB, 270, 270, 400);
    stopTime(5, stopA, tripB, 300, 300, 500);

    BlockConfigurationEntry blockConfig = linkBlockTrips(block, tripA, tripB);

    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    TargetTime target = new TargetTime(t(
        serviceDate, 0, 0), System.currentTimeMillis());

    BlockInstance blockInstance = new BlockInstance(blockConfig, serviceDate);
    BlockLocation location = _service.getLocationForBlockInstance(
        blockInstance, target);

    assertNull(location);

    ScheduledBlockLocation p = new ScheduledBlockLocation();
    p.setActiveTrip(blockConfig.getTrips().get(0));
    p.setClosestStop(blockConfig.getStopTimes().get(0));
    p.setClosestStopTimeOffset(0);
    p.setDistanceAlongBlock(0);
    p.setLocation(new CoordinatePoint(stopA.getStopLat(), stopA.getStopLon()));
    p.setInService(true);

    Mockito.when(
        _blockLocationService.getScheduledBlockLocationFromScheduledTime(
            blockConfig, 1800)).thenReturn(p);

    target = new TargetTime(t(serviceDate, 0, 30), System.currentTimeMillis());

    location = _service.getLocationForBlockInstance(blockInstance, target);

    assertTrue(location.isInService());
    assertEquals(blockConfig.getStopTimes().get(0), location.getClosestStop());
    assertEquals(0, location.getClosestStopTimeOffset());

    assertEquals(stopA.getStopLocation(), location.getLocation());

    assertFalse(location.isScheduleDeviationSet());
    assertTrue(Double.isNaN(location.getScheduleDeviation()));

    assertFalse(location.isDistanceAlongBlockSet());
    assertTrue(Double.isNaN(location.getDistanceAlongBlock()));

    assertEquals(blockInstance, location.getBlockInstance());
    assertEquals(0, location.getLastUpdateTime());
    assertEquals(blockConfig.getTrips().get(0), location.getActiveTrip());
    assertNull(location.getVehicleId());

    assertEquals(47.5, location.getLocation().getLat(), epsilon);
    assertEquals(-122.5, location.getLocation().getLon(), epsilon);
    assertEquals(blockConfig.getStopTimes().get(0), location.getClosestStop());
    assertEquals(0, location.getClosestStopTimeOffset());
    assertEquals("fewSeatsAvailable", blockConfig.getStopTimes().get(0).getStopTime().getHistoricalOccupancy().toString());
  }

  private long t(long serviceDate, int hours, double minutes) {
    return (long) (serviceDate + (((hours * 60) + minutes) * 60) * 1000);
  }
}
