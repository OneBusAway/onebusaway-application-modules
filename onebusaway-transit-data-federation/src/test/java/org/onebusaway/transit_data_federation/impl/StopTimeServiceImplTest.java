package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.date;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.getTimeAsDay;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeIndexServiceImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class StopTimeServiceImplTest {

  private StopTimeServiceImpl _service;

  private ExtendedCalendarServiceImpl _calendarService;

  private AgencyAndId _stopId;

  private StopEntryImpl _stop;

  @Before
  public void setup() {

    _stop = stop("stopId", 47.0, -122.0);
    _stopId = _stop.getId();

    TransitGraphDao graph = Mockito.mock(TransitGraphDao.class);
    Mockito.when(graph.getStopEntryForId(_stop.getId())).thenReturn(_stop);

    CalendarServiceData data = new CalendarServiceData();
    data.putDatesForLocalizedServiceId(lsid("sA"),
        Arrays.asList(date("2009-09-01 00:00"), date("2009-09-02 00:00")));
    data.putDatesForLocalizedServiceId(lsid("sB"),
        Arrays.asList(date("2009-09-03 00:00")));

    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    calendarService.setData(data);

    _calendarService = new ExtendedCalendarServiceImpl();
    _calendarService.setCalendarService(calendarService);

    _service = new StopTimeServiceImpl();
    _service.setTransitGraphDao(graph);
    _service.setCalendarService(_calendarService);
    _service.setBlockStopTimeIndexService(new BlockStopTimeIndexServiceImpl());
  }

  @Test
  public void test01() {

    Date from = date("2009-09-01 10:00");
    Date to = date("2009-09-01 10:30");
    Date day = getTimeAsDay(from);

    StopTimeEntryImpl stA = stopTime(0, _stop, trip("A", "sA"), time(9, 50), 0);
    StopTimeEntryImpl stB = stopTime(1, _stop, trip("B", "sA"), time(10, 10), 0);
    StopTimeEntryImpl stC = stopTime(2, _stop, trip("C", "sA"), time(10, 20), 0);
    StopTimeEntryImpl stD = stopTime(3, _stop, trip("D", "sA"), time(10, 40), 0);

    BlockConfigurationEntry bA = linkBlockTrips("bA", stA.getTrip());
    BlockConfigurationEntry bB = linkBlockTrips("bB", stB.getTrip());
    BlockConfigurationEntry bC = linkBlockTrips("bC", stC.getTrip());
    BlockConfigurationEntry bD = linkBlockTrips("bD", stD.getTrip());

    addFirstStopToBlockIndex(bA, bB, bC, bD);

    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(dateAsLong("2009-09-01 10:10"), sti.getArrivalTime());
    assertEquals(dateAsLong("2009-09-01 10:10"), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-01 10:20").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-01 10:20").getTime(), sti.getDepartureTime());

    from = date("2009-09-01 10:15");
    to = date("2009-09-01 10:25");

    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-01 10:20").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-01 10:20").getTime(), sti.getDepartureTime());

    from = date("2009-09-01 10:21");
    to = date("2009-09-01 10:25");

    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    assertEquals(0, results.size());
  }

  @Test
  public void test02() {

    Date dayA = getTimeAsDay(date("2009-09-01 00:00"));
    Date dayB = getTimeAsDay(date("2009-09-02 00:00"));

    // 10:00am, 10:30am
    StopTimeEntryImpl stA = stopTime(0, _stop, trip("A", "sA"), time(10, 00),
        time(10, 30), 0);

    // 01:00am, 01:30am (both on next day)
    StopTimeEntryImpl stB = stopTime(1, _stop, trip("B", "sA"), time(25, 00),
        time(25, 30), 0);

    BlockConfigurationEntry bA = linkBlockTrips("bA", stA.getTrip());
    BlockConfigurationEntry bB = linkBlockTrips("bB", stB.getTrip());

    addFirstStopToBlockIndex(bA, bB);

    /****
     * 
     ****/

    Date from = date("2009-09-01 10:10");
    Date to = date("2009-09-01 10:40");
    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-02 10:10");
    to = date("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-01 10:10");
    to = date("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(3, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(2);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-01 12:00");
    to = date("2009-09-02 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-02 12:00");
    to = date("2009-09-03 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 01:30").getTime(), sti.getDepartureTime());
  }

  @Test
  public void test03() {

    Date dayA = getTimeAsDay(date("2009-09-02 00:00"));
    Date dayB = getTimeAsDay(date("2009-09-03 00:00"));

    StopTimeEntryImpl stA = stopTime(0, _stop, trip("A", "sA"), time(10, 00),
        time(10, 30), 0);
    StopTimeEntryImpl stB = stopTime(1, _stop, trip("B", "sA"), time(25, 0),
        time(25, 30), 0);
    StopTimeEntryImpl stC = stopTime(2, _stop, trip("C", "sB"), time(10, 00),
        time(10, 30), 0);
    StopTimeEntryImpl stD = stopTime(3, _stop, trip("D", "sB"), time(25, 0),
        time(25, 30), 0);

    BlockConfigurationEntry bA = linkBlockTrips("bA", stA.getTrip());
    BlockConfigurationEntry bB = linkBlockTrips("bB", stB.getTrip());
    BlockConfigurationEntry bC = linkBlockTrips("bC", stC.getTrip());
    BlockConfigurationEntry bD = linkBlockTrips("bD", stD.getTrip());

    addFirstStopToBlockIndex(bA, bB);
    addFirstStopToBlockIndex(bC, bD);

    /****
     * 
     ****/

    Date from = date("2009-09-02 10:10");
    Date to = date("2009-09-02 10:40");
    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-03 10:10");
    to = date("2009-09-03 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-02 10:10");
    to = date("2009-09-03 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(3, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(2);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-02 12:00");
    to = date("2009-09-03 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-03 10:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-03 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = date("2009-09-03 12:00");
    to = date("2009-09-04 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(date("2009-09-04 01:00").getTime(), sti.getArrivalTime());
    assertEquals(date("2009-09-04 01:30").getTime(), sti.getDepartureTime());
  }

  /****
   * Private Methods
   ****/

  private void addFirstStopToBlockIndex(BlockConfigurationEntry... blocks) {

    List<BlockConfigurationEntry> blockConfigs = new ArrayList<BlockConfigurationEntry>();

    for (BlockConfigurationEntry blockConfig : blocks) {
      blockConfigs.add(blockConfig);
    }

    BlockIndicesFactory factory = new BlockIndicesFactory();
    BlockIndex blockIndex = factory.createIndexForGroupOfBlocks(blockConfigs);

    _stop.getStopTimeIndices().add(BlockStopTimeIndex.create(blockIndex, 0));
  }

  private void sort(List<StopTimeInstanceProxy> stopTimes) {
    Collections.sort(stopTimes, new StopTimeInstanceComparator());
  }

  private static class StopTimeInstanceComparator implements
      Comparator<StopTimeInstanceProxy> {
    public int compare(StopTimeInstanceProxy o1, StopTimeInstanceProxy o2) {
      return (int) (o1.getArrivalTime() - o2.getArrivalTime());
    }
  }
}
