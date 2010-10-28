package org.onebusaway.transit_data_federation.impl.blocks;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockIndices;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.findBlockConfig;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.transit_data_federation.impl.ExtendedCalendarServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class BlockCalendarServiceImplTest {

  private BlockCalendarServiceImpl _service;

  private CalendarServiceImpl _calendarService;

  private CalendarServiceData _calendarData;

  private ExtendedCalendarServiceImpl _extendedCalendarService;

  @Before
  public void before() {
    _service = new BlockCalendarServiceImpl();

    _calendarData = new CalendarServiceData();

    _calendarService = new CalendarServiceImpl();
    _calendarService.setData(_calendarData);

    _extendedCalendarService = new ExtendedCalendarServiceImpl();
    _extendedCalendarService.setCalendarService(_calendarService);

    _service.setCalendarService(_extendedCalendarService);
  }

  @Test
  public void testGetActiveBlocksInTimeRange() {

    Date serviceDateA = UnitTestingSupport.date("2010-09-07 00:00");
    Date serviceDateB = UnitTestingSupport.date("2010-09-08 00:00");
    Date serviceDateC = UnitTestingSupport.date("2010-09-09 00:00");

    UnitTestingSupport.addDates(_calendarData, "sidA", serviceDateA,
        serviceDateB);
    UnitTestingSupport.addDates(_calendarData, "sidB", serviceDateB,
        serviceDateC);

    StopEntryImpl stopA = stop("stopA", 0.0, 0.0);
    StopEntryImpl stopB = stop("stopB", 0.0, 0.0);

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "sidA");
    TripEntryImpl tripB = trip("tripB", "sidB");

    stopTime(0, stopA, tripA, time(9, 00), time(9, 00), 0);
    stopTime(1, stopB, tripA, time(9, 30), time(9, 30), 100);
    stopTime(2, stopB, tripB, time(10, 00), time(10, 00), 200);
    stopTime(3, stopA, tripB, time(10, 30), time(10, 30), 300);

    linkBlockTrips(_calendarService, blockA, tripA, tripB);

    BlockConfigurationEntry bcA_A_B = findBlockConfig(blockA,
        serviceIds(lsids("sidA"), lsids("sidB")));
    BlockConfigurationEntry bcA_B_A = findBlockConfig(blockA,
        serviceIds(lsids("sidB"), lsids("sidA")));
    BlockConfigurationEntry bcA_AB = findBlockConfig(blockA,
        serviceIds(lsids("sidA", "sidB"), lsids()));

    BlockEntryImpl blockB = block("blockB");
    TripEntryImpl tripC = trip("tripC", "sidA");
    TripEntryImpl tripD = trip("tripD", "sidB");
    TripEntryImpl tripE = trip("tripE", "sidA");

    stopTime(4, stopA, tripC, time(10, 00), time(10, 00), 0);
    stopTime(5, stopB, tripC, time(10, 30), time(10, 30), 0);
    stopTime(6, stopB, tripD, time(11, 00), time(11, 00), 0);
    stopTime(7, stopA, tripD, time(11, 30), time(11, 30), 0);
    stopTime(8, stopA, tripE, time(12, 00), time(12, 00), 0);
    stopTime(9, stopB, tripE, time(12, 30), time(12, 30), 0);

    linkBlockTrips(_calendarService, blockB, tripC, tripD, tripE);

    BlockConfigurationEntry bcB_A_B = findBlockConfig(blockB,
        serviceIds(lsids("sidA"), lsids("sidB")));
    BlockConfigurationEntry bcB_B_A = findBlockConfig(blockB,
        serviceIds(lsids("sidB"), lsids("sidA")));
    BlockConfigurationEntry bcB_AB = findBlockConfig(blockB,
        serviceIds(lsids("sidA", "sidB"), lsids()));

    List<BlockIndex> blocks = blockIndices(blockA, blockB);

    /****
     * 
     ****/

    long time = timeFromString("2010-09-07 09:15");

    List<BlockInstance> instances = _service.getActiveBlocksInTimeRange(blocks,
        time, time);

    assertEquals(1, instances.size());

    BlockInstance instance = instances.get(0);
    assertEquals(bcA_A_B, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 010:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_A_B, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 011:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_A_B, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 012:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_A_B, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 09:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcA_AB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 10:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(2, instances.size());

    instance = instances.get(0);
    assertEquals(bcA_AB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());

    instance = instances.get(1);
    assertEquals(bcB_AB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 11:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_AB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 12:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_AB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-09 09:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(0, instances.size());

    /****
     * 
     ****/

    time = timeFromString("2010-09-09 10:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcA_B_A, instance.getBlock());
    assertEquals(serviceDateC.getTime(), instance.getServiceDate());

    /****
     * 
     ****/

    time = timeFromString("2010-09-09 11:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(bcB_B_A, instance.getBlock());
    assertEquals(serviceDateC.getTime(), instance.getServiceDate());
  }

  private static long timeFromString(String source) {
    return UnitTestingSupport.date(source).getTime();
  }
}
