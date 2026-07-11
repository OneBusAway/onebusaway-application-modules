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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Test;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;

/**
 * Guards {@link BlockConfigurationEntryImpl#getStopTimes()} across the
 * memoization change in {@code BlockStopTimeList.get(int)}: the values must stay
 * correct, repeated {@code get(index)} must return a stable (cached) instance
 * instead of rebuilding a new {@code BlockStopTimeEntryImpl} on every call, and
 * the (transient) cache must not disturb bundle serialization.
 */
public class BlockConfigurationEntryImplTest {

  /**
   * A block of two trips (3 + 2 stops) so the fixture exercises the
   * cross-trip {@code accumulatedStopTimeIndices} offset, not just a single
   * trip. Stop ids "a".."e" map to block sequences 0..4.
   */
  private static BlockConfigurationEntry fiveStopTwoTripBlock() {
    StopEntryImpl stopA = stop("a", 47.0, -122.0);
    StopEntryImpl stopB = stop("b", 47.1, -122.1);
    StopEntryImpl stopC = stop("c", 47.2, -122.2);
    StopEntryImpl stopD = stop("d", 47.3, -122.3);
    StopEntryImpl stopE = stop("e", 47.4, -122.4);

    TripEntryImpl tripA = trip("tripA", "sid");
    stopTime(0, stopA, tripA, time(9, 0), time(9, 0), 0);
    stopTime(1, stopB, tripA, time(9, 10), time(9, 10), 100);
    stopTime(2, stopC, tripA, time(9, 20), time(9, 20), 200);

    TripEntryImpl tripB = trip("tripB", "sid");
    stopTime(3, stopD, tripB, time(9, 30), time(9, 30), 300);
    stopTime(4, stopE, tripB, time(9, 40), time(9, 40), 400);

    return linkBlockTrips(block("blockA"), tripA, tripB);
  }

  private static void assertStopTimesAreCorrect(List<BlockStopTimeEntry> stopTimes) {
    assertEquals(5, stopTimes.size());
    String[] expectedStopIds = {"a", "b", "c", "d", "e"};
    for (int i = 0; i < 5; i++) {
      BlockStopTimeEntry bst = stopTimes.get(i);
      assertEquals("blockSequence at " + i, i, bst.getBlockSequence());
      assertEquals("stop id at " + i, expectedStopIds[i],
          bst.getStopTime().getStop().getId().getId());
    }
    // hasNextStop is true for every index but the last
    for (int i = 0; i < 4; i++)
      assertTrue("hasNextStop at " + i, stopTimes.get(i).hasNextStop());
    assertFalse("last element hasNextStop", stopTimes.get(4).hasNextStop());
  }

  @Test
  public void getStopTimes_valuesCorrectAndElementsMemoized() {
    BlockConfigurationEntry config = fiveStopTwoTripBlock();
    List<BlockStopTimeEntry> stopTimes = config.getStopTimes();

    assertStopTimesAreCorrect(stopTimes);

    // Elements are memoized: repeated get(index) returns the same instance
    // (before the cache, each call rebuilt an equal-but-distinct wrapper).
    for (int i = 0; i < 5; i++)
      assertSame("get(" + i + ") should be memoized", stopTimes.get(i), stopTimes.get(i));

    // getStopTimes() returns the same backing list, so the cache persists across calls
    assertSame(config.getStopTimes().get(3), config.getStopTimes().get(3));
  }

  /**
   * The cache field is {@code transient} because {@code BlockStopTimeEntryImpl}
   * is not {@code Serializable}. Transit bundles are Java-serialized to disk, and
   * a config's cache can already be populated (from block-index computation) at
   * serialize time — so dropping {@code transient}, or caching a non-transient
   * array, would throw {@code NotSerializableException} at bundle-build time and
   * still pass every other test here. This guards that: a config whose cache has
   * been populated must serialize cleanly.
   *
   * <p>We deliberately don't assert on the deserialized graph's contents — a
   * hand-built fixture isn't reconnected the way the production bundle loader
   * reconnects a {@code TransitGraph} on load. The regression this guards fails
   * at {@code writeObject}, which this exercises.
   */
  @Test
  public void populatedCacheSerializesCleanly() throws Exception {
    BlockConfigurationEntry config = fiveStopTwoTripBlock();
    config.getStopTimes().get(2); // populate the transient cache before serializing

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(config); // must NOT throw NotSerializableException
    }
    assertTrue("serialized form should be non-empty", bos.size() > 0);
  }
}
