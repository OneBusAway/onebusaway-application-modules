/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.services.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class BlockTripInstanceLibraryTest {

  @Test
  public void test() {
    BlockEntryImpl block = UnitTestingSupport.block("block");
    TripEntryImpl tripA = UnitTestingSupport.trip("tripA");
    TripEntryImpl tripB = UnitTestingSupport.trip("tripB");
    UnitTestingSupport.stopTime(0, null, tripA, 0, 0);
    UnitTestingSupport.stopTime(0, null, tripB, 0, 0);
    ServiceIdActivation serviceIds = UnitTestingSupport.serviceIds("sid");
    BlockConfigurationEntry blockConfig = UnitTestingSupport.blockConfiguration(
        block, serviceIds, tripA, tripB);

    BlockInstance blockInstanceA = new BlockInstance(blockConfig, 123L);
    BlockTripInstance blockTripInstance = BlockTripInstanceLibrary.getBlockTripInstance(
        blockInstanceA, UnitTestingSupport.aid("tripA"));
    assertSame(tripA, blockTripInstance.getBlockTrip().getTrip());
    assertEquals(123L, blockTripInstance.getServiceDate());

    blockTripInstance = BlockTripInstanceLibrary.getBlockTripInstance(
        blockInstanceA, UnitTestingSupport.aid("tripB"));
    assertSame(tripB, blockTripInstance.getBlockTrip().getTrip());
    assertEquals(123L, blockTripInstance.getServiceDate());

    blockTripInstance = BlockTripInstanceLibrary.getBlockTripInstance(
        blockInstanceA, UnitTestingSupport.aid("tripC"));
    assertNull(blockTripInstance);
  }
}
