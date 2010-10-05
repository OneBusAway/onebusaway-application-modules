package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.ValueAdapter;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;

public class BlockStopTimeDistanceAlongBlockValueAdapter implements
    ValueAdapter<BlockStopTimeEntry> {

  public static BlockStopTimeDistanceAlongBlockValueAdapter INSTANCE = new BlockStopTimeDistanceAlongBlockValueAdapter();

  @Override
  public double getValue(BlockStopTimeEntry value) {
    return value.getDistaceAlongBlock();
  }
}