package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.aid;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.shapePoints;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stop;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.trip;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.impl.predictions.TripTimePredictionServiceImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.narrative.StopTimeNarrativeService;
import org.onebusaway.transit_data_federation.services.narrative.TripNarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class TripPositionServiceImplTest {

  private TripPositionServiceImpl _service;
  private TripNarrativeService _tripNarrativeService;
  private ShapePointService _shapePointService;
  private StopTimeNarrativeService _stopTimeNarrativeService;

  @Before
  public void setup() {
    _service = new TripPositionServiceImpl();

    _service.setTripTimePredictionService(new TripTimePredictionServiceImpl());

    _tripNarrativeService = Mockito.mock(TripNarrativeService.class);
    _service.setTripNarrativeService(_tripNarrativeService);

    _stopTimeNarrativeService = Mockito.mock(StopTimeNarrativeService.class);
    _service.setStopTimeNarrativeService(_stopTimeNarrativeService);

    _shapePointService = Mockito.mock(ShapePointService.class);
    _service.setShapePointService(_shapePointService);
  }

  @Test
  public void testWithShapeInfo() {

    StopEntryImpl stopA = stop("a", 47.5, -122.5);
    StopEntryImpl stopB = stop("b", 47.6, -122.4);
    StopEntryImpl stopC = stop("c", 47.5, -122.3);

    TripEntryImpl trip = trip("trip");

    StopTimeEntry stopTimeA = stopTime(0, stopA, trip, 30, 90);
    StopTimeEntry stopTimeB = stopTime(1, stopB, trip, 120, 120);
    StopTimeEntry stopTimeC = stopTime(2, stopC, trip, 180, 210);

    trip.setStopTimes(Arrays.asList(stopTimeA, stopTimeB, stopTimeC));

    TripNarrative tripNarrative = TripNarrative.builder().setShapeId(
        aid("shape")).create();
    Mockito.when(_tripNarrativeService.getTripForId(trip.getId())).thenReturn(
        tripNarrative);

    StopTimeNarrative stnA = StopTimeNarrative.builder().setShapeDistTraveled(0).create();
    StopTimeNarrative stnB = StopTimeNarrative.builder().setShapeDistTraveled(
        100).create();
    StopTimeNarrative stnC = StopTimeNarrative.builder().setShapeDistTraveled(
        200).create();

    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeA)).thenReturn(
        stnA);
    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeB)).thenReturn(
        stnB);
    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeC)).thenReturn(
        stnC);

    double[] lats = {47.5, 47.6, 47.5};
    double[] lons = {-122.5, -122.4, -122.3};
    double[] distTraveled = {0, 100, 200};
    ShapePoints shapePoints = shapePoints("shape", lats, lons, distTraveled);
    Mockito.when(
        _shapePointService.getShapePointsForShapeId(tripNarrative.getShapeId())).thenReturn(
        shapePoints);

    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    TripInstanceProxy tripInstance = new TripInstanceProxy(trip, serviceDate);
    CoordinatePoint position = _service.getPositionForTripInstance(
        tripInstance, 1000 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1030 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1060 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1090 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertEquals(47.55, position.getLat(), epsilon);
    assertEquals(-122.45, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertEquals(47.566, position.getLat(), epsilon);
    assertEquals(-122.433, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1120 * 1000);

    assertEquals(47.6, position.getLat(), epsilon);
    assertEquals(-122.4, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1150 * 1000);

    assertEquals(47.55, position.getLat(), epsilon);
    assertEquals(-122.35, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1180 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.3, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1195 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.3, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1210 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.3, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1211 * 1000);

    assertNull(position);
  }

  @Test
  public void testWithoutShapeInfo() {

    StopEntryImpl stopA = stop("a", 47.5, -122.5);
    StopEntryImpl stopB = stop("b", 47.6, -122.4);
    StopEntryImpl stopC = stop("c", 47.5, -122.3);

    TripEntryImpl trip = trip("trip");

    StopTimeEntry stopTimeA = stopTime(0, stopA, trip, 30, 90);
    StopTimeEntry stopTimeB = stopTime(1, stopB, trip, 120, 120);
    StopTimeEntry stopTimeC = stopTime(2, stopC, trip, 180, 210);

    trip.setStopTimes(Arrays.asList(stopTimeA, stopTimeB, stopTimeC));

    TripNarrative tripNarrative = TripNarrative.builder().create();
    Mockito.when(_tripNarrativeService.getTripForId(trip.getId())).thenReturn(
        tripNarrative);

    StopTimeNarrative stnA = StopTimeNarrative.builder().create();
    StopTimeNarrative stnB = StopTimeNarrative.builder().create();
    StopTimeNarrative stnC = StopTimeNarrative.builder().create();

    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeA)).thenReturn(
        stnA);
    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeB)).thenReturn(
        stnB);
    Mockito.when(_stopTimeNarrativeService.getStopTimeForEntry(stopTimeC)).thenReturn(
        stnC);

    _service.setInterpolateWhenNoShapeInfoPresent(false);
    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    TripInstanceProxy tripInstance = new TripInstanceProxy(trip, serviceDate);
    CoordinatePoint position = _service.getPositionForTripInstance(
        tripInstance, 1000 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1030 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1060 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1090 * 1000);

    assertEquals(47.5, position.getLat(), epsilon);
    assertEquals(-122.5, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertNull(position);

    _service.setInterpolateWhenNoShapeInfoPresent(true);

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertEquals(47.55, position.getLat(), epsilon);
    assertEquals(-122.45, position.getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertEquals(47.566, position.getLat(), epsilon);
    assertEquals(-122.433, position.getLon(), epsilon);
  }
}
