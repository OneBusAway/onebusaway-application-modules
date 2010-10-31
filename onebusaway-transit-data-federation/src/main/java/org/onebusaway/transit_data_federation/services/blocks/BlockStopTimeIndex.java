package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;

public class BlockStopTimeIndex extends AbstractBlockStopTimeIndex<BlockIndex> {

  public static BlockStopTimeIndex create(BlockIndex blockIndex,
      int blockSequence) {

    ServiceInterval serviceInterval = computeServiceInterval(blockIndex,
        blockSequence);

    return new BlockStopTimeIndex(blockIndex, blockSequence, serviceInterval);
  }

  public BlockStopTimeIndex(BlockIndex blockIndex, int blockSequence,
      ServiceInterval serviceInterval) {
    super(blockIndex, blockSequence, serviceInterval);
  }
}
