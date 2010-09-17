package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.testing.DateSupport;
import org.onebusaway.transit_data_federation.testing.MockEntryFactory;

public class BlockToTripScheduleAdherenceInterpolationTest {

  private BlockToTripScheduleAdherenceInterpolation _service;

  private TransitGraphDao _graphDao;

  @Before
  public void setup() {

    _service = new BlockToTripScheduleAdherenceInterpolation();

    _graphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_graphDao);
  }

  @Test
  public void test() {

    BlockEntryImpl block = MockEntryFactory.block("blockId");

    TripEntryImpl tripA = MockEntryFactory.trip("tripA");
    TripEntryImpl tripB = MockEntryFactory.trip("tripB");
    TripEntryImpl tripC = MockEntryFactory.trip("tripC");

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    addStopTimeRange(stopTimes, tripA, 6.5, 7.5);
    addStopTimeRange(stopTimes, tripB, 7.5, 8.5);
    addStopTimeRange(stopTimes, tripC, 9.0, 10.0);
    block.setStopTimes(stopTimes);

    MockEntryFactory.linkBlockTrips(block, tripA, tripB, tripC);

    Mockito.when(_graphDao.getTripsForBlockId(block.getId())).thenReturn(
        block.getTrips());

    /****
     * 
     ****/

    Date serviceDate = DateSupport.date("2010-03-30 00:00");
    Date currentTime = DateSupport.date("2010-03-30 07:25");

    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setBlockId(block.getId());
    record.setServiceDate(serviceDate.getTime());
    record.setTimeOfRecord(currentTime.getTime());
    record.setScheduleDeviation(5 * 60);

    List<VehicleLocationRecord> records = _service.interpolate(record);
    assertEquals(2, records.size());

    VehicleLocationRecord recordA = records.get(0);
    assertEquals(tripA.getId(), recordA.getTripId());

    VehicleLocationRecord recordB = records.get(1);
    assertEquals(tripB.getId(), recordB.getTripId());

    /****
     * 
     ****/

    serviceDate = DateSupport.date("2010-03-30 00:00");
    currentTime = DateSupport.date("2010-03-30 08:45");

    record = new VehicleLocationRecord();
    record.setBlockId(block.getId());
    record.setServiceDate(serviceDate.getTime());
    record.setTimeOfRecord(currentTime.getTime());
    record.setScheduleDeviation(20 * 60);

    records = _service.interpolate(record);

    assertEquals(1, records.size());

    recordA = records.get(0);
    assertEquals(tripB.getId(), recordA.getTripId());

    /****
     * 
     ****/

    serviceDate = DateSupport.date("2010-03-30 00:00");
    currentTime = DateSupport.date("2010-03-30 08:45");

    record = new VehicleLocationRecord();
    record.setBlockId(block.getId());
    record.setServiceDate(serviceDate.getTime());
    record.setTimeOfRecord(currentTime.getTime());
    record.setScheduleDeviation(0 * 60);

    records = _service.interpolate(record);

    assertEquals(1, records.size());

    recordA = records.get(0);
    assertEquals(tripC.getId(), recordA.getTripId());
  }

  @Test
  public void test02() {

    BlockEntryImpl block = MockEntryFactory.block("blockId");
    AgencyAndId blockId = block.getId();
    
    TripEntryImpl tripA = MockEntryFactory.trip("tripA");
    TripEntryImpl tripB = MockEntryFactory.trip("tripB");
    TripEntryImpl tripC = MockEntryFactory.trip("tripC");

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    addStopTimeRange(stopTimes, tripA, 6.5, 7.5);
    addStopTimeRange(stopTimes, tripB, 7.5, 8.5);
    addStopTimeRange(stopTimes, tripC, 9.0, 10.0);
    block.setStopTimes(stopTimes);

    MockEntryFactory.linkBlockTrips(block, tripA, tripB, tripC);

    Mockito.when(_graphDao.getTripsForBlockId(block.getId())).thenReturn(
        block.getTrips());

    List<TripEntry> trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(6), DateSupport.hourToSec(7));

    assertEquals(1, trips.size());
    assertEquals(tripA, trips.get(0));

    trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(6), DateSupport.hourToSec(8));

    assertEquals(2, trips.size());
    assertEquals(tripA, trips.get(0));
    assertEquals(tripB, trips.get(1));

    trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(8), DateSupport.hourToSec(8.5));

    assertEquals(1, trips.size());
    assertEquals(tripB, trips.get(0));

    trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(6), DateSupport.hourToSec(11));

    assertEquals(3, trips.size());
    assertEquals(tripA, trips.get(0));
    assertEquals(tripB, trips.get(1));
    assertEquals(tripC, trips.get(2));

    trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(10.5), DateSupport.hourToSec(11));

    assertEquals(0, trips.size());

    trips = _service.getTripsInTimeRangeForBlock(blockId,
        DateSupport.hourToSec(8), DateSupport.hourToSec(9.5));

    assertEquals(2, trips.size());
    assertEquals(tripB, trips.get(0));
    assertEquals(tripC, trips.get(1));
  }

  private void addStopTimeRange(List<StopTimeEntry> stopTimes,
      TripEntryImpl trip, double timeFrom, double timeTo) {

    StopTimeEntryImpl stopTimeA = new StopTimeEntryImpl();
    int tFrom = DateSupport.hourToSec(timeFrom);
    stopTimeA.setArrivalTime(tFrom);
    stopTimeA.setDepartureTime(tFrom);
    stopTimeA.setTrip(trip);

    StopTimeEntryImpl stopTimeB = new StopTimeEntryImpl();
    int tTo = DateSupport.hourToSec(timeTo);
    stopTimeB.setArrivalTime(tTo);
    stopTimeB.setDepartureTime(tTo);
    stopTimeB.setTrip(trip);

    trip.setStopTimeIndices(stopTimes.size(), stopTimes.size() + 2);

    stopTimes.add(stopTimeA);
    stopTimes.add(stopTimeB);
  }
}
