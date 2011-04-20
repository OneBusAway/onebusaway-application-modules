package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;

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
    return "BlockSequenceIndex [blocks=" + _sequences + ", serviceIds="
        + getServiceIds() + "]";
  }
}
