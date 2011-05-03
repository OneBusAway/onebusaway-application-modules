package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class BlockSequenceIndex extends AbstractBlockSequenceIndex {

  private final ServiceIntervalBlock _serviceIntervalBlock;

  public BlockSequenceIndex(List<BlockSequence> sequences,
      ServiceIntervalBlock serviceIntervalBlock) {
    super(sequences);
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {

    BlockSequence first = _sequences.get(0);
    BlockConfigurationEntry blockConfig = first.getBlockConfig();
    BlockEntry block = blockConfig.getBlock();
    List<BlockStopTimeEntry> bsts = first.getStopTimes();
    BlockStopTimeEntry firstBst = bsts.get(0);
    BlockStopTimeEntry lastBst = bsts.get(bsts.size() - 1);
    StopEntry fromStop = firstBst.getStopTime().getStop();
    StopEntry toStop = lastBst.getStopTime().getStop();
    return "BlockSequenceIndex [ex: block=" + block.getId() + " fromStop="
        + fromStop.getId() + " toStop=" + toStop.getId() + " serviceIds="
        + getServiceIds() + "]";
  }

}
