package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.ValueAdapter;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;

public class BlockStopTimeArrivalTimeValueAdapter implements
    ValueAdapter<BlockStopTimeEntry> {

  public static BlockStopTimeArrivalTimeValueAdapter INSTANCE = new BlockStopTimeArrivalTimeValueAdapter();

  @Override
  public double getValue(BlockStopTimeEntry value) {
    return value.getStopTime().getArrivalTime();
  }

}