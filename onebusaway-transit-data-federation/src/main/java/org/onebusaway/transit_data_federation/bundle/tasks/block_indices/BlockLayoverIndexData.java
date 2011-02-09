package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.BlockLayoverIndex;
import org.onebusaway.transit_data_federation.services.blocks.LayoverIntervalBlock;
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
