package org.onebusaway.transit_data_federation.impl.blocks;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.IndexAdapter;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class BlockConfigDepartureTimeIndexAdapter implements
    IndexAdapter<BlockConfigurationEntry> {

  public static BlockConfigDepartureTimeIndexAdapter INSTANCE = new BlockConfigDepartureTimeIndexAdapter();

  @Override
  public double getValue(BlockConfigurationEntry source, int index) {
    return source.getDepartureTimeForIndex(index);
  }
}