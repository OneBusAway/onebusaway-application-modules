package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.aid;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.block;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.linkBlockTrips;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stop;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.time;
import static org.onebusaway.transit_data_federation.impl.MockEntryFactory.trip;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTestSupport;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.realtime.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.TripLocation;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.onebusaway.utility.DateLibrary;

public class BlockLocationServiceImplTest {

  private BlockLocationServiceImpl _service;

  private TransitGraphDao _transitGraphDao;

  private ScheduledBlockLocationService _blockLocationService;

  @Before
  public void setup() {

    _service = new BlockLocationServiceImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_transitGraphDao);

    _blockLocationService = Mockito.mock(ScheduledBlockLocationService.class);
    _service.setScheduledBlockLocationService(_blockLocationService);

    _service.setBlockLocationRecordCache(TransitDataFederationBaseTestSupport.createCache());
  }

  @Test
  public void testApplyRealtimeData() {

    StopEntryImpl stopA = stop("a", 47.5, -122.5);
    StopEntryImpl stopB = stop("b", 47.6, -122.6);
    StopEntryImpl stopC = stop("c", 47.7, -122.7);
    StopEntryImpl stopD = stop("d", 47.8, -122.8);

    TripEntryImpl trip = trip("trip");
    BlockEntryImpl block = block("block");

    linkBlockTrips(block, trip);

    StopTimeEntryImpl stopTimeA = stopTime(0, stopA, trip, time(9, 10),
        time(9, 11), -1);
    StopTimeEntryImpl stopTimeB = stopTime(1, stopB, trip, time(9, 20),
        time(9, 22), -1);
    StopTimeEntryImpl stopTimeC = stopTime(2, stopC, trip, time(9, 30),
        time(9, 30), -1);
    StopTimeEntryImpl stopTimeD = stopTime(3, stopD, trip, time(9, 40),
        time(9, 45), -1);
    stopTimeB.setAccumulatedSlackTime(60);
    stopTimeC.setAccumulatedSlackTime(180);
    stopTimeD.setAccumulatedSlackTime(180);

    block.setStopTimes(Arrays.asList((StopTimeEntry) stopTimeA, stopTimeB,
        stopTimeC, stopTimeD));
    trip.setStopTimeIndices(0, 4);

    Mockito.when(_transitGraphDao.getTripEntryForId(aid("trip"))).thenReturn(
        trip);
    Mockito.when(_transitGraphDao.getBlockEntryForId(aid("block"))).thenReturn(
        block);

    Date date = DateLibrary.getTimeAsDay(new Date());
    long serviceDate = date.getTime();

    VehicleLocationRecord vprA = new VehicleLocationRecord();
    vprA.setTripId(trip.getId());
    vprA.setScheduleDeviation(120);
    vprA.setTimeOfRecord(t(serviceDate, 9, 0));
    vprA.setServiceDate(serviceDate);

    VehicleLocationRecord vprB = new VehicleLocationRecord();
    vprB.setTripId(trip.getId());
    vprB.setScheduleDeviation(130);
    vprB.setTimeOfRecord(t(serviceDate, 9, 8));
    vprB.setServiceDate(serviceDate);

    _service.handleVehicleLocationRecords(Arrays.asList(vprA, vprB));

    StopTimeInstanceProxy stiA = new StopTimeInstanceProxy(stopTimeA,
        serviceDate);
    StopTimeInstanceProxy stiB = new StopTimeInstanceProxy(stopTimeB,
        serviceDate);
    StopTimeInstanceProxy stiC = new StopTimeInstanceProxy(stopTimeC,
        serviceDate);
    StopTimeInstanceProxy stiD = new StopTimeInstanceProxy(stopTimeD,
        serviceDate);

    List<StopTimeInstanceProxy> stis = Arrays.asList(stiA, stiB, stiC, stiD);

    _service.applyRealtimeData(stis, t(serviceDate, 9, 0));

    assertEquals(120, stiA.getPredictedArrivalOffset());
    assertEquals(60, stiA.getPredictedDepartureOffset());
    assertEquals(60, stiB.getPredictedArrivalOffset());
    assertEquals(0, stiB.getPredictedDepartureOffset());
    assertEquals(0, stiC.getPredictedArrivalOffset());
    assertEquals(0, stiC.getPredictedDepartureOffset());
    assertEquals(0, stiD.getPredictedArrivalOffset());
    assertEquals(0, stiD.getPredictedDepartureOffset());

    _service.applyRealtimeData(stis, t(serviceDate, 9, 9));

    assertEquals(130, stiA.getPredictedArrivalOffset());
    assertEquals(70, stiA.getPredictedDepartureOffset());
    assertEquals(70, stiB.getPredictedArrivalOffset());
    assertEquals(0, stiB.getPredictedDepartureOffset());
    assertEquals(0, stiC.getPredictedArrivalOffset());
    assertEquals(0, stiC.getPredictedDepartureOffset());
    assertEquals(0, stiD.getPredictedArrivalOffset());
    assertEquals(0, stiD.getPredictedDepartureOffset());

    VehicleLocationRecord vpr = new VehicleLocationRecord();
    vpr.setTripId(trip.getId());
    vpr.setScheduleDeviation(240);
    vpr.setTimeOfRecord(t(serviceDate, 9, 10.5));
    vpr.setServiceDate(serviceDate);

    _service.handleVehicleLocationRecords(Arrays.asList(vpr));

    _service.applyRealtimeData(stis, t(serviceDate, 9, 11));

    assertEquals(240, stiA.getPredictedArrivalOffset());
    assertEquals(180, stiA.getPredictedDepartureOffset());
    assertEquals(180, stiB.getPredictedArrivalOffset());
    assertEquals(60, stiB.getPredictedDepartureOffset());
    assertEquals(60, stiC.getPredictedArrivalOffset());
    assertEquals(60, stiC.getPredictedDepartureOffset());
    assertEquals(60, stiD.getPredictedArrivalOffset());
    assertEquals(0, stiD.getPredictedDepartureOffset());

    vpr = new VehicleLocationRecord();
    vpr.setTripId(trip.getId());
    vpr.setScheduleDeviation(90);
    vpr.setTimeOfRecord(t(serviceDate, 9, 12));
    vpr.setServiceDate(serviceDate);

    _service.handleVehicleLocationRecords(Arrays.asList(vpr));

    _service.applyRealtimeData(stis, t(serviceDate, 9, 12.5));

    assertEquals(109, stiA.getPredictedArrivalOffset());
    assertEquals(90, stiA.getPredictedDepartureOffset());
    assertEquals(90, stiB.getPredictedArrivalOffset());
    assertEquals(0, stiB.getPredictedDepartureOffset());
    assertEquals(0, stiC.getPredictedArrivalOffset());
    assertEquals(0, stiC.getPredictedDepartureOffset());
    assertEquals(0, stiD.getPredictedArrivalOffset());
    assertEquals(0, stiD.getPredictedDepartureOffset());
  }

  @Test
  public void testWithShapeInfo() {

    StopEntryImpl stopA = stop("a", 47.5, -122.5);
    StopEntryImpl stopB = stop("b", 47.6, -122.4);
    StopEntryImpl stopC = stop("c", 47.5, -122.3);

    BlockEntryImpl block = block("block");

    TripEntryImpl tripA = trip("tripA");
    TripEntryImpl tripB = trip("tripB");

    linkBlockTrips(block, tripA, tripB);

    StopTimeEntry stopTimeA = stopTime(0, stopA, tripA, 30, 90, 0);
    StopTimeEntry stopTimeB = stopTime(1, stopB, tripA, 120, 120, 100);
    StopTimeEntry stopTimeC = stopTime(2, stopC, tripA, 180, 210, 200);

    StopTimeEntry stopTimeD = stopTime(3, stopC, tripB, 240, 240, 300);
    StopTimeEntry stopTimeE = stopTime(4, stopB, tripB, 270, 270, 400);
    StopTimeEntry stopTimeF = stopTime(5, stopA, tripB, 300, 300, 500);

    block.setStopTimes(Arrays.asList(stopTimeA, stopTimeB, stopTimeC,
        stopTimeD, stopTimeE, stopTimeF));

    tripA.setStopTimeIndices(0, 3);
    tripB.setStopTimeIndices(3, 6);

    long serviceDate = 1000 * 1000;

    double epsilon = 0.001;

    /*
     * Mockito.when(_blockLocationService.getScheduledBlockPosition(tripA,
     * 0)).thenReturn( null);
     */

    TripInstanceProxy tripInstance = new TripInstanceProxy(tripA, serviceDate);
    TripLocation position = _service.getPositionForTripInstance(tripInstance,
        t(serviceDate, 0, 0));

    assertFalse(position.isInService());
    assertNull(position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
    assertFalse(position.hasScheduleDeviation());
    assertTrue(Double.isNaN(position.getScheduleDeviation()));
    assertFalse(position.hasDistanceAlongTrip());
    assertTrue(Double.isNaN(position.getDistanceAlongTrip()));
    assertNull(position.getLocation());
    assertEquals(serviceDate, position.getServiceDate());
    assertEquals(0, position.getLastUpdateTime());
    assertEquals(tripA, position.getTrip());
    assertNull(position.getVehicleId());

    ScheduledBlockLocation p = new ScheduledBlockLocation();
    p.setActiveTrip(tripA);
    p.setClosestStop(stopTimeA);
    p.setClosestStopTimeOffset(0);
    p.setDistanceAlongBlock(0);
    p.setPosition(new CoordinatePoint(stopA.getStopLat(), stopA.getStopLon()));

    Mockito.when(
        _blockLocationService.getScheduledBlockPositionFromScheduledTime(
            block.getStopTimes(), 1800)).thenReturn(p);

    position = _service.getPositionForTripInstance(tripInstance,
        t(serviceDate, 0, 30));

    assertTrue(position.isInService());
    assertEquals(stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());

    assertEquals(stopA.getStopLocation(), position.getLocation());

    assertFalse(position.hasScheduleDeviation());
    assertTrue(Double.isNaN(position.getScheduleDeviation()));

    assertTrue(position.hasDistanceAlongTrip());
    assertEquals(0.0, position.getDistanceAlongTrip(), 0.0);

    assertEquals(serviceDate, position.getServiceDate());
    assertEquals(0, position.getLastUpdateTime());
    assertEquals(tripA, position.getTrip());
    assertNull(position.getVehicleId());

    assertEquals(47.5, position.getLocation().getLat(), epsilon);
    assertEquals(-122.5, position.getLocation().getLon(), epsilon);
    assertEquals(stopTimeA, position.getClosestStop());
    assertEquals(0, position.getClosestStopTimeOffset());
  }

  private long t(long serviceDate, int hours, double minutes) {
    return (long) (serviceDate + (((hours * 60) + minutes) * 60) * 1000);
  }
}
