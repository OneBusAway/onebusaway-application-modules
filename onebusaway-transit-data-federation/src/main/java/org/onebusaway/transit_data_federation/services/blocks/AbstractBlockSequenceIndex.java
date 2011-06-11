package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.block_indices.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public abstract class AbstractBlockSequenceIndex {

  protected final List<BlockSequence> _sequences;

  protected final ServiceIdActivation _serviceIds;

  protected final boolean _privateService;

  public AbstractBlockSequenceIndex(List<BlockSequence> sequences,
      boolean privateService) {
    if (sequences == null)
      throw new IllegalArgumentException("sequences is null");
    if (sequences.isEmpty())
      throw new IllegalArgumentException("sequences is empty");

    checkSequencesHaveSameServiceids(sequences);

    _sequences = sequences;
    _serviceIds = _sequences.get(0).getBlockConfig().getServiceIds();
    _privateService = privateService;
  }

  public List<BlockSequence> getSequences() {
    return _sequences;
  }

  public ServiceIdActivation getServiceIds() {
    return _serviceIds;
  }

  public boolean isPrivateService() {
    return _privateService;
  }

  public int size() {
    return _sequences.size();
  }

  private static void checkSequencesHaveSameServiceids(
      List<BlockSequence> blocks) {
    ServiceIdActivation expected = blocks.get(0).getBlockConfig().getServiceIds();
    for (int i = 1; i < blocks.size(); i++) {
      ServiceIdActivation actual = blocks.get(i).getBlockConfig().getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
