package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public class BlockIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockConfigurationIndex> _blockIndices;

  private final ServiceIntervalBlock _serviceIntervalBlock;

  public BlockIndexData(List<BlockConfigurationIndex> blockIndices,
      ServiceIntervalBlock serviceIntervalBlock) {
    _blockIndices = blockIndices;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockConfigurationIndex> getBlockIndices() {
    return _blockIndices;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public BlockIndex createIndex(TransitGraphDao dao) {

    List<BlockConfigurationEntry> configurations = new ArrayList<BlockConfigurationEntry>();

    for (BlockConfigurationIndex blockConfigurationIndex : _blockIndices) {
      AgencyAndId blockId = blockConfigurationIndex.getBlockId();
      int configurationIndex = blockConfigurationIndex.getConfigurationIndex();
      BlockEntry block = dao.getBlockEntryForId(blockId);
      if (block == null)
        throw new IllegalStateException("block does not exist: "
            + blockConfigurationIndex);
      BlockConfigurationEntry configuration = block.getConfigurations().get(
          configurationIndex);
      configurations.add(configuration);
    }

    return new BlockIndex(configurations, _serviceIntervalBlock);
  }
}
