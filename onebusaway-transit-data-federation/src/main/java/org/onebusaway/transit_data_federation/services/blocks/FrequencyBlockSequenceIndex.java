package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyBlockSequenceIndex extends AbstractBlockSequenceIndex {

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param trips
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public FrequencyBlockSequenceIndex(List<BlockSequence> sequences,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    super(sequences);
    if (frequencies == null)
      throw new IllegalArgumentException("frequencies is null");
    if (frequencies.isEmpty())
      throw new IllegalArgumentException("frequencies is empty");

    _frequencies = frequencies;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public FrequencyServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  @Override
  public String toString() {
    return "FrequencyBlockSequenceIndex [block=" + _sequences
        + ", serviceIntervalBlock=" + _serviceIntervalBlock + "]";
  }
}
