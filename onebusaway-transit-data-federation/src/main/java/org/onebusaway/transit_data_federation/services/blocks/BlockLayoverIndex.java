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

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

/**
 * An ordered index over layover intervals of {@link BlockTripEntry} elements. A
 * layover is a portion of a block where we have determined that a vehicle will
 * be sitting, out of operation, between trips. Note that our definition of a
 * layover doesn't include portions of a trip where there is a pause between the
 * arrival and departure of a vehicle from a stop. Instead, we focus on layovers
 * where the passengers typically aren't allowed to stay on the bus, such as a
 * layover at the route terminus.
 * 
 * The start and end times for each layover internal are captured in a
 * {@link LayoverIntervalBlock}. The {@link BlockTripEntry} trips associated
 * with the layover are kept as well. Each {@link BlockTripEntry} in the index
 * will have the same {@link ServiceIdActivation}.
 * 
 * @author bdferris
 * @see LayoverIntervalBlock
 */
@TransitTimeIndex
public class BlockLayoverIndex extends AbstractBlockTripIndex {

  private final LayoverIntervalBlock _layoverIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param layoverIntervalBlock
   */
  public BlockLayoverIndex(List<BlockTripEntry> trips,
      LayoverIntervalBlock layoverIntervalBlock) {
    super(trips);
    _layoverIntervalBlock = layoverIntervalBlock;
  }

  public LayoverIntervalBlock getLayoverIntervalBlock() {
    return _layoverIntervalBlock;
  }

  @Override
  public String toString() {
    return "BlockLayoverIndex [blocks=" + _trips + ", serviceIds="
        + getServiceIds() + "]";
  }
}
