package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.blocks.GenericBinarySearch.ValueAdapter;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;

public class BlockStopTimeDepartureTimeValueAdapter implements
    ValueAdapter<BlockStopTimeEntry> {

  public static BlockStopTimeDepartureTimeValueAdapter INSTANCE = new BlockStopTimeDepartureTimeValueAdapter();

  @Override
  public double getValue(BlockStopTimeEntry value) {
    return value.getStopTime().getDepartureTime();
  }

}