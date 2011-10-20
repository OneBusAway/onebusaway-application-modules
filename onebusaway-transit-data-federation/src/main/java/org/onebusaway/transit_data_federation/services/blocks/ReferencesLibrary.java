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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class ReferencesLibrary {

  public static BlockConfigurationReference getBlockAsReference(
      BlockConfigurationEntry blockConfig) {
    BlockEntry block = blockConfig.getBlock();
    int configurationIndex = block.getConfigurations().indexOf(blockConfig);
    return new BlockConfigurationReference(block.getId(), configurationIndex);
  }

  public static BlockTripReference getTripAsReference(BlockTripEntry trip) {
    BlockConfigurationEntry blockConfig = trip.getBlockConfiguration();
    BlockConfigurationReference blockConfigRef = getBlockAsReference(blockConfig);
    int tripIndex = blockConfig.getTrips().indexOf(trip);
    return new BlockTripReference(blockConfigRef, tripIndex);
  }

  public static BlockTripEntry getReferenceAsTrip(BlockTripReference reference,
      TransitGraphDao dao) {

    BlockConfigurationEntry blockConfig = getReferenceAsBlockConfiguration(
        reference.getBlockConfigurationReference(), dao);

    List<BlockTripEntry> trips = blockConfig.getTrips();
    int tripIndex = reference.getTripIndex();

    return trips.get(tripIndex);
  }

  public static BlockConfigurationEntry getReferenceAsBlockConfiguration(
      BlockConfigurationReference reference, TransitGraphDao dao) {
    AgencyAndId blockId = reference.getBlockId();
    int configurationIndex = reference.getConfigurationIndex();
    BlockEntry block = dao.getBlockEntryForId(blockId);
    if (block == null)
      throw new IllegalStateException("block does not exist: " + reference);
    return block.getConfigurations().get(configurationIndex);
  }

}
