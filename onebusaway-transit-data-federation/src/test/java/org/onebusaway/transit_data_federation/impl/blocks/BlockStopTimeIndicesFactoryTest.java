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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public class BlockStopTimeIndicesFactoryTest {

  @Test
  public void test() {

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

    BlockConfigurationEntry bcA = linkBlockTrips(blockA, tripA1, tripA2, tripA3);

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

    BlockConfigurationEntry bcB = linkBlockTrips(blockB, tripB1, tripB2, tripB3);

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

    BlockConfigurationEntry bcC = linkBlockTrips(blockC, tripC1, tripC2, tripC3);

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

    BlockConfigurationEntry bcD = linkBlockTrips(blockD, tripD1, tripD2, tripD3);

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

    BlockConfigurationEntry bcE = linkBlockTrips(blockE, tripE1, tripE2, tripE3);

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

    BlockConfigurationEntry bcF = linkBlockTrips(blockF, tripF1, tripF2, tripF3);


    /**
     *  Block G - Copy of A, but with Historical Occupancy
     */
    BlockEntryImpl blockG = block("g");

    TripEntryImpl tripG1 = trip("g1", "s1");
    TripEntryImpl tripG2 = trip("g2", "s1");
    TripEntryImpl tripG3 = trip("g3", "s1");

    stopTime(0, stopA, tripG1,  0, 10, 0, 0.0);
    stopTime(0, stopB, tripG1, 20, 20, 0, 25.0);
    stopTime(0, stopC, tripG2, 30, 30, 0, 50.0);
    stopTime(0, stopA, tripG2, 40, 40, 0, 75.0);
    stopTime(0, stopA, tripG3, 50, 50, 0, 90.0);
    stopTime(0, stopA, tripG3, 50, 50, 0, 99.0);
    stopTime(0, stopB, tripG3, 60, 70, 0, 100.0);

    BlockConfigurationEntry bcG = linkBlockTrips(blockG, tripG1, tripG2, tripG3);




    BlockStopTimeIndicesFactory factory = new BlockStopTimeIndicesFactory();

    List<BlockStopTimeIndex> allIndices = factory.createIndices(Arrays.asList(
        (BlockEntry) blockF, blockE, blockD, blockC, blockB, blockA));

    assertEquals(6, allIndices.size());

    List<BlockStopTimeIndex> indices = grep(allIndices, aid("a"),
        serviceIds(lsids("s1"), lsids()));
    assertEquals(1, indices.size());
    BlockStopTimeIndex index = indices.get(0);
    assertEquals(15, index.getStopTimes().size());
    assertEquals(bcA.getStopTimes().get(0), index.getStopTimes().get(0));
    assertEquals(bcB.getStopTimes().get(0), index.getStopTimes().get(1));
    assertEquals(bcA.getStopTimes().get(3), index.getStopTimes().get(2));
    assertEquals(bcF.getStopTimes().get(0), index.getStopTimes().get(3));
    assertEquals(bcC.getStopTimes().get(0), index.getStopTimes().get(4));
    assertEquals(bcA.getStopTimes().get(4), index.getStopTimes().get(5));
    assertEquals(bcE.getStopTimes().get(0), index.getStopTimes().get(6));
    assertEquals(bcB.getStopTimes().get(3), index.getStopTimes().get(7));
    assertEquals(bcC.getStopTimes().get(3), index.getStopTimes().get(8));
    assertEquals(bcB.getStopTimes().get(4), index.getStopTimes().get(9));
    assertEquals(bcC.getStopTimes().get(4), index.getStopTimes().get(10));
    assertEquals(bcF.getStopTimes().get(3), index.getStopTimes().get(11));
    assertEquals(bcF.getStopTimes().get(4), index.getStopTimes().get(12));
    assertEquals(bcE.getStopTimes().get(3), index.getStopTimes().get(13));
    assertEquals(bcE.getStopTimes().get(4), index.getStopTimes().get(14));

    indices = grep(allIndices, aid("a"), serviceIds(lsids("s1", "s2"), lsids()));
    assertEquals(1, indices.size());
    index = indices.get(0);
    assertEquals(3, index.getStopTimes().size());
    assertEquals(bcD.getStopTimes().get(0), index.getStopTimes().get(0));
    assertEquals(bcD.getStopTimes().get(3), index.getStopTimes().get(1));
    assertEquals(bcD.getStopTimes().get(4), index.getStopTimes().get(2));

    indices = grep(allIndices, aid("b"), serviceIds(lsids("s1"), lsids()));
    assertEquals(1, indices.size());
    index = indices.get(0);
    assertEquals(8, index.getStopTimes().size());
    assertEquals(bcA.getStopTimes().get(1), index.getStopTimes().get(0));
    assertEquals(bcB.getStopTimes().get(1), index.getStopTimes().get(1));
    assertEquals(bcC.getStopTimes().get(1), index.getStopTimes().get(2));
    assertEquals(bcA.getStopTimes().get(5), index.getStopTimes().get(3));
    assertEquals(bcF.getStopTimes().get(1), index.getStopTimes().get(4));
    assertEquals(bcE.getStopTimes().get(1), index.getStopTimes().get(5));
    assertEquals(bcC.getStopTimes().get(5), index.getStopTimes().get(6));
    assertEquals(bcB.getStopTimes().get(5), index.getStopTimes().get(7));

    indices = grep(allIndices, aid("b"), serviceIds(lsids("s1", "s2"), lsids()));
    assertEquals(1, indices.size());
    index = indices.get(0);
    assertEquals(2, index.getStopTimes().size());
    assertEquals(bcD.getStopTimes().get(1), index.getStopTimes().get(0));
    assertEquals(bcD.getStopTimes().get(5), index.getStopTimes().get(1));

    indices = grep(allIndices, aid("c"), serviceIds(lsids("s1"), lsids()));
    assertEquals(1, indices.size());
    index = indices.get(0);
    assertEquals(5, index.getStopTimes().size());
    assertEquals(bcA.getStopTimes().get(2), index.getStopTimes().get(0));
    assertEquals(bcB.getStopTimes().get(2), index.getStopTimes().get(1));
    assertEquals(bcC.getStopTimes().get(2), index.getStopTimes().get(2));
    assertEquals(bcF.getStopTimes().get(2), index.getStopTimes().get(3));
    assertEquals(bcE.getStopTimes().get(2), index.getStopTimes().get(4));

    indices = grep(allIndices, aid("c"), serviceIds(lsids("s1", "s2"), lsids()));
    assertEquals(1, indices.size());
    index = indices.get(0);
    assertEquals(1, index.getStopTimes().size());
    assertEquals(bcD.getStopTimes().get(2), index.getStopTimes().get(0));


    assertNotNull(bcG.getStopTimes());
    assertEquals(OccupancyStatus.EMPTY, bcG.getStopTimes().get(0).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.MANY_SEATS_AVAILABLE, bcG.getStopTimes().get(1).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.FEW_SEATS_AVAILABLE, bcG.getStopTimes().get(2).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.STANDING_ROOM_ONLY, bcG.getStopTimes().get(3).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.CRUSHED_STANDING_ROOM_ONLY, bcG.getStopTimes().get(4).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.FULL, bcG.getStopTimes().get(5).getStopTime().getHistoricalOccupancy());
    assertEquals(OccupancyStatus.FULL, bcG.getStopTimes().get(6).getStopTime().getHistoricalOccupancy());
  }

  @Test
  public void testFrequencies() {

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

    BlockConfigurationEntry bcA = linkBlockTrips(blockA, freqsA, tripA);

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

    BlockConfigurationEntry bcB = linkBlockTrips(blockB, freqsB, tripB);

    BlockStopTimeIndicesFactory factory = new BlockStopTimeIndicesFactory();

    List<FrequencyBlockStopTimeIndex> allIndices = factory.createFrequencyIndices(Arrays.asList(
        (BlockEntry) blockB, blockA));

    assertEquals(2, allIndices.size());

    List<FrequencyBlockStopTimeIndex> indices = grep(allIndices, aid("a"),
        serviceIds(lsids("s1"), lsids()));
    assertEquals(1, indices.size());

    FrequencyBlockStopTimeIndex index = indices.get(0);

    assertEquals(freqA1, index.getFrequencies().get(0));
    assertEquals(freqB1, index.getFrequencies().get(1));
    assertEquals(freqA2, index.getFrequencies().get(2));
    assertEquals(freqB2, index.getFrequencies().get(3));

    assertEquals(bcA, index.getBlockConfigs().get(0));
    assertEquals(bcB, index.getBlockConfigs().get(1));
    assertEquals(bcA, index.getBlockConfigs().get(2));
    assertEquals(bcB, index.getBlockConfigs().get(3));
  }

  private <T extends AbstractBlockStopTimeIndex> List<T> grep(List<T> datas,
      AgencyAndId stopId, ServiceIdActivation serviceIds) {

    List<T> matches = new ArrayList<T>();

    for (T data : datas) {
      if (data.getStop().getId().equals(stopId)
          && data.getServiceIds().equals(serviceIds))
        matches.add(data);
    }
    return matches;
  }
}
