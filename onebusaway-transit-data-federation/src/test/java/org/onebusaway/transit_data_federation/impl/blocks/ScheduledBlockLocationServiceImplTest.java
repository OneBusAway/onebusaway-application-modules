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
package org.onebusaway.transit_data_federation.impl.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class ScheduledBlockLocationServiceImplTest {

  private ScheduledBlockLocationServiceImpl _service;

  private ShapePointService _shapePointService;

  private BlockTripEntry _tripA;

  private BlockTripEntry _tripB;

  private BlockStopTimeEntry _stopTimeA;

  private BlockStopTimeEntry _stopTimeB;

  private BlockStopTimeEntry _stopTimeC;

  private StopEntryImpl _stopA;

  private StopEntryImpl _stopB;

  private StopEntryImpl _stopC;

  private BlockConfigurationEntry _blockConfig;

  @Before
  public void before() {
    _service = new ScheduledBlockLocationServiceImpl();
    _shapePointService = Mockito.mock(ShapePointService.class);
    _service.setShapePointService(_shapePointService);

    TripEntryImpl tripA = trip("A", "serviceId", 1000.0);
    TripEntryImpl tripB = trip("B", "serviceId", 1000.0);

    tripA.setShapeId(aid("shapeA"));
    tripB.setShapeId(aid("shapeB"));

    ShapePointsFactory m = new ShapePointsFactory();
    m.setShapeId(aid("shapeA"));
    m.addPoint(47.670170374084805, -122.3875880241394);
    m.addPoint(47.66871094987642, -122.38756656646729);
    m.addPoint(47.66862425012441, -122.38610744476318);
    m.addPoint(47.66869649992775, -122.38439083099365);
    m.addPoint(47.664852671516094, -122.3800778388977);
    m.addPoint(47.664467962697906, -122.37945087847474);

    Mockito.when(_shapePointService.getShapePointsForShapeId(aid("shapeA"))).thenReturn(
        m.create());

    m = new ShapePointsFactory();
    m.setShapeId(aid("shapeB"));
    m.addPoint(47.664467962697906, -122.37945087847474);
    m.addPoint(47.663667674849385, -122.37814664840698);
    m.addPoint(47.663667674849385, -122.37355470657349);

    Mockito.when(_shapePointService.getShapePointsForShapeId(aid("shapeB"))).thenReturn(
        m.create());

    _stopA = stop("stopA", 47.66868114116101, -122.3870648978625);
    _stopB = stop("stopB", 47.66583195331816, -122.38117664826683);
    _stopC = stop("stopC", 47.663667674849385, -122.37724035677341);

    stopTime(1, _stopA, tripA, time(10, 00), time(10, 00), 200, 1);
    stopTime(2, _stopB, tripA, time(10, 10), time(10, 15), 800, 3);
    stopTime(3, _stopC, tripB, time(10, 20), time(10, 20), 200, 1);

    _blockConfig = linkBlockTrips("blockA", tripA, tripB);

    List<BlockTripEntry> trips = _blockConfig.getTrips();
    _tripA = trips.get(0);
    _tripB = trips.get(1);

    List<BlockStopTimeEntry> stopTimes = _blockConfig.getStopTimes();
    _stopTimeA = stopTimes.get(0);
    _stopTimeB = stopTimes.get(1);
    _stopTimeC = stopTimes.get(2);
  }

  @Test
  public void test00a() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(9, 55));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(300, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertEquals(_stopTimeA, position.getNextStop());
    assertEquals(300, position.getNextStopTimeOffset());
    assertEquals(0.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.670170374084805, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.3875880241394, position.getLocation().getLon(), 1e-6);
    assertEquals(270.8, position.getOrientation(), 0.1);
    assertEquals(time(9, 55), position.getScheduledTime());
    assertFalse(position.isInService());
    assertEquals(0, position.getStopTimeIndex());

    ScheduledBlockLocation next = _service.getScheduledBlockLocationFromScheduledTime(
        position, time(9, 57));
    assertEquals(time(9, 57), next.getScheduledTime());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 00));
    assertEquals(time(10, 00), next.getScheduledTime());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 10));
    assertEquals(time(10, 10), next.getScheduledTime());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 17));
    assertEquals(time(10, 17), next.getScheduledTime());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 20));
    assertEquals(time(10, 20), next.getScheduledTime());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, -100);
    assertNull(position);

  }

  @Test
  public void test00b() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(9, 59));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(60, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertEquals(_stopTimeA, position.getNextStop());
    assertEquals(60, position.getNextStopTimeOffset());
    assertEquals(140.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668911387520204, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38756951346872, position.getLocation().getLon(), 1e-6);
    assertEquals(270.8, position.getOrientation(), 0.1);
    assertEquals(time(9, 59), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(0, position.getStopTimeIndex());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 140.0);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(60, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertEquals(_stopTimeA, position.getNextStop());
    assertEquals(60, position.getNextStopTimeOffset());
    assertEquals(140.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668911387520204, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38756951346872, position.getLocation().getLon(), 1e-6);
    assertEquals(270.8, position.getOrientation(), 0.1);
    assertEquals(time(9, 59), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(0, position.getStopTimeIndex());
  }

  @Test
  public void test01() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 00));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertEquals(_stopTimeA, position.getNextStop());
    assertEquals(0, position.getNextStopTimeOffset());
    assertEquals(200.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopA.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopA.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(356.6, position.getOrientation(), 0.1);
    assertEquals(time(10, 00), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(0, position.getStopTimeIndex());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 200.0);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertEquals(_stopTimeA, position.getNextStop());
    assertEquals(0, position.getNextStopTimeOffset());
    assertEquals(200.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopA.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopA.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(356.6, position.getOrientation(), 0.1);
    assertEquals(time(10, 00), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(0, position.getStopTimeIndex());
  }

  @Test
  public void test02() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 02));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeA, position.getPreviousStop());
    assertEquals(_stopTimeB, position.getNextStop());
    assertEquals(480, position.getNextStopTimeOffset());
    assertEquals(320.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668651, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.385467, position.getLocation().getLon(), 1e-6);
    assertEquals(2.4, position.getOrientation(), 0.1);
    assertEquals(time(10, 02), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(1, position.getStopTimeIndex());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 320.0);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeA, position.getPreviousStop());
    assertEquals(_stopTimeB, position.getNextStop());
    assertEquals(480, position.getNextStopTimeOffset());
    assertEquals(320.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668651, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.385467, position.getLocation().getLon(), 1e-6);
    assertEquals(2.4, position.getOrientation(), 0.1);
    assertEquals(time(10, 02), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(1, position.getStopTimeIndex());
  }

  @Test
  public void test03() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 8));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeA, position.getPreviousStop());
    assertEquals(_stopTimeB, position.getNextStop());
    assertEquals(120, position.getNextStopTimeOffset());
    assertEquals(680, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6666929645559, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38214275139767, position.getLocation().getLon(), 1e-6);
    assertEquals(318.3, position.getOrientation(), 0.1);
    assertEquals(time(10, 8), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(1, position.getStopTimeIndex());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 680);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(_stopTimeA, position.getPreviousStop());
    assertEquals(_stopTimeB, position.getNextStop());
    assertEquals(120, position.getNextStopTimeOffset());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(680, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6666929645559, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38214275139767, position.getLocation().getLon(), 1e-6);
    assertEquals(318.3, position.getOrientation(), 0.1);
    assertEquals(time(10, 8), position.getScheduledTime());
    assertTrue(position.isInService());
    assertEquals(1, position.getStopTimeIndex());
  }

  @Test
  public void test04() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 12));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeA, position.getPreviousStop());
    assertEquals(_stopTimeB, position.getNextStop());
    assertEquals(0, position.getNextStopTimeOffset());
    assertEquals(800, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopB.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopB.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(318.3, position.getOrientation(), 0.1);
    assertEquals(time(10, 12), position.getScheduledTime());
    assertTrue(position.isInService());
  }

  @Test
  public void test05() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 17));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeB, position.getPreviousStop());
    assertEquals(_stopTimeC, position.getNextStop());
    assertEquals(180, position.getNextStopTimeOffset());
    assertEquals(960, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.66471023595962, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.37984571150027, position.getLocation().getLon(), 1e-6);
    assertEquals(328.5, position.getOrientation(), 0.1);
    assertTrue(position.isInService());
    assertEquals(2, position.getStopTimeIndex());
  }

  @Test
  public void test06() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 18));

    assertEquals(_tripB, position.getActiveTrip());
    assertEquals(_stopTimeC, position.getClosestStop());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeB, position.getPreviousStop());
    assertEquals(_stopTimeC, position.getNextStop());
    assertEquals(120, position.getNextStopTimeOffset());
    assertEquals(1040, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6642256894362, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.3790560454492, position.getLocation().getLon(), 1e-6);
    assertEquals(328.5, position.getOrientation(), 0.1);
  }

  @Test
  public void test07() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 20));

    assertEquals(_tripB, position.getActiveTrip());
    assertEquals(_stopTimeC, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(_stopTimeB, position.getPreviousStop());
    assertEquals(_stopTimeC, position.getNextStop());
    assertEquals(0, position.getNextStopTimeOffset());
    assertEquals(1200, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopC.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopC.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(0.0, position.getOrientation(), 0.1);
    assertTrue(position.isInService());
    assertEquals(2, position.getStopTimeIndex());
  }

  @Test
  public void test08() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 25));

    assertNull(position);

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 1500);

    assertEquals(_tripB, position.getActiveTrip());
    assertEquals(_stopTimeC, position.getClosestStop());
    assertEquals(-225, position.getClosestStopTimeOffset());
    assertNull(position.getPreviousStop());
    assertNull(position.getNextStop());
    assertEquals(0, position.getNextStopTimeOffset());
    assertEquals(1500, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.663667674849385, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.37355470657349, position.getLocation().getLon(), 1e-6);
    assertEquals(0.0, position.getOrientation(), 0.1);
    assertFalse(position.isInService());
    assertEquals(3, position.getStopTimeIndex());
  }

  @Test
  public void test09() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 2500);

    assertNull(position);
  }

  @Test
  public void testHint00() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(9, 55));

    ScheduledBlockLocation next = _service.getScheduledBlockLocationFromScheduledTime(
        position, time(9, 55));
    assertEquals(time(9, 55), next.getScheduledTime());
    assertEquals(0, next.getDistanceAlongBlock(), 0.0);
    assertEquals(0, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(9, 58));
    assertEquals(time(9, 58), next.getScheduledTime());
    assertEquals(80, next.getDistanceAlongBlock(), 0.0);
    assertEquals(0, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 00));
    assertEquals(time(10, 00), next.getScheduledTime());
    assertEquals(200, next.getDistanceAlongBlock(), 0.0);
    assertEquals(0, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 05));
    assertEquals(time(10, 05), next.getScheduledTime());
    assertEquals(500, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 10));
    assertEquals(time(10, 10), next.getScheduledTime());
    assertEquals(800, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 17));
    assertEquals(time(10, 17), next.getScheduledTime());
    assertEquals(960, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 20));
    assertEquals(time(10, 20), next.getScheduledTime());
    assertEquals(1200, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 25));
    assertNull(next);

    /****
     * 
     ****/

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, -100);
    assertNull(position);
  }

  @Test
  public void testHint01() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 05));

    try {
      _service.getScheduledBlockLocationFromScheduledTime(position, time(9, 58));
      fail();
    } catch (Throwable ex) {

    }

    ScheduledBlockLocation next = _service.getScheduledBlockLocationFromScheduledTime(
        position, time(10, 05));
    assertEquals(time(10, 05), next.getScheduledTime());
    assertEquals(500, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 10));
    assertEquals(time(10, 10), next.getScheduledTime());
    assertEquals(800, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 17));
    assertEquals(time(10, 17), next.getScheduledTime());
    assertEquals(960, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 20));
    assertEquals(time(10, 20), next.getScheduledTime());
    assertEquals(1200, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 25));
    assertNull(next);

    /****
     * 
     ****/

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _blockConfig, 500);

    try {
      _service.getScheduledBlockLocationFromDistanceAlongBlock(position, 400);
      fail();
    } catch (Throwable ex) {

    }

    next = _service.getScheduledBlockLocationFromDistanceAlongBlock(position,
        680);
    assertEquals(time(10, 8), next.getScheduledTime());
    assertEquals(680, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromDistanceAlongBlock(position,
        800);
    assertEquals(time(10, 10), next.getScheduledTime());
    assertEquals(800, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromDistanceAlongBlock(position,
        960);
    assertEquals(time(10, 17), next.getScheduledTime());
    assertEquals(960, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());
  }

  @Test
  public void testHint02() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _blockConfig, time(10, 12));

    ScheduledBlockLocation next = _service.getScheduledBlockLocationFromScheduledTime(
        position, time(10, 12));
    assertEquals(time(10, 12), next.getScheduledTime());
    assertEquals(800, next.getDistanceAlongBlock(), 0.0);
    assertEquals(1, next.getStopTimeIndex());

    next = _service.getScheduledBlockLocationFromScheduledTime(position,
        time(10, 17));
    assertEquals(time(10, 17), next.getScheduledTime());
    assertEquals(960, next.getDistanceAlongBlock(), 0.0);
    assertEquals(2, next.getStopTimeIndex());

  }

}