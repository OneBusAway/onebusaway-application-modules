package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class BlockTripIndex extends AbstractBlockTripIndex {

  private final ServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public BlockTripIndex(List<BlockTripEntry> trips,
      ServiceIntervalBlock serviceIntervalBlock) {
    super(trips);
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {
    return "BlockTripIndex [blocks=" + _trips + ", serviceIds="
        + getServiceIds() + "]";
  }
}
