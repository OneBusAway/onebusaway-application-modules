/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

/**
 * A BlockIndex is a collection of {@link BlockConfigurationEntry} elements that
 * have the following properties in common:
 * 
 * 1) Each {@link BlockConfigurationEntry} refers to the same stop sequence
 * pattern and underlying shape of travel.
 * 
 * 2) Each {@link BlockConfigurationEntry} has the same set of service ids (see
 * {@link BlockConfigurationEntry#getServiceIds()}
 * 
 * 3) The list of {@link BlockConfigurationEntry} elements is sorted by arrival
 * time and no block ever overtakes another block.
 * 
 * 4) The {@link ServiceIntervalBlock} additionally captures the min and max
 * arrival and departure times for each block in the list, in the same sorted
 * order as the block list.
 * 
 * These assumptions allow us to do efficient searches for blocks that are
 * active at a particular time.
 * 
 * @author bdferris
 * @see BlockCalendarService
 */
@TransitTimeIndex
public class BlockIndex implements HasBlocks {

  private final List<BlockConfigurationEntry> _blocks;

  private final ServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public BlockIndex(List<BlockConfigurationEntry> blocks,
      ServiceIntervalBlock serviceIntervalBlock) {

    if (blocks == null)
      throw new IllegalArgumentException("blocks is null");
    if (blocks.isEmpty())
      throw new IllegalArgumentException("blocks is empty");
    checkBlocksHaveSameServiceids(blocks);

    _blocks = blocks;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockConfigurationEntry> getBlocks() {
    return _blocks;
  }

  public ServiceIdActivation getServiceIds() {
    return _blocks.get(0).getServiceIds();
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {
    return "BlockIndex [blocks=" + _blocks + ", serviceIntervalBlock="
        + _serviceIntervalBlock + "]";
  }

  private static void checkBlocksHaveSameServiceids(
      List<BlockConfigurationEntry> blocks) {
    ServiceIdActivation expected = blocks.get(0).getServiceIds();
    for (int i = 1; i < blocks.size(); i++) {
      ServiceIdActivation actual = blocks.get(i).getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
