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

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.model.TripInstance;

/**
 * A block instance is the combination of a {@link BlockEntry} and a service
 * date for which that block is active. The "service date" is the
 * "midnight time" from which the {@link StopTimeEntry} entries are relative.
 * Blocks are slightly more complicated than {@link TripInstance}, because a
 * block can be composed of trips with different service ids, not all which are
 * necessarily active on a given service date.
 * 
 * @author bdferris
 * @see BlockEntry
 * @see LocalizedServiceId
 */
public class BlockInstance {

  private final BlockConfigurationEntry _block;

  private final InstanceState _state;

  public BlockInstance(BlockConfigurationEntry block, long serviceDate) {
    this(block, serviceDate, null);
  }

  public BlockInstance(BlockConfigurationEntry block, long serviceDate,
      FrequencyEntry frequency) {
    this(block, new InstanceState(serviceDate, frequency));
  }

  public BlockInstance(BlockConfigurationEntry block, InstanceState state) {
    if (block == null || state == null)
      throw new IllegalArgumentException();
    _block = block;
    _state = state;
  }

  public BlockConfigurationEntry getBlock() {
    return _block;
  }
  
  public InstanceState getState() {
    return _state;
  }

  /**
   * The service date that the block instance is operating. This is the
   * "midnight" time relative to the stop times for the trip.
   * 
   * @return the service date on which the block is operating (Unix-time)
   */
  public long getServiceDate() {
    return _state.getServiceDate();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _block.hashCode();
    result = prime * result + _state.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BlockInstance other = (BlockInstance) obj;
    if (!_block.equals(other._block))
      return false;
    if (!_state.equals(other._state))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return _block.toString() + " " + _state;
  }

}
