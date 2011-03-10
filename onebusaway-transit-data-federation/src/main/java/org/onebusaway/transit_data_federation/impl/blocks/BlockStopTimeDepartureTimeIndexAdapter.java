package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;

public class BlockStopTimeDepartureTimeIndexAdapter implements
    IndexAdapter<BlockStopTimeIndex> {

  public static BlockStopTimeDepartureTimeIndexAdapter INSTANCE = new BlockStopTimeDepartureTimeIndexAdapter();

  @Override
  public double getValue(BlockStopTimeIndex source, int index) {
    return source.getDepartureTimeForIndex(index);
  }
}