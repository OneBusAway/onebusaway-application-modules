package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.Comparator;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;

public class BlockInstanceComparator implements Comparator<BlockInstance> {

  @Override
  public int compare(BlockInstance o1, BlockInstance o2) {

    BlockConfigurationEntry bc1 = o1.getBlock();
    BlockConfigurationEntry bc2 = o2.getBlock();
    BlockEntry b1 = bc1.getBlock();
    BlockEntry b2 = bc2.getBlock();

    AgencyAndId bId1 = b1.getId();
    AgencyAndId bId2 = b2.getId();

    int rc = bId1.compareTo(bId2);

    if (rc != 0)
      return rc;

    rc = bc1.getServiceIds().compareTo(bc2.getServiceIds());

    if (rc != 0)
      return rc;

    return Double.compare(o1.getServiceDate(), o2.getServiceDate());
  }
}
