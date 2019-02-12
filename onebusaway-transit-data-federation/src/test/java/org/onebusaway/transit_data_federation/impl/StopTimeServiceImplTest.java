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
package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.*;
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

import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexFactoryServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;

public class StopTimeServiceImplTest {

  private BlockIndexFactoryServiceImpl _factory;

  private StopTimeServiceImpl _service;

  private ExtendedCalendarServiceImpl _calendarService;

  private BlockIndexService _blockIndexService;

  private AgencyAndId _stopId;

  private StopEntryImpl _stop;

  private TransitGraphDao _transitGraphDao;

  private boolean includePrivateService = false;

  @Before
  public void setup() {

    _factory = new BlockIndexFactoryServiceImpl();

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

    StopTimeEntryImpl stA = stopTime(0, _stop, trip("A", "sA"), time(10, 00),
        time(10, 30), 0, -1, 20.0);
    StopTimeEntryImpl stB = stopTime(1, _stop, trip("B", "sA"), time(25, 0),
        time(25, 30), 0, -1, 40.0);
    StopTimeEntryImpl stC = stopTime(2, _stop, trip("C", "sB"), time(10, 00),
        time(10, 30), 0, -1, 60.0);
    StopTimeEntryImpl stD = stopTime(3, _stop, trip("D", "sB"), time(25, 0),
        time(25, 30), 0, -1, 80.0);


    assertNotNull(stA.getHistoricalOccupancy());
    assertNotNull(stB.getHistoricalOccupancy());
    assertNotNull(stC.getHistoricalOccupancy());
    assertNotNull(stD.getHistoricalOccupancy());

    assertEquals( OccupancyStatus.MANY_SEATS_AVAILABLE, stA.getHistoricalOccupancy());
    assertEquals( OccupancyStatus.FEW_SEATS_AVAILABLE, stB.getHistoricalOccupancy());
    assertEquals( OccupancyStatus.STANDING_ROOM_ONLY, stC.getHistoricalOccupancy());
    assertEquals( OccupancyStatus.CRUSHED_STANDING_ROOM_ONLY, stD.getHistoricalOccupancy());

    assertEquals( OccupancyStatus.MANY_SEATS_AVAILABLE, stA.getTrip().getStopTimes().get(0).getHistoricalOccupancy());



  }

  /****
   * Private Methods
   ****/

  private void addFirstStopToBlockIndex(BlockConfigurationEntry... blocks) {

    List<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (BlockConfigurationEntry blockConfig : blocks) {
      trips.add(blockConfig.getTrips().get(0));
    }

    BlockTripIndex blockIndex = _factory.createTripIndexForGroupOfBlockTrips(trips);
    BlockStopTimeIndex index = BlockStopTimeIndex.create(blockIndex, 0);
    _stop.addStopTimeIndex(index);

    Mockito.when(_blockIndexService.getStopTimeIndicesForStop(_stop)).thenReturn(
        _stop.getStopTimeIndices());
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
