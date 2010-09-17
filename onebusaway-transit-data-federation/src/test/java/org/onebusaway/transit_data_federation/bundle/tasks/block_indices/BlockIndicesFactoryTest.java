package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.addStopTime;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.aid;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.block;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.stop;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.stopTime;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public class BlockIndicesFactoryTest {
  @Test
  public void test() {

    BlockIndicesFactory factory = new BlockIndicesFactory();

    CalendarServiceData calendarData = new CalendarServiceData();
    CalendarServiceImpl calendarService = new CalendarServiceImpl();
    calendarService.setData(calendarData);
    factory.setCalendarService(calendarService);

    calendarData.putTimeZoneForAgencyId("1",
        TimeZone.getTimeZone("America/Los_Angeles"));

    LocalizedServiceId lsid1 = calendarService.getLocalizedServiceIdForAgencyAndServiceId(
        "1", aid("s1"));
    LocalizedServiceId lsid2 = calendarService.getLocalizedServiceIdForAgencyAndServiceId(
        "1", aid("s2"));

    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);
    StopEntryImpl stopC = stop("c", 47.2, -122.2);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("a");

    TripEntryImpl tripA1 = trip("a1", "s1");
    TripEntryImpl tripA2 = trip("a2", "s1");
    TripEntryImpl tripA3 = trip("a3", "s1");

    linkBlockTrips(blockA, tripA1, tripA2, tripA3);
    addStopTime(tripA1, stopTime(0, stopA, tripA1, 0, 10, 0));
    addStopTime(tripA1, stopTime(0, stopB, tripA1, 20, 20, 0));
    addStopTime(tripA2, stopTime(0, stopC, tripA2, 30, 30, 0));
    addStopTime(tripA2, stopTime(0, stopA, tripA2, 40, 40, 0));
    addStopTime(tripA3, stopTime(0, stopA, tripA3, 50, 50, 0));
    addStopTime(tripA3, stopTime(0, stopB, tripA3, 60, 70, 0));

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("b");

    TripEntryImpl tripB1 = trip("b1", "s1");
    TripEntryImpl tripB2 = trip("b2", "s1");
    TripEntryImpl tripB3 = trip("b3", "s1");

    linkBlockTrips(blockB, tripB1, tripB2, tripB3);
    addStopTime(tripB1, stopTime(0, stopA, tripB1, 20, 30, 0));
    addStopTime(tripB1, stopTime(0, stopB, tripB1, 50, 50, 0));
    addStopTime(tripB2, stopTime(0, stopC, tripB2, 60, 60, 0));
    addStopTime(tripB2, stopTime(0, stopA, tripB2, 70, 70, 0));
    addStopTime(tripB3, stopTime(0, stopA, tripB3, 80, 80, 0));
    addStopTime(tripB3, stopTime(0, stopB, tripB3, 90, 100, 0));

    /****
     * Block C - Same stop sequence, but runs a little bit faster
     ****/

    BlockEntryImpl blockC = block("c");

    TripEntryImpl tripC1 = trip("c1", "s1");
    TripEntryImpl tripC2 = trip("c2", "s1");
    TripEntryImpl tripC3 = trip("c3", "s1");

    linkBlockTrips(blockC, tripC1, tripC2, tripC3);
    addStopTime(tripC1, stopTime(0, stopA, tripC1, 40, 50, 0));
    addStopTime(tripC1, stopTime(0, stopB, tripC1, 60, 60, 0));
    addStopTime(tripC2, stopTime(0, stopC, tripC2, 70, 70, 0));
    addStopTime(tripC2, stopTime(0, stopA, tripC2, 80, 80, 0));
    addStopTime(tripC3, stopTime(0, stopA, tripC3, 85, 85, 0));
    addStopTime(tripC3, stopTime(0, stopB, tripC3, 90, 95, 0));

    /****
     * Block D - Same stop sequence, but with different service id
     ****/

    BlockEntryImpl blockD = block("d");

    TripEntryImpl tripD1 = trip("d1", "s1");
    TripEntryImpl tripD2 = trip("d2", "s1");
    TripEntryImpl tripD3 = trip("d3", "s2");

    linkBlockTrips(blockD, tripD1, tripD2, tripD3);
    addStopTime(tripD1, stopTime(0, stopA, tripD1, 40, 50, 0));
    addStopTime(tripD1, stopTime(0, stopB, tripD1, 70, 70, 0));
    addStopTime(tripD2, stopTime(0, stopC, tripD2, 80, 80, 0));
    addStopTime(tripD2, stopTime(0, stopA, tripD2, 90, 90, 0));
    addStopTime(tripD3, stopTime(0, stopA, tripD3, 100, 100, 0));
    addStopTime(tripD3, stopTime(0, stopB, tripD3, 110, 120, 0));

    /****
     * Block E - One less stop
     ****/

    BlockEntryImpl blockE = block("e");

    TripEntryImpl tripE1 = trip("e1", "s1");
    TripEntryImpl tripE2 = trip("e2", "s1");
    TripEntryImpl tripE3 = trip("e3", "s1");

    linkBlockTrips(blockE, tripE1, tripE2, tripE3);
    addStopTime(tripE1, stopTime(0, stopA, tripE1, 50, 60, 0));
    addStopTime(tripE1, stopTime(0, stopB, tripE1, 80, 80, 0));
    addStopTime(tripE2, stopTime(0, stopC, tripE2, 90, 90, 0));
    addStopTime(tripE2, stopTime(0, stopA, tripE2, 100, 100, 0));
    addStopTime(tripE3, stopTime(0, stopA, tripE3, 110, 110, 0));

    /****
     * Block F - Another to group with E, but earlier
     ****/

    BlockEntryImpl blockF = block("f");

    TripEntryImpl tripF1 = trip("f1", "s1");
    TripEntryImpl tripF2 = trip("f2", "s1");
    TripEntryImpl tripF3 = trip("ef3", "s1");

    linkBlockTrips(blockF, tripF1, tripF2, tripF3);
    addStopTime(tripF1, stopTime(0, stopA, tripF1, 40, 50, 0));
    addStopTime(tripF1, stopTime(0, stopB, tripF1, 70, 70, 0));
    addStopTime(tripF2, stopTime(0, stopC, tripF2, 80, 80, 0));
    addStopTime(tripF2, stopTime(0, stopA, tripF2, 90, 90, 0));
    addStopTime(tripF3, stopTime(0, stopA, tripF3, 100, 100, 0));

    List<BlockIndexData> allData = factory.createData(Arrays.asList(
        (BlockEntry) blockF, blockE, blockD, blockC, blockB, blockA));

    assertEquals(4, allData.size());

    List<BlockIndexData> datas = grep(allData, aid("a"));
    assertEquals(1, datas.size());
    BlockIndexData data = datas.get(0);
    List<AgencyAndId> blockIds = data.getBlockIds();
    assertEquals(2, blockIds.size());
    assertEquals(aid("a"), blockIds.get(0));
    assertEquals(aid("b"), blockIds.get(1));
    ServiceIdIntervals intervals = data.getServiceIdIntervals();
    Set<LocalizedServiceId> serviceIds = intervals.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsid1));
    ServiceInterval interval = intervals.getIntervalForServiceId(lsid1);
    assertEquals(0, interval.getMinArrival());
    assertEquals(10, interval.getMinDeparture());
    assertEquals(90, interval.getMaxArrival());
    assertEquals(100, interval.getMaxDeparture());
    Map<LocalizedServiceId, ServiceIntervalBlock> inntervalBlocks = data.getIntervalsByServiceId();
    assertEquals(1, inntervalBlocks.size());
    ServiceIntervalBlock intervalBlock = inntervalBlocks.get(lsid1);
    assertTrue(Arrays.equals(new int[] {0, 20}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {10, 30},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {60, 90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {70, 100},
        intervalBlock.getMaxDepartures()));

    datas = grep(allData, aid("c"));
    assertEquals(1, datas.size());
    data = datas.get(0);
    blockIds = data.getBlockIds();
    assertEquals(1, blockIds.size());
    assertEquals(aid("c"), blockIds.get(0));
    intervals = data.getServiceIdIntervals();
    serviceIds = intervals.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsid1));
    interval = intervals.getIntervalForServiceId(lsid1);
    assertEquals(40, interval.getMinArrival());
    assertEquals(50, interval.getMinDeparture());
    assertEquals(90, interval.getMaxArrival());
    assertEquals(95, interval.getMaxDeparture());
    inntervalBlocks = data.getIntervalsByServiceId();
    assertEquals(1, inntervalBlocks.size());
    intervalBlock = inntervalBlocks.get(lsid1);
    assertTrue(Arrays.equals(new int[] {40}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {95}, intervalBlock.getMaxDepartures()));

    datas = grep(allData, aid("d"));
    assertEquals(1, datas.size());
    data = datas.get(0);
    blockIds = data.getBlockIds();
    assertEquals(1, blockIds.size());
    assertEquals(aid("d"), blockIds.get(0));
    intervals = data.getServiceIdIntervals();
    serviceIds = intervals.getServiceIds();
    assertEquals(2, serviceIds.size());
    assertTrue(serviceIds.contains(lsid1));
    assertTrue(serviceIds.contains(lsid2));
    interval = intervals.getIntervalForServiceId(lsid1);
    assertEquals(40, interval.getMinArrival());
    assertEquals(50, interval.getMinDeparture());
    assertEquals(90, interval.getMaxArrival());
    assertEquals(90, interval.getMaxDeparture());
    interval = intervals.getIntervalForServiceId(lsid2);
    assertEquals(100, interval.getMinArrival());
    assertEquals(100, interval.getMinDeparture());
    assertEquals(110, interval.getMaxArrival());
    assertEquals(120, interval.getMaxDeparture());
    inntervalBlocks = data.getIntervalsByServiceId();
    assertEquals(2, inntervalBlocks.size());
    intervalBlock = inntervalBlocks.get(lsid1);
    assertTrue(Arrays.equals(new int[] {40}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxDepartures()));
    intervalBlock = inntervalBlocks.get(lsid2);
    assertTrue(Arrays.equals(new int[] {100}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {100}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {110}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {120}, intervalBlock.getMaxDepartures()));
    
    datas = grep(allData, aid("e"));
    assertEquals(1, datas.size());
    data = datas.get(0);
    blockIds = data.getBlockIds();
    assertEquals(2, blockIds.size());
    assertEquals(aid("f"), blockIds.get(0));
    assertEquals(aid("e"), blockIds.get(1));
    intervals = data.getServiceIdIntervals();
    serviceIds = intervals.getServiceIds();
    assertEquals(1, serviceIds.size());
    assertTrue(serviceIds.contains(lsid1));
    interval = intervals.getIntervalForServiceId(lsid1);
    assertEquals(40, interval.getMinArrival());
    assertEquals(50, interval.getMinDeparture());
    assertEquals(110, interval.getMaxArrival());
    assertEquals(110, interval.getMaxDeparture());
    inntervalBlocks = data.getIntervalsByServiceId();
    assertEquals(1, inntervalBlocks.size());
    intervalBlock = inntervalBlocks.get(lsid1);
    assertTrue(Arrays.equals(new int[] {40,50}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50,60}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {100,110}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {100,110}, intervalBlock.getMaxDepartures()));
  }

  private List<BlockIndexData> grep(List<BlockIndexData> datas,
      AgencyAndId blockId) {
    List<BlockIndexData> matches = new ArrayList<BlockIndexData>();
    for (BlockIndexData data : datas) {
      if (data.getBlockIds().contains(blockId))
        matches.add(data);
    }
    return matches;
  }
}
