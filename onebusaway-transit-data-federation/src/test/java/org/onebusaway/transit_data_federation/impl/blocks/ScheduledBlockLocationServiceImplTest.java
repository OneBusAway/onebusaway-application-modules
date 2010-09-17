package org.onebusaway.transit_data_federation.impl.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.aid;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.stop;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.time;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.transit_data_federation.impl.blocks.ScheduledBlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class ScheduledBlockLocationServiceImplTest {

  private ScheduledBlockLocationServiceImpl _service;

  private ShapePointService _shapePointService;

  private TripEntryImpl _tripA;

  private TripEntryImpl _tripB;

  private StopTimeEntryImpl _stopTimeA;

  private StopTimeEntryImpl _stopTimeB;

  private StopTimeEntryImpl _stopTimeC;

  private List<StopTimeEntry> _stopTimes;

  private StopEntryImpl _stopA;

  private StopEntryImpl _stopB;

  private StopEntryImpl _stopC;

  @Before
  public void before() {
    _service = new ScheduledBlockLocationServiceImpl();
    _shapePointService = Mockito.mock(ShapePointService.class);
    _service.setShapePointService(_shapePointService);

    _tripA = trip("A", 0.0);
    _tripB = trip("B", 1000.0);

    _tripA.setShapeId(aid("shapeA"));
    _tripB.setShapeId(aid("shapeB"));
    
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

    _stopTimeA = stopTime(1, _stopA, _tripA, time(10, 00), time(10, 00), 200);
    _stopTimeB = stopTime(2, _stopB, _tripA, time(10, 10), time(10, 15), 800);
    _stopTimeC = stopTime(3, _stopC, _tripB, time(10, 20), time(10, 20), 200);

    _stopTimes = Arrays.asList((StopTimeEntry) _stopTimeA, _stopTimeB,
        _stopTimeC);
  }

  @Test
  public void test00() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(9, 55));
    assertNull(position);

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _stopTimes, -100);
    assertNull(position);
  }

  @Test
  public void test01() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 00));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(200.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopA.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopA.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,00),position.getScheduledTime());
    
    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _stopTimes, 200.0);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(200.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopA.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopA.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,00),position.getScheduledTime());
  }

  @Test
  public void test02() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 02));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(320.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668651, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.385467, position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,02),position.getScheduledTime());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _stopTimes, 320.0);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeA, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(320.0, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.668651, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.385467, position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,02),position.getScheduledTime());
  }

  @Test
  public void test03() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 8));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(680, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6666929645559, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38214275139767, position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,8),position.getScheduledTime());

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _stopTimes, 680);

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(680, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6666929645559, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.38214275139767, position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,8),position.getScheduledTime());
  }

  @Test
  public void test04() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 12));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(800, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopB.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopB.getStopLon(), position.getLocation().getLon(), 1e-6);
    assertEquals(time(10,12),position.getScheduledTime());
  }

  @Test
  public void test05() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 17));

    assertEquals(_tripA, position.getActiveTrip());
    assertEquals(_stopTimeB, position.getClosestStop());
    assertEquals(-120, position.getClosestStopTimeOffset());
    assertEquals(960, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.66471023595962, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.37984571150027, position.getLocation().getLon(), 1e-6);
  }

  @Test
  public void test06() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 18));

    assertEquals(_tripB, position.getActiveTrip());
    assertEquals(_stopTimeC, position.getClosestStop());
    assertEquals(120, position.getClosestStopTimeOffset());
    assertEquals(1040, position.getDistanceAlongBlock(), 0.0);
    assertEquals(47.6642256894362, position.getLocation().getLat(), 1e-6);
    assertEquals(-122.3790560454492, position.getLocation().getLon(), 1e-6);
  }

  @Test
  public void test07() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 20));

    assertEquals(_tripB, position.getActiveTrip());
    assertEquals(_stopTimeC, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertEquals(1200, position.getDistanceAlongBlock(), 0.0);
    assertEquals(_stopC.getStopLat(), position.getLocation().getLat(), 1e-6);
    assertEquals(_stopC.getStopLon(), position.getLocation().getLon(), 1e-6);
  }

  @Test
  public void test08() {

    ScheduledBlockLocation position = _service.getScheduledBlockLocationFromScheduledTime(
        _stopTimes, time(10, 25));

    assertNull(position);

    position = _service.getScheduledBlockLocationFromDistanceAlongBlock(
        _stopTimes, 1500);

    assertNull(position);
  }
}
