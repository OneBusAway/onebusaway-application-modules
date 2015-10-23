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
package org.onebusaway.transit_data_federation.impl.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.frequency;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.HasBlockTrips;
import org.onebusaway.transit_data_federation.services.blocks.LayoverIntervalBlock;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class BlockIndexFactoryServiceImplTest {

  @Test
  public void test() {

    BlockIndexFactoryServiceImpl factory = new BlockIndexFactoryServiceImpl();

    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);
    StopEntryImpl stopC = stop("c", 47.2, -122.2);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("a");

    TripEntryImpl tripA1 = trip("a1", "s1"); // 1
    TripEntryImpl tripA2 = trip("a2", "s1");
    TripEntryImpl tripA3 = trip("a3", "s1");

    stopTime(0, stopA, tripA1, 0, 10, 0);
    stopTime(0, stopB, tripA1, 20, 20, 0);
    stopTime(0, stopC, tripA2, 30, 30, 0);
    stopTime(0, stopA, tripA2, 40, 40, 0);
    stopTime(0, stopA, tripA3, 50, 50, 0);
    stopTime(0, stopB, tripA3, 60, 70, 0);

    linkBlockTrips(blockA, tripA1, tripA2, tripA3);

    /****
     * Block B - Same trip/stop sequence as A
     ****/

    BlockEntryImpl blockB = block("b");

    TripEntryImpl tripB1 = trip("b1", "s1");
    TripEntryImpl tripB2 = trip("b2", "s1");
    TripEntryImpl tripB3 = trip("b3", "s1");

    stopTime(0, stopA, tripB1, 20, 30, 0);
    stopTime(0, stopB, tripB1, 50, 50, 0);
    stopTime(0, stopC, tripB2, 60, 60, 0);
    stopTime(0, stopA, tripB2, 70, 70, 0);
    stopTime(0, stopA, tripB3, 80, 80, 0);
    stopTime(0, stopB, tripB3, 90, 100, 0);

    linkBlockTrips(blockB, tripB1, tripB2, tripB3);

    /****
     * Block C - Same stop sequence, but runs a little bit faster
     ****/

    BlockEntryImpl blockC = block("c");

    TripEntryImpl tripC1 = trip("c1", "s1");
    TripEntryImpl tripC2 = trip("c2", "s1");
    TripEntryImpl tripC3 = trip("c3", "s1");

    stopTime(0, stopA, tripC1, 40, 50, 0);
    stopTime(0, stopB, tripC1, 60, 60, 0);
    stopTime(0, stopC, tripC2, 70, 70, 0);
    stopTime(0, stopA, tripC2, 80, 80, 0);
    stopTime(0, stopA, tripC3, 85, 85, 0);
    stopTime(0, stopB, tripC3, 90, 95, 0);

    linkBlockTrips(blockC, tripC1, tripC2, tripC3);

    /****
     * Block D - Same stop sequence, but with different service id
     ****/

    BlockEntryImpl blockD = block("d");

    TripEntryImpl tripD1 = trip("d1", "s1");
    TripEntryImpl tripD2 = trip("d2", "s1");
    TripEntryImpl tripD3 = trip("d3", "s2");

    stopTime(0, stopA, tripD1, 40, 50, 0);
    stopTime(0, stopB, tripD1, 70, 70, 0);
    stopTime(0, stopC, tripD2, 80, 80, 0);
    stopTime(0, stopA, tripD2, 90, 90, 0);
    stopTime(0, stopA, tripD3, 100, 100, 0);
    stopTime(0, stopB, tripD3, 110, 120, 0);

    linkBlockTrips(blockD, tripD1, tripD2, tripD3);

    /****
     * Block E - One less stop
     ****/

    BlockEntryImpl blockE = block("e");

    TripEntryImpl tripE1 = trip("e1", "s1");
    TripEntryImpl tripE2 = trip("e2", "s1");
    TripEntryImpl tripE3 = trip("e3", "s1");

    stopTime(0, stopA, tripE1, 50, 60, 0);
    stopTime(0, stopB, tripE1, 80, 80, 0);
    stopTime(0, stopC, tripE2, 90, 90, 0);
    stopTime(0, stopA, tripE2, 100, 100, 0);
    stopTime(0, stopA, tripE3, 110, 110, 0);

    linkBlockTrips(blockE, tripE1, tripE2, tripE3);

    /****
     * Block F - Another to group with E, but earlier
     ****/

    BlockEntryImpl blockF = block("f");

    TripEntryImpl tripF1 = trip("f1", "s1");
    TripEntryImpl tripF2 = trip("f2", "s1");
    TripEntryImpl tripF3 = trip("f3", "s1");

    stopTime(0, stopA, tripF1, 40, 50, 0);
    stopTime(0, stopB, tripF1, 70, 70, 0);
    stopTime(0, stopC, tripF2, 80, 80, 0);
    stopTime(0, stopA, tripF2, 90, 90, 0);
    stopTime(0, stopA, tripF3, 100, 100, 0);

    linkBlockTrips(blockF, tripF1, tripF2, tripF3);

    List<BlockTripIndex> allIndices = factory.createTripIndices(Arrays.asList(
        (BlockEntry) blockF, blockE, blockD, blockC, blockB, blockA));

    assertEquals(6, allIndices.size());

    List<BlockTripIndex> indices = grep(allIndices, aid("a1"));
    assertEquals(1, indices.size());
    BlockTripIndex index = indices.get(0);
    List<TripEntry> trips = trips(index.getTrips());
    assertEquals(5, trips.size());
    assertEquals(tripA1, trips.get(0));
    assertEquals(tripB1, trips.get(1));
    assertEquals(tripF1, trips.get(2));
    assertEquals(tripE1, trips.get(3));
    assertEquals(tripB3, trips.get(4));
    ServiceIdActivation serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    ServiceIntervalBlock intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {0, 20, 40, 50, 80},
        intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {10, 30, 50, 60, 80},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {20, 50, 70, 80, 90},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {20, 50, 70, 80, 100},
        intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("a2"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    trips = trips(index.getTrips());
    assertEquals(5, trips.size());
    assertEquals(tripA2, trips.get(0));
    assertEquals(tripB2, trips.get(1));
    assertEquals(tripC2, trips.get(2));
    assertEquals(tripF2, trips.get(3));
    assertEquals(tripE2, trips.get(4));
    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {30, 60, 70, 80, 90},
        intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {30, 60, 70, 80, 90},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {40, 70, 80, 90, 100},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {40, 70, 80, 90, 100},
        intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("c1"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    trips = trips(index.getTrips());
    assertEquals(3, trips.size());
    assertEquals(tripC1, trips.get(0));
    assertEquals(tripA3, trips.get(1));
    assertEquals(tripC3, trips.get(2));
    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40, 50, 85},
        intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50, 50, 85},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {60, 60, 90},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {60, 70, 95},
        intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("d1"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    trips = trips(index.getTrips());
    assertEquals(2, trips.size());
    assertEquals(tripD1, trips.get(0));
    assertEquals(tripD3, trips.get(1));
    serviceIds = index.getServiceIds();
    assertEquals(2, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s2")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40, 100},
        intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {50, 100},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {70, 110},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {70, 120},
        intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("d2"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    trips = trips(index.getTrips());
    assertEquals(1, trips.size());
    assertEquals(tripD2, trips.get(0));
    serviceIds = index.getServiceIds();
    assertEquals(2, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s2")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {80}, intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {80}, intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {90}, intervalBlock.getMaxDepartures()));

    indices = grep(allIndices, aid("e3"));
    assertEquals(1, indices.size());
    index = indices.get(0);
    trips = trips(index.getTrips());
    assertEquals(2, trips.size());
    assertEquals(tripF3, trips.get(0));
    assertEquals(tripE3, trips.get(1));
    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMinArrivals()));
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMinDepartures()));
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMaxArrivals()));
    assertTrue(Arrays.equals(new int[] {100, 110},
        intervalBlock.getMaxDepartures()));

    /****
     * Test Layover Indices
     ****/

    List<BlockLayoverIndex> allLayoverIndices = factory.createLayoverIndices(Arrays.asList(
        (BlockEntry) blockF, blockE, blockD, blockC, blockB, blockA));

    List<BlockLayoverIndex> layoverIndices = grep(allLayoverIndices, aid("a2"));
    assertEquals(1, layoverIndices.size());
    BlockLayoverIndex layoverIndiex = layoverIndices.get(0);
    trips = trips(layoverIndiex.getTrips());
    assertEquals(5, trips.size());
    assertEquals(tripA2, trips.get(0));
    assertEquals(tripB2, trips.get(1));
    assertEquals(tripC2, trips.get(2));
    assertEquals(tripF2, trips.get(3));
    assertEquals(tripE2, trips.get(4));
    serviceIds = layoverIndiex.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    LayoverIntervalBlock layoverIntervalBlock = layoverIndiex.getLayoverIntervalBlock();
    assertTrue(Arrays.equals(new int[] {20, 50, 60, 70, 80},
        layoverIntervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {30, 60, 70, 80, 90},
        layoverIntervalBlock.getEndTimes()));

    layoverIndices = grep(allLayoverIndices, aid("a3"));
    assertEquals(1, layoverIndices.size());
    layoverIndiex = layoverIndices.get(0);
    trips = trips(layoverIndiex.getTrips());
    assertEquals(5, trips.size());
    assertEquals(tripA3, trips.get(0));
    assertEquals(tripB3, trips.get(1));
    assertEquals(tripC3, trips.get(2));
    assertEquals(tripF3, trips.get(3));
    assertEquals(tripE3, trips.get(4));
    serviceIds = layoverIndiex.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    layoverIntervalBlock = layoverIndiex.getLayoverIntervalBlock();
    assertTrue(Arrays.equals(new int[] {40, 70, 80, 90, 100},
        layoverIntervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {50, 80, 85, 100, 110},
        layoverIntervalBlock.getEndTimes()));

    layoverIndices = grep(allLayoverIndices, aid("d2"));
    assertEquals(1, layoverIndices.size());
    layoverIndiex = layoverIndices.get(0);
    trips = trips(layoverIndiex.getTrips());
    assertEquals(1, trips.size());
    assertEquals(tripD2, trips.get(0));
    serviceIds = layoverIndiex.getServiceIds();
    assertEquals(2, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s2")));
    layoverIntervalBlock = layoverIndiex.getLayoverIntervalBlock();
    assertTrue(Arrays.equals(new int[] {70},
        layoverIntervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {80}, layoverIntervalBlock.getEndTimes()));

    layoverIndices = grep(allLayoverIndices, aid("d3"));
    assertEquals(1, layoverIndices.size());
    layoverIndiex = layoverIndices.get(0);
    trips = trips(layoverIndiex.getTrips());
    assertEquals(1, trips.size());
    assertEquals(tripD3, trips.get(0));
    serviceIds = layoverIndiex.getServiceIds();
    assertEquals(2, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s2")));
    layoverIntervalBlock = layoverIndiex.getLayoverIntervalBlock();
    assertTrue(Arrays.equals(new int[] {90},
        layoverIntervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {100},
        layoverIntervalBlock.getEndTimes()));
  }

  private List<TripEntry> trips(List<BlockTripEntry> trips) {
    List<TripEntry> tes = new ArrayList<TripEntry>();
    for (BlockTripEntry blockTrip : trips)
      tes.add(blockTrip.getTrip());
    return tes;
  }

  @Test
  public void testFrequencies() {

    BlockIndexFactoryServiceImpl factory = new BlockIndexFactoryServiceImpl();

    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("a");

    TripEntryImpl tripA = trip("a", "s1");

    stopTime(0, stopA, tripA, 0, 10, 0);
    stopTime(0, stopB, tripA, 20, 20, 0);

    FrequencyEntry freqA1 = frequency(time(6, 00), time(9, 00), 10, 0);
    FrequencyEntry freqA2 = frequency(time(15, 00), time(18, 00), 10, 0);
    List<FrequencyEntry> freqsA = Arrays.asList(freqA1, freqA2);

    linkBlockTrips(blockA, freqsA, tripA);

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("b");

    TripEntryImpl tripB = trip("b", "s1");

    stopTime(0, stopA, tripB, 20, 30, 0);
    stopTime(0, stopB, tripB, 50, 50, 0);

    FrequencyEntry freqB1 = frequency(time(9, 00), time(15, 00), 20, 0);
    FrequencyEntry freqB2 = frequency(time(18, 00), time(21, 00), 20, 0);
    List<FrequencyEntry> freqsB = Arrays.asList(freqB1, freqB2);

    linkBlockTrips(blockB, freqsB, tripB);

    List<FrequencyBlockTripIndex> allIndices = factory.createFrequencyTripIndices(Arrays.asList(
        (BlockEntry) blockB, blockA));

    assertEquals(1, allIndices.size());

    List<FrequencyBlockTripIndex> indices = grep(allIndices, aid("a"));
    assertEquals(1, indices.size());

    FrequencyBlockTripIndex index = indices.get(0);

    List<TripEntry> trips = trips(index.getTrips());
    assertEquals(4, trips.size());
    assertEquals(tripA, trips.get(0));
    assertEquals(tripB, trips.get(1));
    assertEquals(tripA, trips.get(2));
    assertEquals(tripB, trips.get(3));

    List<FrequencyEntry> freqs = index.getFrequencies();
    assertEquals(Arrays.asList(freqA1, freqB1, freqA2, freqB2), freqs);

    ServiceIdActivation serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));

    FrequencyServiceIntervalBlock intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {
        time(6, 0), time(9, 0), time(15, 0), time(18, 0)},
        intervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {
        time(9, 0), time(15, 0), time(18, 0), time(21, 0)},
        intervalBlock.getEndTimes()));
  }

  @Test
  public void testOverlappingFrequencies() {

    BlockIndexFactoryServiceImpl factory = new BlockIndexFactoryServiceImpl();

    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("a");

    TripEntryImpl tripA = trip("a", "s1");

    stopTime(0, stopA, tripA, 0, 10, 0);
    stopTime(0, stopB, tripA, 20, 20, 0);

    FrequencyEntry freqA1 = frequency(time(6, 00), time(9, 00), 10, 0);
    FrequencyEntry freqA2 = frequency(time(15, 00), time(18, 00), 10, 0);
    List<FrequencyEntry> freqsA = Arrays.asList(freqA1, freqA2);

    linkBlockTrips(blockA, freqsA, tripA);

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("b");

    TripEntryImpl tripB = trip("b", "s1");

    stopTime(0, stopA, tripB, 20, 30, 0);
    stopTime(0, stopB, tripB, 50, 50, 0);

    FrequencyEntry freqB1 = frequency(time(8, 00), time(14, 00), 20, 0);
    FrequencyEntry freqB2 = frequency(time(17, 00), time(20, 00), 20, 0);
    List<FrequencyEntry> freqsB = Arrays.asList(freqB1, freqB2);

    linkBlockTrips(blockB, freqsB, tripB);

    List<FrequencyBlockTripIndex> allIndices = factory.createFrequencyTripIndices(Arrays.asList(
        (BlockEntry) blockB, blockA));

    assertEquals(2, allIndices.size());

    List<FrequencyBlockTripIndex> indices = grep(allIndices, aid("a"));
    assertEquals(1, indices.size());

    FrequencyBlockTripIndex index = indices.get(0);

    List<TripEntry> trips = trips(index.getTrips());
    assertEquals(2, trips.size());
    assertEquals(tripA, trips.get(0));
    assertEquals(tripA, trips.get(1));

    List<FrequencyEntry> freqs = index.getFrequencies();
    assertEquals(Arrays.asList(freqA1, freqA2), freqs);

    ServiceIdActivation serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));

    FrequencyServiceIntervalBlock intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {time(6, 0), time(15, 0)},
        intervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {time(9, 0), time(18, 0)},
        intervalBlock.getEndTimes()));

    /****
     * 
     ****/

    indices = grep(allIndices, aid("b"));
    assertEquals(1, indices.size());

    index = indices.get(0);

    trips = trips(index.getTrips());
    assertEquals(2, trips.size());
    assertEquals(tripB, trips.get(0));
    assertEquals(tripB, trips.get(1));

    freqs = index.getFrequencies();
    assertEquals(Arrays.asList(freqB1, freqB2), freqs);

    serviceIds = index.getServiceIds();
    assertEquals(1, serviceIds.getActiveServiceIds().size());
    assertTrue(serviceIds.getActiveServiceIds().contains(lsid("s1")));

    intervalBlock = index.getServiceIntervalBlock();
    assertTrue(Arrays.equals(new int[] {time(8, 0), time(17, 0)},
        intervalBlock.getStartTimes()));
    assertTrue(Arrays.equals(new int[] {time(14, 0), time(20, 0)},
        intervalBlock.getEndTimes()));
  }

  private <T extends HasBlockTrips> List<T> grep(List<T> datas,
      AgencyAndId tripId) {

    List<T> matches = new ArrayList<T>();

    for (T data : datas) {
      for (BlockTripEntry trip : data.getTrips()) {
        if (trip.getTrip().getId().equals(tripId)) {
          matches.add(data);
          break;
        }
      }
    }
    return matches;
  }
}
