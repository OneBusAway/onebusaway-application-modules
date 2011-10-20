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
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public interface BlockIndexFactoryService {

  public List<BlockTripIndexData> createTripData(Iterable<BlockEntry> blocks);

  public List<BlockLayoverIndexData> createLayoverData(
      Iterable<BlockEntry> blocks);

  public List<FrequencyBlockTripIndexData> createFrequencyTripData(
      Iterable<BlockEntry> blocks);

  /****
   * 
   ****/

  public List<BlockTripIndex> createTripIndices(Iterable<BlockEntry> blocks);

  public List<BlockLayoverIndex> createLayoverIndices(
      Iterable<BlockEntry> blocks);

  public List<FrequencyBlockTripIndex> createFrequencyTripIndices(
      Iterable<BlockEntry> blocks);

  public List<BlockSequenceIndex> createSequenceIndices(
      Iterable<BlockEntry> blocks);

  /****
   * 
   ****/

  public BlockTripIndex createTripIndexForGroupOfBlockTrips(
      List<BlockTripEntry> blocks);

  public BlockLayoverIndex createLayoverIndexForGroupOfBlockTrips(
      List<BlockTripEntry> trips);

  public FrequencyBlockTripIndex createFrequencyIndexForTrips(
      List<BlockTripEntry> trips, List<FrequencyEntry> frequencies);

  public BlockSequenceIndex createSequenceIndexForGroupOfBlockSequences(
      List<BlockSequence> sequences);
}
