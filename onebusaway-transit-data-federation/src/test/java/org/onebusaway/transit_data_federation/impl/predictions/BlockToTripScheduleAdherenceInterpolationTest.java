package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.DateSupport;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleAdherenceRecord;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

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

    AgencyAndId blockId = new AgencyAndId("agency", "blockId");

    TripEntry tripA = getTripEntryWithStopTimeRange(6.5, 7.5);
    TripEntry tripB = getTripEntryWithStopTimeRange(7.5, 8.5);
    TripEntry tripC = getTripEntryWithStopTimeRange(9.0, 10.0);

    List<TripEntry> tripsForBlock = Arrays.asList(tripA, tripB, tripC);

    Mockito.when(_graphDao.getTripsForBlockId(blockId)).thenReturn(
        tripsForBlock);

    /****
     * 
     ****/

    Date serviceDate = DateSupport.date("2010-03-30 00:00");
    Date currentTime = DateSupport.date("2010-03-30 07:25");

    ScheduleAdherenceRecord record = new ScheduleAdherenceRecord();
    record.setBlockId(blockId);
    record.setServiceDate(serviceDate.getTime());
    record.setCurrentTime(currentTime.getTime());
    record.setScheduleDeviation(5 * 60);

    List<ScheduleAdherenceRecord> records = _service.interpolate(record);
    assertEquals(2, records.size());

    ScheduleAdherenceRecord recordA = records.get(0);
    assertEquals(tripA.getId(), recordA.getTripId());

    ScheduleAdherenceRecord recordB = records.get(1);
    assertEquals(tripB.getId(), recordB.getTripId());

    /****
     * 
     ****/

    serviceDate = DateSupport.date("2010-03-30 00:00");
    currentTime = DateSupport.date("2010-03-30 08:45");

    record = new ScheduleAdherenceRecord();
    record.setBlockId(blockId);
    record.setServiceDate(serviceDate.getTime());
    record.setCurrentTime(currentTime.getTime());
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

    record = new ScheduleAdherenceRecord();
    record.setBlockId(blockId);
    record.setServiceDate(serviceDate.getTime());
    record.setCurrentTime(currentTime.getTime());
    record.setScheduleDeviation(0 * 60);

    records = _service.interpolate(record);

    assertEquals(1, records.size());

    recordA = records.get(0);
    assertEquals(tripC.getId(), recordA.getTripId());
  }

  @Test
  public void test02() {

    AgencyAndId blockId = new AgencyAndId("agency", "blockId");

    TripEntry tripA = getTripEntryWithStopTimeRange(6.5, 7.5);
    TripEntry tripB = getTripEntryWithStopTimeRange(7.5, 8.5);
    TripEntry tripC = getTripEntryWithStopTimeRange(9.0, 10.0);

    List<TripEntry> tripsForBlock = Arrays.asList(tripA, tripB, tripC);

    Mockito.when(_graphDao.getTripsForBlockId(blockId)).thenReturn(
        tripsForBlock);

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

  private TripEntry getTripEntryWithStopTimeRange(double timeFrom, double timeTo) {

    StopTimeEntryImpl stopTimeA = new StopTimeEntryImpl();
    int tFrom = DateSupport.hourToSec(timeFrom);
    stopTimeA.setArrivalTime(tFrom);
    stopTimeA.setDepartureTime(tFrom);

    StopTimeEntryImpl stopTimeB = new StopTimeEntryImpl();
    int tTo = DateSupport.hourToSec(timeTo);
    stopTimeB.setArrivalTime(tTo);
    stopTimeB.setDepartureTime(tTo);

    TripEntryImpl trip = new TripEntryImpl();
    trip.setStopTimes(Arrays.asList((StopTimeEntry) stopTimeA, stopTimeB));
    return trip;
  }
}
