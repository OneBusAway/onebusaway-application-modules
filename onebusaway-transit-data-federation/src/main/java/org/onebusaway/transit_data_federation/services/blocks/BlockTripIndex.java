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

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

@TransitTimeIndex
public class BlockTripIndex extends AbstractBlockTripIndex {

  private final ServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public BlockTripIndex(List<BlockTripEntry> trips,
      ServiceIntervalBlock serviceIntervalBlock) {
    super(trips);
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {
    return "BlockTripIndex [blocks=" + _trips + ", serviceIds="
        + getServiceIds() + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockTripIndex other = (BlockTripIndex) obj;
    // we don't consider serviceInterval as it tends to be dynamic
    if (_trips.size() != other._trips.size() && !_trips.containsAll(other._trips)) {
      return false;
    }
    for (int i = 0; i < _trips.size(); i++) {
      List<BlockStopTimeEntry> stopTimes = _trips.get(i).getStopTimes();
      List<BlockStopTimeEntry> otherStopTimes = other._trips.get(i).getStopTimes();
      if (stopTimes.size() != otherStopTimes.size() && !stopTimes.containsAll(otherStopTimes)) {
        return false;
      }
    }
    return true;
  }
}
