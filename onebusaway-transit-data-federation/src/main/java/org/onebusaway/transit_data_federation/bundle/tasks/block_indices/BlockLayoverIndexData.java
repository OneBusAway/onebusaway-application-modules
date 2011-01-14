package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class BlockLayoverIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockTripReference> _blockTripReferences;

  private final ServiceIntervalBlock _serviceIntervalBlock;

  public BlockLayoverIndexData(List<BlockTripReference> blockTripReferences,
      ServiceIntervalBlock serviceIntervalBlock) {
    _blockTripReferences = blockTripReferences;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockTripReference> getBlockTripReferences() {
    return _blockTripReferences;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public BlockTripIndex createIndex(TransitGraphDao dao) {

    List<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (BlockTripReference blockTripReference : _blockTripReferences) {
      BlockTripEntry trip = ReferencesLibrary.getReferenceAsTrip(
          blockTripReference, dao);
      trips.add(trip);
    }

    return new BlockTripIndex(trips, _serviceIntervalBlock);
  }
}
