package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.blocks.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class BlockConfigDistanceAlongBlockIndexAdapter implements
    IndexAdapter<BlockConfigurationEntry> {

  public static BlockConfigDistanceAlongBlockIndexAdapter INSTANCE = new BlockConfigDistanceAlongBlockIndexAdapter();

  @Override
  public double getValue(BlockConfigurationEntry source, int index) {
    return source.getDistanceAlongBlockForIndex(index);
  }
}