package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class FrequencyBlockIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockConfigurationIndex> _blockIndices;

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  public FrequencyBlockIndexData(List<BlockConfigurationIndex> blockIndices,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    _blockIndices = blockIndices;
    _frequencies = frequencies;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockConfigurationIndex> getBlockIndices() {
    return _blockIndices;
  }

  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public FrequencyServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public FrequencyBlockIndex createIndex(TransitGraphDao dao) {

    ArrayList<BlockConfigurationEntry> configurations = new ArrayList<BlockConfigurationEntry>();

    for (int i = 0; i < _blockIndices.size(); i++) {

      BlockConfigurationIndex blockConfigurationIndex = _blockIndices.get(i);

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

    configurations.trimToSize();

    return new FrequencyBlockIndex(configurations, _frequencies,
        _serviceIntervalBlock);
  }
}
