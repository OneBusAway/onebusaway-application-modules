package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.date;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.getTimeAsDay;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
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
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndexFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyStopTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public class StopTimeServiceImplTest {

  private static final BlockIndexFactory _factory = new BlockIndexFactory();

  private StopTimeServiceImpl _service;

  private ExtendedCalendarServiceImpl _calendarService;

  private BlockIndexService _blockIndexService;

  private AgencyAndId _stopId;

  private StopEntryImpl _stop;

  private TransitGraphDao _transitGraphDao;

  @Before
  public void setup() {

    _stop = stop("stopId", 47.0, -122.0);
    _stopId = _stop.getId();

    TransitGraphDao graph = Mockito.mock(TransitGraphDao.class);
    Mockito.when(graph.getStopEntryForId(_stop.getId(), true)).thenReturn(_stop);

    CalendarServiceData data = new CalendarServiceData();
    data.putDatesForLocalizedServiceId(lsid("sA"),
        Arrays.asList(date("2009-09-01 00:00"), date("2009-09-02 00:00")));
    data.putDatesForLocalizedServiceId(lsid("sB"),
        Arrays.asList(date("2009-09-03 00:00")));

    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    calendarService.setData(data);

    _calendarService = new ExtendedCalendarServiceImpl();
    _calendarService.setCalendarService(calendarService);

    _blockIndexService = Mockito.mock(BlockIndexService.class);

    _service = new StopTimeServiceImpl();
    _service.setTransitGraphDao(graph);
    _service.setCalendarService(_calendarService);
    _service.setBlockIndexService(_blockIndexService);

    BlockConfigurationEntry bcA = blockConfiguration(block("bA"),
        serviceIds(lsids("sA"), lsids()));
    BlockConfigurationEntry bcB = blockConfiguration(block("bB"),
        serviceIds(lsids("sB"), lsids()));

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    Mockito.when(_transitGraphDao.getAllBlocks()).thenReturn(
        Arrays.asList(bcA.getBlock(), bcB.getBlock()));
    _calendarService.setTransitGraphDao(_transitGraphDao);

    _calendarService.start();
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

    List<StopTimeInstance> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    StopTimeInstance sti = results.get(0);
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
    List<StopTimeInstance> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstance sti = results.get(0);
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
    List<StopTimeInstance> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstance sti = results.get(0);
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

  @Test
  public void test04() {

    StopEntryImpl fromStop = stop("stopA", 47.0, -122.0);
    StopEntryImpl toStop = stop("stopB", 47.0, -122.0);

    TripEntryImpl trip1A = trip("1A", "sA");
    TripEntryImpl trip1B = trip("1B", "sA");
    TripEntryImpl trip1C = trip("1C", "sA");
    TripEntryImpl trip1D = trip("1D", "sA");

    stopTime(0, fromStop, trip1A, time(10, 00), time(10, 05), 0.0);
    stopTime(1, fromStop, trip1B, time(10, 10), time(10, 15), 0.0);
    stopTime(2, fromStop, trip1C, time(10, 20), time(10, 25), 0.0);
    stopTime(3, fromStop, trip1D, time(10, 30), time(10, 35), 0.0);

    stopTime(4, toStop, trip1A, time(10, 10), time(10, 15), 0.0);
    stopTime(5, toStop, trip1B, time(10, 20), time(10, 25), 0.0);
    stopTime(6, toStop, trip1C, time(10, 30), time(10, 35), 0.0);
    stopTime(7, toStop, trip1D, time(10, 40), time(10, 45), 0.0);

    BlockConfigurationEntry b1A = linkBlockTrips("b1A", trip1A);
    BlockConfigurationEntry b1B = linkBlockTrips("b1B", trip1B);
    BlockConfigurationEntry b1C = linkBlockTrips("b1C", trip1C);
    BlockConfigurationEntry b1D = linkBlockTrips("b1D", trip1D);

    BlockSequenceIndex index1 = createBlockSequenceIndex(b1A, b1B, b1C, b1D);
    BlockStopSequenceIndex fromIndex1 = new BlockStopSequenceIndex(index1, 0);
    BlockStopSequenceIndex toIndex1 = new BlockStopSequenceIndex(index1, 1);

    Pair<BlockStopSequenceIndex> indexPair1 = Tuples.pair(fromIndex1, toIndex1);

    TripEntryImpl trip2A = trip("2A", "sA");
    TripEntryImpl trip2B = trip("2B", "sA");
    TripEntryImpl trip2C = trip("2C", "sA");
    TripEntryImpl trip2D = trip("2D", "sA");

    stopTime(0, fromStop, trip2A, time(10, 07), time(10, 07), 0.0);
    stopTime(1, fromStop, trip2B, time(10, 17), time(10, 17), 0.0);
    stopTime(2, fromStop, trip2C, time(10, 27), time(10, 27), 0.0);
    stopTime(3, fromStop, trip2D, time(10, 37), time(10, 37), 0.0);

    stopTime(4, toStop, trip2A, time(10, 17), time(10, 17), 0.0);
    stopTime(5, toStop, trip2B, time(10, 27), time(10, 27), 0.0);
    stopTime(6, toStop, trip2C, time(10, 37), time(10, 37), 0.0);
    stopTime(7, toStop, trip2D, time(10, 47), time(10, 47), 0.0);

    BlockConfigurationEntry b2A = linkBlockTrips("bA", trip2A);
    BlockConfigurationEntry b2B = linkBlockTrips("bB", trip2B);
    BlockConfigurationEntry b2C = linkBlockTrips("bC", trip2C);
    BlockConfigurationEntry b2D = linkBlockTrips("bD", trip2D);

    BlockSequenceIndex index2 = createBlockSequenceIndex(b2A, b2B, b2C, b2D);
    BlockStopSequenceIndex fromIndex2 = new BlockStopSequenceIndex(index2, 0);
    BlockStopSequenceIndex toIndex2 = new BlockStopSequenceIndex(index2, 1);

    Pair<BlockStopSequenceIndex> indexPair2 = Tuples.pair(fromIndex2, toIndex2);

    List<Pair<BlockStopSequenceIndex>> indices = new ArrayList<Pair<BlockStopSequenceIndex>>();
    indices.add(indexPair1);
    indices.add(indexPair2);

    Mockito.when(
        _blockIndexService.getBlockSequenceIndicesBetweenStops(fromStop, toStop)).thenReturn(
        indices);

    TripEntryImpl tripF = trip("freq", "sA");

    stopTime(0, fromStop, tripF, time(10, 00), time(10, 00), 0.0);
    stopTime(4, toStop, tripF, time(10, 05), time(10, 05), 0.0);

    FrequencyEntry frequency = new FrequencyEntryImpl(time(10, 30),
        time(11, 30), 10 * 60);
    List<FrequencyEntry> frequencies = Arrays.asList(frequency);

    BlockConfigurationEntry bcFreq = linkBlockTrips(block("bFreq"),
        frequencies, tripF);

    FrequencyBlockTripIndex freqIndex = _factory.createFrequencyIndexForTrips(
        bcFreq.getTrips(), frequencies);
    FrequencyStopTripIndex freqFromIndex = new FrequencyStopTripIndex(
        freqIndex, 0);
    FrequencyStopTripIndex freqToIndex = new FrequencyStopTripIndex(freqIndex,
        1);
    Pair<FrequencyStopTripIndex> freqIndexPair = Tuples.pair(freqFromIndex,
        freqToIndex);

    List<Pair<FrequencyStopTripIndex>> frequencyIndices = new ArrayList<Pair<FrequencyStopTripIndex>>();
    frequencyIndices.add(freqIndexPair);

    Mockito.when(
        _blockIndexService.getFrequencyIndicesBetweenStops(fromStop, toStop)).thenReturn(
        frequencyIndices);

    /****
     * 
     ****/

    Date time = date("2009-09-01 08:00");

    List<Pair<StopTimeInstance>> instances = _service.getNextDeparturesBetweenStopPair(
        fromStop, toStop, time, 0, 0, 3);

    assertEquals(3, instances.size());

    Pair<StopTimeInstance> pair = instances.get(0);
    assertSame(fromStop, pair.getFirst().getStop());
    assertSame(toStop, pair.getSecond().getStop());
    assertEquals(dateAsLong("2009-09-01 00:00"),
        pair.getFirst().getServiceDate());
    assertEquals(b1A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1A.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(1);
    assertEquals(b2A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2A.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(2);
    assertEquals(b1B.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1B.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-01 10:06");

    instances = _service.getNextDeparturesBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());

    pair = instances.get(0);
    assertEquals(b2A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2A.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(1);
    assertEquals(b1B.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1B.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(2);
    assertEquals(b2B.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2B.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-02 10:06");

    instances = _service.getNextDeparturesBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());

    pair = instances.get(0);
    assertEquals(dateAsLong("2009-09-02 00:00"),
        pair.getFirst().getServiceDate());

    assertEquals(b2A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2A.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(1);
    assertEquals(b1B.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1B.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(2);
    assertEquals(b2B.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2B.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-02 10:29");

    instances = _service.getNextDeparturesBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());

    pair = instances.get(0);
    assertEquals(bcFreq.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(bcFreq.getStopTimes().get(1), pair.getSecond().getStopTime());
    assertEquals(dateAsLong("2009-09-02 10:30"),pair.getFirst().getDepartureTime());
    assertEquals(dateAsLong("2009-09-02 10:35"),pair.getSecond().getDepartureTime());

    pair = instances.get(1);
    assertEquals(b1D.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1D.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(2);
    assertEquals(b2D.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2D.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-02 10:40");

    instances = _service.getNextDeparturesBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(1, instances.size());

    pair = instances.get(0);
    assertEquals(bcFreq.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(bcFreq.getStopTimes().get(1), pair.getSecond().getStopTime());
    assertEquals(dateAsLong("2009-09-02 10:40"),pair.getFirst().getDepartureTime());
    assertEquals(dateAsLong("2009-09-02 10:45"),pair.getSecond().getDepartureTime());

    /****
     * 
     ****/

    time = date("2009-09-02 11:40");

    instances = _service.getNextDeparturesBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(0, instances.size());

    /****
     * 
     ****/

    time = date("2009-09-02 10:50");

    instances = _service.getPreviousArrivalsBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());

    pair = instances.get(0);
    assertEquals(b2D.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2D.getStopTimes().get(1), pair.getSecond().getStopTime());
    
    pair = instances.get(1);
    assertEquals(bcFreq.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(bcFreq.getStopTimes().get(1), pair.getSecond().getStopTime());
    assertEquals(dateAsLong("2009-09-02 10:40"),pair.getFirst().getDepartureTime());
    assertEquals(dateAsLong("2009-09-02 10:45"),pair.getSecond().getDepartureTime());

    pair = instances.get(2);
    assertEquals(b1D.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1D.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-02 10:46");

    instances = _service.getPreviousArrivalsBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());
    
    pair = instances.get(0);
    assertEquals(bcFreq.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(bcFreq.getStopTimes().get(1), pair.getSecond().getStopTime());
    assertEquals(dateAsLong("2009-09-02 10:40"),pair.getFirst().getDepartureTime());
    assertEquals(dateAsLong("2009-09-02 10:45"),pair.getSecond().getDepartureTime());

    pair = instances.get(1);
    assertEquals(b1D.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1D.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(2);
    assertEquals(b2C.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2C.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-02 10:40");

    instances = _service.getPreviousArrivalsBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(3, instances.size());

    pair = instances.get(0);
    assertEquals(b2C.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2C.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(1);
    assertEquals(bcFreq.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(bcFreq.getStopTimes().get(1), pair.getSecond().getStopTime());
    assertEquals(dateAsLong("2009-09-02 10:30"),pair.getFirst().getDepartureTime());
    assertEquals(dateAsLong("2009-09-02 10:35"),pair.getSecond().getDepartureTime());

    pair = instances.get(2);
    assertEquals(b1C.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1C.getStopTimes().get(1), pair.getSecond().getStopTime());

    /****
     * 
     ****/

    time = date("2009-09-01 10:18");

    instances = _service.getPreviousArrivalsBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(2, instances.size());

    pair = instances.get(0);
    assertEquals(b2A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b2A.getStopTimes().get(1), pair.getSecond().getStopTime());

    pair = instances.get(1);
    assertEquals(b1A.getStopTimes().get(0), pair.getFirst().getStopTime());
    assertEquals(b1A.getStopTimes().get(1), pair.getSecond().getStopTime());
    
    /****
     * 
     ****/

    time = date("2009-09-01 09:00");

    instances = _service.getPreviousArrivalsBetweenStopPair(fromStop, toStop,
        time, 0, 0, 3);

    assertEquals(0, instances.size());
  }

  /****
   * Private Methods
   ****/

  private void addFirstStopToBlockIndex(BlockConfigurationEntry... blocks) {

    List<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (BlockConfigurationEntry blockConfig : blocks) {
      trips.add(blockConfig.getTrips().get(0));
    }

    BlockIndexFactory factory = new BlockIndexFactory();
    BlockTripIndex blockIndex = factory.createTripIndexForGroupOfBlockTrips(trips);
    BlockStopTimeIndex index = BlockStopTimeIndex.create(blockIndex, 0);
    _stop.addStopTimeIndex(index);

    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(_stop)).thenReturn(
        _stop.getStopTimeIndices());
  }

  private BlockSequenceIndex createBlockSequenceIndex(
      BlockConfigurationEntry... blocks) {

    List<BlockStopTimeEntry> stopTimes = blocks[0].getStopTimes();
    List<BlockSequence> sequences = new ArrayList<BlockSequence>();
    for (BlockConfigurationEntry blockConfig : blocks) {
      BlockSequence sequence = new BlockSequence(blockConfig, 0,
          stopTimes.size());
      sequences.add(sequence);
    }

    return _factory.createSequenceIndexForGroupOfBlockSequences(sequences);
  }

  private void sort(List<StopTimeInstance> stopTimes) {
    Collections.sort(stopTimes, new StopTimeInstanceComparator());
  }

  private static class StopTimeInstanceComparator implements
      Comparator<StopTimeInstance> {
    public int compare(StopTimeInstance o1, StopTimeInstance o2) {
      return (int) (o1.getArrivalTime() - o2.getArrivalTime());
    }
  }
}
