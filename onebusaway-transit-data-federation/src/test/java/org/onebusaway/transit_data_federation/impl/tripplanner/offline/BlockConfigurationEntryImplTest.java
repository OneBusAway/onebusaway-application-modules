package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockConfigurationEntryImpl.Builder;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class BlockConfigurationEntryImplTest {

  @Test
  public void test() {

    
    ServiceIdActivation serviceIds = serviceIds(lsids("sA"),lsids("sB"));

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.1, -122.0);

    BlockEntryImpl block = block("blockA");

    TripEntryImpl tripA = trip("tripA", 1000);
    TripEntryImpl tripB = trip("tripB", 2000);
    TripEntryImpl tripC = trip("tripB", 1500);

    List<TripEntry> trips = Arrays.asList((TripEntry) tripA, tripB, tripC);

    StopTimeEntryImpl st1 = stopTime(1, stopA, tripA, time(6, 30), time(6, 35),
        200);
    StopTimeEntryImpl st2 = stopTime(2, stopB, tripA, time(7, 00), time(7, 10),
        800);

    StopTimeEntryImpl st3 = stopTime(3, stopB, tripB, time(7, 30), time(7, 35),
        400);
    StopTimeEntryImpl st4 = stopTime(4, stopA, tripB, time(8, 00), time(8, 07),
        1600);

    StopTimeEntryImpl st5 = stopTime(5, stopA, tripC, time(8, 30), time(8, 35),
        300);
    StopTimeEntryImpl st6 = stopTime(6, stopB, tripC, time(9, 00), time(9, 02),
        1200);

    Builder builder = BlockConfigurationEntryImpl.builder();
    builder.setBlock(block);
    builder.setTrips(trips);
    builder.setServiceIds(serviceIds);
    BlockConfigurationEntry entry = builder.create();

    assertSame(block, entry.getBlock());
    assertSame(serviceIds, entry.getServiceIds());
    
    assertEquals(4500.0, entry.getTotalBlockDistance(), 0.0);

    List<BlockTripEntry> blockTrips = entry.getTrips();
    assertEquals(3, blockTrips.size());

    BlockTripEntry blockTrip = blockTrips.get(0);
    assertEquals(0, blockTrip.getAccumulatedStopTimeIndex());
    assertEquals(0, blockTrip.getAccumulatedSlackTime());
    assertEquals(0.0, blockTrip.getDistanceAlongBlock(), 0.0);
    assertSame(blockTrips.get(1), blockTrip.getNextTrip());
    assertNull(blockTrip.getPreviousTrip());

    blockTrip = blockTrips.get(1);
    assertEquals(2, blockTrip.getAccumulatedStopTimeIndex());
    assertEquals(15 * 60, blockTrip.getAccumulatedSlackTime());
    assertEquals(1000.0, blockTrip.getDistanceAlongBlock(), 0.0);
    assertSame(blockTrips.get(2), blockTrip.getNextTrip());
    assertSame(blockTrips.get(0), blockTrip.getPreviousTrip());

    blockTrip = blockTrips.get(2);
    assertEquals(4, blockTrip.getAccumulatedStopTimeIndex());
    assertEquals(27 * 60, blockTrip.getAccumulatedSlackTime());
    assertEquals(3000.0, blockTrip.getDistanceAlongBlock(), 0.0);
    assertNull(blockTrip.getNextTrip());
    assertSame(blockTrips.get(1), blockTrip.getPreviousTrip());

    List<BlockStopTimeEntry> stopTimes = entry.getStopTimes();
    assertEquals(6, stopTimes.size());

    BlockStopTimeEntry bst = stopTimes.get(0);
    assertEquals(0, bst.getAccumulatedSlackTime());
    assertEquals(0, bst.getBlockSequence());
    assertEquals(200, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st1, bst.getStopTime());
    assertSame(blockTrips.get(0), bst.getTrip());

    bst = stopTimes.get(1);
    assertEquals(300, bst.getAccumulatedSlackTime());
    assertEquals(1, bst.getBlockSequence());
    assertEquals(800, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st2, bst.getStopTime());
    assertSame(blockTrips.get(0), bst.getTrip());

    bst = stopTimes.get(2);
    assertEquals(15 * 60, bst.getAccumulatedSlackTime());
    assertEquals(2, bst.getBlockSequence());
    assertEquals(1400, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st3, bst.getStopTime());
    assertSame(blockTrips.get(1), bst.getTrip());

    bst = stopTimes.get(3);
    assertEquals(20 * 60, bst.getAccumulatedSlackTime());
    assertEquals(3, bst.getBlockSequence());
    assertEquals(2600, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st4, bst.getStopTime());
    assertSame(blockTrips.get(1), bst.getTrip());

    bst = stopTimes.get(4);
    assertEquals(27 * 60, bst.getAccumulatedSlackTime());
    assertEquals(4, bst.getBlockSequence());
    assertEquals(3300, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st5, bst.getStopTime());
    assertSame(blockTrips.get(2), bst.getTrip());

    bst = stopTimes.get(5);
    assertEquals(32 * 60, bst.getAccumulatedSlackTime());
    assertEquals(5, bst.getBlockSequence());
    assertEquals(4200, bst.getDistaceAlongBlock(), 0.0);
    assertSame(st6, bst.getStopTime());
    assertSame(blockTrips.get(2), bst.getTrip());
  }
}
