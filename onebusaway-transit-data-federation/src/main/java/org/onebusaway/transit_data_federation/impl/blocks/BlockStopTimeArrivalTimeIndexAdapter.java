package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;

public class BlockStopTimeArrivalTimeIndexAdapter implements
    IndexAdapter<BlockStopTimeIndex> {

  public static BlockStopTimeArrivalTimeIndexAdapter INSTANCE = new BlockStopTimeArrivalTimeIndexAdapter();

  @Override
  public double getValue(BlockStopTimeIndex source, int index) {
    return source.getArrivalTimeForIndex(index);
  }
}