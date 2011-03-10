package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

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
