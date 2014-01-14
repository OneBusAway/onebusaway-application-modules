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

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;

public class BlockSequence implements HasBlockStopTimes {

  private final BlockConfigurationEntry blockConfig;

  private final int blockSequenceFrom;

  private final int blockSequenceTo;

  public BlockSequence(BlockConfigurationEntry blockConfig,
      int blockSequenceFrom, int blockSequenceTo) {
    this.blockConfig = blockConfig;
    this.blockSequenceFrom = blockSequenceFrom;
    this.blockSequenceTo = blockSequenceTo;
  }

  public BlockConfigurationEntry getBlockConfig() {
    return blockConfig;
  }

  public int getBlockSequenceFrom() {
    return blockSequenceFrom;
  }

  public int getBlockSequenceTo() {
    return blockSequenceTo;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    return stopTimes.subList(blockSequenceFrom, blockSequenceTo);
  }

  public int getArrivalTimeForIndex(int index) {
    return blockConfig.getArrivalTimeForIndex(blockSequenceFrom + index);
  }

  public int getDepartureTimeForIndex(int index) {
    return blockConfig.getDepartureTimeForIndex(blockSequenceFrom + index);
  }
}
