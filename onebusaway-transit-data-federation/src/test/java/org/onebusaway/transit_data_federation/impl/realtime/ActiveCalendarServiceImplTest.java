package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
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
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.DateSupport;
import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockIndicesFactory;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.realtime.BlockInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public class ActiveCalendarServiceImplTest {

  private ActiveCalendarServiceImpl _service;

  private CalendarServiceImpl _calendarService;

  private CalendarServiceData _calendarData;

  @Before
  public void before() {
    _service = new ActiveCalendarServiceImpl();

    _calendarService = new CalendarServiceImpl();
    _service.setCalendarService(_calendarService);

    _calendarData = new CalendarServiceData();
    _calendarService.setData(_calendarData);
  }

  @Test
  public void testGetActiveBlocksInTimeRange() {

    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    LocalizedServiceId lsidA = new LocalizedServiceId(aid("sidA"), tz);
    LocalizedServiceId lsidB = new LocalizedServiceId(aid("sidB"), tz);

    Date serviceDateA = DateSupport.date("2010-09-07 00:00");
    Date serviceDateB = DateSupport.date("2010-09-08 00:00");
    Date serviceDateC = DateSupport.date("2010-09-09 00:00");

    _calendarData.putDatesForLocalizedServiceId(lsidA,
        Arrays.asList(serviceDateA, serviceDateB));
    _calendarData.putDatesForLocalizedServiceId(lsidB,
        Arrays.asList(serviceDateB, serviceDateC));
    _calendarData.putTimeZoneForAgencyId("1", tz);
    
    StopEntryImpl stopA = stop("stopA",0.0,0.0);
    StopEntryImpl stopB = stop("stopB",0.0,0.0);

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA");
    TripEntryImpl tripB = trip("tripB");
    tripA.setServiceId(aid("sidA"));
    tripB.setServiceId(aid("sidB"));

    linkBlockTrips(blockA, tripA, tripB);

    StopTimeEntry stA = stopTime(0, stopA, tripA, time(9, 00), time(9, 00), 0);
    StopTimeEntry stB = stopTime(1, stopB, tripA, time(9, 30), time(9, 30), 100);
    StopTimeEntry stC = stopTime(2, stopB, tripB, time(10, 00), time(10, 00),
        200);
    StopTimeEntry stD = stopTime(3, stopA, tripB, time(10, 30), time(10, 30),
        300);

    blockA.setStopTimes(Arrays.asList(stA, stB, stC, stD));
    tripA.setStopTimeIndices(0, 2);
    tripB.setStopTimeIndices(2, 4);

    BlockEntryImpl blockB = block("blockB");
    TripEntryImpl tripC = trip("tripC");
    TripEntryImpl tripD = trip("tripD");
    TripEntryImpl tripE = trip("tripE");

    tripC.setServiceId(aid("sidA"));
    tripD.setServiceId(aid("sidB"));
    tripE.setServiceId(aid("sidA"));

    linkBlockTrips(blockB, tripC, tripD, tripE);

    StopTimeEntry stE = stopTime(4, stopA, tripC, time(10, 00), time(10, 00), 0);
    StopTimeEntry stF = stopTime(5, stopB, tripC, time(10, 30), time(10, 30), 0);
    StopTimeEntry stG = stopTime(6, stopB, tripD, time(11, 00), time(11, 00), 0);
    StopTimeEntry stH = stopTime(7, stopA, tripD, time(11, 30), time(11, 30), 0);
    StopTimeEntry stI = stopTime(8, stopA, tripE, time(12, 00), time(12, 00), 0);
    StopTimeEntry stJ = stopTime(9, stopB, tripE, time(12, 30), time(12, 30), 0);

    blockB.setStopTimes(Arrays.asList(stE, stF, stG, stH, stI, stJ));
    tripC.setStopTimeIndices(0, 2);
    tripD.setStopTimeIndices(2, 4);
    tripE.setStopTimeIndices(4, 6);

    BlockIndicesFactory factory = new BlockIndicesFactory();
    factory.setCalendarService(_calendarService);
    List<BlockIndex> blocks = factory.createIndices(Arrays.asList(
        (BlockEntry) blockA, blockB));

    /****
     * 
     ****/

    long time = timeFromString("2010-09-07 09:15");

    List<BlockInstance> instances = _service.getActiveBlocksInTimeRange(blocks,
        time, time);

    assertEquals(1, instances.size());

    BlockInstance instance = instances.get(0);
    assertEquals(blockA, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());
    Set<LocalizedServiceId> serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 010:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 011:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));

    /****
     * 
     ****/

    time = timeFromString("2010-09-07 012:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateA.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 09:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockA, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));
    assertTrue(serviceIds.contains(lsidB));

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 10:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(2, instances.size());

    instance = instances.get(0);
    assertEquals(blockA, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));
    assertTrue(serviceIds.contains(lsidB));

    instance = instances.get(1);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));
    assertTrue(serviceIds.contains(lsidB));

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 11:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));
    assertTrue(serviceIds.contains(lsidB));

    /****
     * 
     ****/

    time = timeFromString("2010-09-08 12:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateB.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsidA));
    assertTrue(serviceIds.contains(lsidB));

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
    assertEquals(blockA, instance.getBlock());
    assertEquals(serviceDateC.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidB));

    /****
     * 
     ****/

    time = timeFromString("2010-09-09 11:15");

    instances = _service.getActiveBlocksInTimeRange(blocks, time, time);

    assertEquals(1, instances.size());

    instance = instances.get(0);
    assertEquals(blockB, instance.getBlock());
    assertEquals(serviceDateC.getTime(), instance.getServiceDate());
    serviceIds = instance.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsidB));
  }
  
  private static long timeFromString(String source) {
    return DateSupport.date(source).getTime();
  }
}
