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

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

public class BlockTripInstance {

  private final BlockTripEntry blockTrip;

  private final InstanceState state;

  public BlockTripInstance(BlockTripEntry blockTrip, InstanceState state) {
    this.blockTrip = blockTrip;
    this.state = state;
  }

  public BlockTripEntry getBlockTrip() {
    return blockTrip;
  }

  public InstanceState getState() {
    return state;
  }
  
  public long getServiceDate() {
    return state.getServiceDate();
  }

  /**
   * If the {@link InstanceState} associated wit this block trip instance has a
   * {@link FrequencyEntry} (see {@link InstanceState#getFrequency()}), it is
   * returned. Otherwise, if the {@link TripEntry} associated with this block
   * trip instance has a {@link FrequencyEntry} (see
   * {@link TripEntry#getFrequencyLabel()}), then it is returned. Otherwise,
   * null is returned.
   * 
   * @return
   */
  public FrequencyEntry getFrequencyLabel() {
    if (state.getFrequency() != null) {
      return state.getFrequency();
    }
    return blockTrip.getTrip().getFrequencyLabel();
  }
}
