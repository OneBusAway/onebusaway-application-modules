package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;

public class BlockStopTimeDistanceAlongBlockIndexAdapter implements
    IndexAdapter<BlockStopTimeIndex> {

  public static BlockStopTimeDistanceAlongBlockIndexAdapter INSTANCE = new BlockStopTimeDistanceAlongBlockIndexAdapter();

  @Override
  public double getValue(BlockStopTimeIndex source, int index) {
    return source.getDistanceAlongBlockForIndex(index);
  }
}