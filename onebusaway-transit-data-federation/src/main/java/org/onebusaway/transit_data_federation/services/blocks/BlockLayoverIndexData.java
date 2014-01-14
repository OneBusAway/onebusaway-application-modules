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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class BlockLayoverIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockTripReference> _blockTripReferences;

  private final LayoverIntervalBlock _layoverIntervalBlock;

  public BlockLayoverIndexData(List<BlockTripReference> blockTripReferences,
      LayoverIntervalBlock layoverIntervalBlock) {
    _blockTripReferences = blockTripReferences;
    _layoverIntervalBlock = layoverIntervalBlock;
  }

  public List<BlockTripReference> getBlockTripReferences() {
    return _blockTripReferences;
  }

  public LayoverIntervalBlock getLayoverIntervalBlock() {
    return _layoverIntervalBlock;
  }

  public BlockLayoverIndex createIndex(TransitGraphDao dao) {

    List<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (BlockTripReference blockTripReference : _blockTripReferences) {
      BlockTripEntry trip = ReferencesLibrary.getReferenceAsTrip(
          blockTripReference, dao);
      trips.add(trip);
    }

    return new BlockLayoverIndex(trips, _layoverIntervalBlock);
  }
}
