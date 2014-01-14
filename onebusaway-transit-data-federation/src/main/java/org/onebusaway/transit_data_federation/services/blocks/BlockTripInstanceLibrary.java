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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Methods for manipulating {@link BlockTripInstance} objects.
 * 
 * @author bdferris
 * @see BlockTripInstance
 */
public class BlockTripInstanceLibrary {

  /**
   * Creates a {@link BlockTripInstance} from the specified
   * {@link BlockInstance} with the trip from the block matching the specified
   * tripId. If no trip is found in the block with the specified id, then null
   * is returned.
   * 
   * Note that this is just a linear search. If this ends up being a performance
   * bottle-neck, we may have to look for a faster method here
   * 
   * @param blockInstance
   * @param tripId
   * @return the new matching BlockTripInstance
   */
  public static BlockTripInstance getBlockTripInstance(
      BlockInstance blockInstance, AgencyAndId tripId) {
    BlockConfigurationEntry blockConfig = blockInstance.getBlock();
    List<BlockTripEntry> blockTrips = blockConfig.getTrips();
    for (int i = 0; i < blockTrips.size(); ++i) {
      BlockTripEntry blockTrip = blockTrips.get(i);
      if (blockTrip.getTrip().getId().equals(tripId)) {
        return new BlockTripInstance(blockTrip, blockInstance.getState());
      }
    }
    return null;
  }
}
