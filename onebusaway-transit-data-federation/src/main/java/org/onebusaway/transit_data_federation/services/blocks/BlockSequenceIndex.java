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

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

/**
 * A {@link BlockSequenceIndex} is an ordered collection of
 * {@linkplain BlockSequence block sequences}, where each block sequence visits
 * the same sequence of stops and the arrival-departure times for each stop
 * increase between each block. All blocks are guaranteed to have the same
 * {@link ServiceIdActivation}. This ordered index allows for fast look-up and
 * search operations within the various blocks that make up the index.
 * 
 * Note that the sequence of stops is typically not the ENTIRE sequence of stops
 * for each block, but instead usually a subset. Typically, a block is broken up
 * into pieces when the block changes direction of travel or has an extended
 * layover. See {@link BlockIndexFactoryService} for more details.
 * 
 * @author bdferris
 * 
 * @see BlockSequence
 * @see BlockIndexFactoryService
 * @see BlockStopSequenceIndex
 * @see BlockIndexService
 */
@TransitTimeIndex
public class BlockSequenceIndex extends AbstractBlockSequenceIndex {

  private final ServiceIntervalBlock _serviceIntervalBlock;

  public BlockSequenceIndex(List<BlockSequence> sequences,
      ServiceIntervalBlock serviceIntervalBlock, boolean privateService) {
    super(sequences, privateService);
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {

    BlockSequence first = _sequences.get(0);
    BlockConfigurationEntry blockConfig = first.getBlockConfig();
    BlockEntry block = blockConfig.getBlock();
    List<BlockStopTimeEntry> bsts = first.getStopTimes();
    BlockStopTimeEntry firstBst = bsts.get(0);
    BlockStopTimeEntry lastBst = bsts.get(bsts.size() - 1);
    StopEntry fromStop = firstBst.getStopTime().getStop();
    StopEntry toStop = lastBst.getStopTime().getStop();
    return "BlockSequenceIndex [ex: block=" + block.getId() + " fromStop="
        + fromStop.getId() + " toStop=" + toStop.getId() + " serviceIds="
        + getServiceIds() + "]";
  }

}
