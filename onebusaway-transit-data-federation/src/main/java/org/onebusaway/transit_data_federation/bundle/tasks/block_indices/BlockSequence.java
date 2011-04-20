package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;

public class BlockSequence implements HasBlockStopTimes {

  private final BlockConfigurationEntry blockConfig;

  private final int blockSequenceFrom;

  private final int blockSequenceTo;

  public BlockSequence(BlockConfigurationEntry blockConfig,
      int blockSequenceFrom, int blockSequenceTo) {
    this.blockConfig = blockConfig;
    this.blockSequenceFrom = blockSequenceFrom;
    this.blockSequenceTo = blockSequenceTo;
  }

  public BlockConfigurationEntry getBlockConfig() {
    return blockConfig;
  }

  public int getBlockSequenceFrom() {
    return blockSequenceFrom;
  }

  public int getBlockSequenceTo() {
    return blockSequenceTo;
  }

  @Override
  public List<BlockStopTimeEntry> getStopTimes() {
    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    return stopTimes.subList(blockSequenceFrom, blockSequenceTo);
  }

  public int getArrivalTimeForIndex(int index) {
    return blockConfig.getArrivalTimeForIndex(blockSequenceFrom + index);
  }

  public int getDepartureTimeForIndex(int index) {
    return blockConfig.getDepartureTimeForIndex(blockSequenceFrom + index);
  }
}
