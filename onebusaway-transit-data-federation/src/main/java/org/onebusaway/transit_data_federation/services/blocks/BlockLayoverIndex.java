package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class BlockLayoverIndex extends AbstractBlockTripIndex {

  private final LayoverIntervalBlock _layoverIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param layoverIntervalBlock
   */
  public BlockLayoverIndex(List<BlockTripEntry> trips,
      LayoverIntervalBlock layoverIntervalBlock) {
    super(trips);
    _layoverIntervalBlock = layoverIntervalBlock;
  }

  public LayoverIntervalBlock getLayoverIntervalBlock() {
    return _layoverIntervalBlock;
  }

  @Override
  public String toString() {
    return "BlockLayoverIndex [blocks=" + _trips + ", serviceIds="
        + getServiceIds() + "]";
  }
}
