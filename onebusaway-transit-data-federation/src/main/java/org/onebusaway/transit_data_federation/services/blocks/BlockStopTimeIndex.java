package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class BlockStopTimeIndex extends AbstractBlockStopTimeIndex<BlockIndex> {

  public static BlockStopTimeIndex create(BlockIndex blockIndex,
      int blockSequence) {

    ServiceInterval serviceInterval = computeServiceInterval(blockIndex,
        blockSequence);

    return new BlockStopTimeIndex(blockIndex, blockSequence, serviceInterval);
  }

  public int getArrivalTimeForIndex(int index) {
    BlockConfigurationEntry block = _blockIndex.getBlocks().get(index);
    return block.getArrivalTimeForIndex(_blockSequence);
  }

  public int getDepartureTimeForIndex(int index) {
    BlockConfigurationEntry block = _blockIndex.getBlocks().get(index);
    return block.getDepartureTimeForIndex(_blockSequence);
  }
  
  public double getDistanceAlongBlockForIndex(int index) {
    BlockConfigurationEntry block = _blockIndex.getBlocks().get(index);
    return block.getDistanceAlongBlockForIndex(_blockSequence);
  }

  public BlockStopTimeIndex(BlockIndex blockIndex, int blockSequence,
      ServiceInterval serviceInterval) {
    super(blockIndex, blockSequence, serviceInterval);
  }
}
