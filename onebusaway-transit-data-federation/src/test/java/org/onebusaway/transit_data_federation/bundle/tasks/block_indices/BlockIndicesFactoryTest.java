package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addStopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public class BlockIndicesFactoryTest {
  @Test
  public void test() {

    BlockIndicesFactory factory = new BlockIndicesFactory();

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

    addStopTime(tripA1, stopTime(0, stopA, tripA1, 0, 10, 0));
    addStopTime(tripA1, stopTime(0, stopB, tripA1, 20, 20, 0));
    addStopTime(tripA2, stopTime(0, stopC, tripA2, 30, 30, 0));
    addStopTime(tripA2, stopTime(0, stopA, tripA2, 40, 40, 0));
    addStopTime(tripA3, stopTime(0, stopA, tripA3, 50, 50, 0));
    addStopTime(tripA3, stopTime(0, stopB, tripA3, 60, 70, 0));

    BlockConfigurationEntry bcA = linkBlockTrips(blockA, tripA1, tripA2, tripA3);

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("b");

    TripEntryImpl tripB1 = trip("b1", "s1");
    TripEntryImpl tripB2 = trip("b2", "s1");
    TripEntryImpl tripB3 = trip("b3", "s1");

    addStopTime(tripB1, stopTime(0, stopA, tripB1, 20, 30, 0));
    addStopTime(tripB1, stopTime(0, stopB, tripB1, 50, 50, 0));
    addStopTime(tripB2, stopTime(0, stopC, tripB2, 60, 60, 0));
    addStopTime(tripB2, stopTime(0, stopA, tripB2, 70, 70, 0));
    addStopTime(tripB3, stopTime(0, stopA, tripB3, 80, 80, 0));
    addStopTime(tripB3, stopTime(0, stopB, tripB3, 90, 100, 0));

    BlockConfigurationEntry bcB = linkBlockTrips(blockB, tripB1, tripB2, tripB3);

    /****
     * Block C - Same stop sequence, but runs a little bit faster
     ****/

    BlockEntryImpl blockC = block("c");

    TripEntryImpl tripC1 = trip("c1", "s1");
    TripEntryImpl tripC2 = trip("c2", "s1");
    TripEntryImpl tripC3 = trip("c3", "s1");

    addStopTime(tripC1, stopTime(0, stopA, tripC1, 40, 50, 0));
    addStopTime(tripC1, stopTime(0, stopB, tripC1, 60, 60, 0));
    addStopTime(tripC2, stopTime(0, stopC, tripC2, 70, 70, 0));
    addStopTime(tripC2, stopTime(0, stopA, tripC2, 80, 80, 0));
    addStopTime(tripC3, stopTime(0, stopA, tripC3, 85, 85, 0));
    addStopTime(tripC3, stopTime(0, stopB, tripC3, 90, 95, 0));

    BlockConfigurationEntry bcC = linkBlockTrips(blockC, tripC1, tripC2, tripC3);

    /****
     * Block D - Same stop sequence, but with different service id
     ****/

    BlockEntryImpl blockD = block("d");

    TripEntryImpl tripD1 = trip("d1", "s1");
    TripEntryImpl tripD2 = trip("d2", "s1");
    TripEntryImpl tripD3 = trip("d3", "s2");

    addStopTime(tripD1, stopTime(0, stopA, tripD1, 40, 50, 0));
    addStopTime(tripD1, stopTime(0, stopB, tripD1, 70, 70, 0));
    addStopTime(tripD2, stopTime(0, stopC, tripD2, 80, 80, 0));
    addStopTime(tripD2, stopTime(0, stopA, tripD2, 90, 90, 0));
    addStopTime(tripD3, stopTime(0, stopA, tripD3, 100, 100, 0));
    addStopTime(tripD3, stopTime(0, stopB, tripD3, 110, 120, 0));

    BlockConfigurationEntry bcD = linkBlockTrips(blockD, tripD1, tripD2, tripD3);

    /****
     * Block E - One less stop
     ****/

    BlockEntryImpl blockE = block("e");

    TripEntryImpl tripE1 = trip("e1", "s1");
    TripEntryImpl tripE2 = trip("e2", "s1");
    TripEntryImpl tripE3 = trip("e3", "s1");

    addStopTime(tripE1, stopTime(0, stopA, tripE1, 50, 60, 0));
    addStopTime(tripE1, stopTime(0, stopB, tripE1, 80, 80, 0));
    addStopTime(tripE2, stopTime(0, stopC, tripE2, 90, 90, 0));
    addStopTime(tripE2, stopTime(0, stopA, tripE2, 100, 100, 0));
    addStopTime(tripE3, stopTime(0, stopA, tripE3, 110, 110, 0));

    BlockConfigurationEntry bcE = linkBlockTrips(blockE, tripE1, tripE2, tripE3);

    /****
     * Block F - Another to group with E, but earlier
     ****/

    BlockEntryImpl blockF = block("f");

    TripEntryImpl tripF1 = trip("f1", "s1");
    TripEntryImpl tripF2 = trip("f2", "s1");
    TripEntryImpl tripF3 = trip("ef3", "s1");

    addStopTime(tripF1, stopTime(0, stopA, tripF1, 40, 50, 0));
    addStopTime(tripF1, stopTime(0, stopB, tripF1, 70, 70, 0));
    addStopTime(tripF2, stopTime(0, stopC, tripF2, 80, 80, 0));
    addStopTime(tripF2, stopTime(0, stopA, tripF2, 90, 90, 0));
    addStopTime(tripF3, stopTime(0, stopA, tripF3, 100, 100, 0));

    BlockConfigurationEntry bcF = linkBlockTrips(blockF, tripF1, tripF2, tripF3);

    List<BlockIndex> allIndices = factory.createIndices(Arrays.asList(
        (BlockEntry) blockF, blockE, blockD, blockC, blockB, blockA));

    assertEquals(4, allIndices.size());

    List<BlockIndex> indices = grep(allIndices, aid("a"));
    assertEquals(1, indices.size());
    BlockIndex index = indices.get(0);
    List<BlockConfigurationEntry> configs = index.getBlocks();
    assertEquals(2, configs.size());
    assertEquals(bcA, configs.get(0));
    assertEquals(bcB, configs.get(1));
    ServiceIdActivation serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    ServiceIntervalBlock intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {0, 20}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {10, 30},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {60, 90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {70, 100},
        intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("c"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    configs = index.getBlocks();
    assertEquals(1, configs.size());
    assertEquals(bcC, configs.get(0));
    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {95}, intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("d"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    configs = index.getBlocks();
    assertEquals(1, configs.size());
    assertEquals(bcD, configs.get(0));
    serviceIds = index.getServiceIds();
    assertEquals(2, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s2")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {110}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {120}, intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("e"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    configs = index.getBlocks();
    assertEquals(2, configs.size());
    assertEquals(bcF, configs.get(0));
    assertEquals(bcE, configs.get(1));
    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40, 50}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50, 60},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMaxDepartures()));
  }

  private List<BlockIndex> grep(List<BlockIndex> datas, AgencyAndId blockId) {
    List<BlockIndex> matches = new ArrayList<BlockIndex>();
    for (BlockIndex data : datas) {
      for (BlockConfigurationEntry config : data.getBlocks()) {
        if (config.getBlock().getId().equals(blockId)) {
          matches.add(data);
          break;
        }
      }
    }
    return matches;
  }
}
