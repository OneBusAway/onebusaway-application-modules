package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.aid;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.shapePoints;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stop;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.trip;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTestSupport;
import org.onebusaway.transit_data_federation.impl.predictions.TripTimePredictionServiceImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.TripPosition;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;

public class TripPositionServiceImplTest {

  private TripPositionServiceImpl _service;
  private ShapePointService _shapePointService;
  private NarrativeService _narrativeService;

  @Before
  public void setup() {
    _service = new TripPositionServiceImpl();

    TripTimePredictionServiceImpl tripTimePredictionService = new TripTimePredictionServiceImpl();
    tripTimePredictionService.setTripTimePredictionsCache(TransitDataFederationBaseTestSupport.createCache());
    _service.setTripTimePredictionService(tripTimePredictionService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

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
    Mockito.when(_narrativeService.getTripForId(trip.getId())).thenReturn(
        tripNarrative);

    StopTimeNarrative stnA = StopTimeNarrative.builder().setShapeDistTraveled(0).create();
    StopTimeNarrative stnB = StopTimeNarrative.builder().setShapeDistTraveled(
        100).create();
    StopTimeNarrative stnC = StopTimeNarrative.builder().setShapeDistTraveled(
        200).create();

    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeA)).thenReturn(
        stnA);
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeB)).thenReturn(
        stnB);
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeC)).thenReturn(
        stnC);

    double[] lats = {47.5, 47.56, 47.6, 47.54, 47.5};
    double[] lons = {-122.5, -122.45, -122.4, -122.35, -122.3};
    double[] distTraveled = {0, 50, 100, 150, 200};
    ShapePoints shapePoints = shapePoints("shape", lats, lons, distTraveled);
    Mockito.when(
        _shapePointService.getShapePointsForShapeId(tripNarrative.getShapeId())).thenReturn(
        shapePoints);

    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    TripInstanceProxy tripInstance = new TripInstanceProxy(trip, serviceDate);
    TripPosition position = _service.getPositionForTripInstance(
        tripInstance, 1000 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1030 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeA,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1060 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeA,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1090 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeA,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertEquals(47.56, position.getPosition().getLat(), epsilon);
    assertEquals(-122.45, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeB,position.getClosestStop());
    assertEquals(15,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertEquals(47.57333, position.getPosition().getLat(), epsilon);
    assertEquals(-122.433, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeB,position.getClosestStop());
    assertEquals(10,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1120 * 1000);

    assertEquals(47.6, position.getPosition().getLat(), epsilon);
    assertEquals(-122.4, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeB,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());
    
    position = _service.getPositionForTripInstance(tripInstance, 1135 * 1000);

    assertEquals(47.57, position.getPosition().getLat(), epsilon);
    assertEquals(-122.375, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeB,position.getClosestStop());
    assertEquals(-15,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1150 * 1000);

    assertEquals(47.54, position.getPosition().getLat(), epsilon);
    assertEquals(-122.35, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeC,position.getClosestStop());
    assertEquals(30,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1180 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.3, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeC,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());
    
    position = _service.getPositionForTripInstance(tripInstance, 1195 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.3, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeC,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());

    position = _service.getPositionForTripInstance(tripInstance, 1210 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.3, position.getPosition().getLon(), epsilon);
    assertEquals(stopTimeC,position.getClosestStop());
    assertEquals(0,position.getClosestStopTimeOffset());

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
    Mockito.when(_narrativeService.getTripForId(trip.getId())).thenReturn(
        tripNarrative);

    StopTimeNarrative stnA = StopTimeNarrative.builder().create();
    StopTimeNarrative stnB = StopTimeNarrative.builder().create();
    StopTimeNarrative stnC = StopTimeNarrative.builder().create();

    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeA)).thenReturn(
        stnA);
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeB)).thenReturn(
        stnB);
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeC)).thenReturn(
        stnC);

    _service.setInterpolateWhenNoShapeInfoPresent(false);
    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    TripInstanceProxy tripInstance = new TripInstanceProxy(trip, serviceDate);
    TripPosition position = _service.getPositionForTripInstance(
        tripInstance, 1000 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1030 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1060 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1090 * 1000);

    assertEquals(47.5, position.getPosition().getLat(), epsilon);
    assertEquals(-122.5, position.getPosition().getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertNull(position);

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertNull(position);

    _service.setInterpolateWhenNoShapeInfoPresent(true);

    position = _service.getPositionForTripInstance(tripInstance, 1105 * 1000);

    assertEquals(47.55, position.getPosition().getLat(), epsilon);
    assertEquals(-122.45, position.getPosition().getLon(), epsilon);

    position = _service.getPositionForTripInstance(tripInstance, 1110 * 1000);

    assertEquals(47.566, position.getPosition().getLat(), epsilon);
    assertEquals(-122.433, position.getPosition().getLon(), epsilon);
  }
}
