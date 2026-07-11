/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.List;

import org.junit.Test;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

/**
 * Guards {@link BlockConfigurationEntryImpl#getStopTimes()} across the
 * memoization change in {@code BlockStopTimeList.get(int)}: the values must stay
 * correct, and repeated {@code get(index)} must return a stable (cached)
 * instance instead of rebuilding a new {@code BlockStopTimeEntryImpl} on every
 * call. The identity assertions fail before the memoization change and pass
 * after it, while the value assertions must hold either way.
 */
public class BlockConfigurationEntryImplTest {

  @Test
  public void getStopTimes_valuesCorrectAndElementsMemoized() {

    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);
    StopEntryImpl stopC = stop("c", 47.2, -122.2);
    StopEntryImpl stopD = stop("d", 47.3, -122.3);
    StopEntryImpl stopE = stop("e", 47.4, -122.4);

    TripEntryImpl tripA = trip("tripA", "sid");
    StopTimeEntry st0 = stopTime(0, stopA, tripA, time(9, 0), time(9, 0), 0);
    StopTimeEntry st1 = stopTime(1, stopB, tripA, time(9, 10), time(9, 10), 100);
    StopTimeEntry st2 = stopTime(2, stopC, tripA, time(9, 20), time(9, 20), 200);

    TripEntryImpl tripB = trip("tripB", "sid");
    StopTimeEntry st3 = stopTime(3, stopD, tripB, time(9, 30), time(9, 30), 300);
    StopTimeEntry st4 = stopTime(4, stopE, tripB, time(9, 40), time(9, 40), 400);

    BlockConfigurationEntry config = linkBlockTrips(block("blockA"), tripA, tripB);
    List<BlockStopTimeEntry> stopTimes = config.getStopTimes();

    // --- values are correct ---
    assertEquals(5, stopTimes.size());

    StopTimeEntry[] expected = {st0, st1, st2, st3, st4};
    for (int i = 0; i < 5; i++) {
      BlockStopTimeEntry bst = stopTimes.get(i);
      assertEquals("blockSequence at " + i, i, bst.getBlockSequence());
      assertSame("stopTime at " + i, expected[i], bst.getStopTime());
    }
    // hasNextStop is true for every index but the last
    for (int i = 0; i < 4; i++)
      assertTrue("hasNextStop at " + i, stopTimes.get(i).hasNextStop());
    assertFalse("last element hasNextStop", stopTimes.get(4).hasNextStop());

    // --- elements are memoized: repeated get(index) returns the same instance ---
    for (int i = 0; i < 5; i++)
      assertSame("get(" + i + ") should be memoized", stopTimes.get(i), stopTimes.get(i));

    // getStopTimes() returns the same backing list, so the cache persists across calls
    assertSame(config.getStopTimes().get(3), config.getStopTimes().get(3));
  }
}
