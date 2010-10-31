package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public class AbstractBlockIndex implements HasBlocks {

  private final List<BlockConfigurationEntry> _blocks;

  /**
   * See the requirements in the class documentation.
   * 
   * @param blocks
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public AbstractBlockIndex(List<BlockConfigurationEntry> blocks) {

    if (blocks == null)
      throw new IllegalArgumentException("blocks is null");
    if (blocks.isEmpty())
      throw new IllegalArgumentException("blocks is empty");
    checkBlocksHaveSameServiceids(blocks);

    _blocks = blocks;
  }

  public List<BlockConfigurationEntry> getBlocks() {
    return _blocks;
  }

  public ServiceIdActivation getServiceIds() {
    return _blocks.get(0).getServiceIds();
  }

  private static void checkBlocksHaveSameServiceids(
      List<BlockConfigurationEntry> blocks) {
    ServiceIdActivation expected = blocks.get(0).getServiceIds();
    for (int i = 1; i < blocks.size(); i++) {
      ServiceIdActivation actual = blocks.get(i).getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
