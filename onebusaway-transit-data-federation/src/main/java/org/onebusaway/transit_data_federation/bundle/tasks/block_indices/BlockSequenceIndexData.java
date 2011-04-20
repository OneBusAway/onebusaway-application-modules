package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.ServiceIntervalBlock;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class BlockSequenceIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockConfigurationReference> _blockConfigReferences;

  private final int[] _blockSequenceFrom;

  private final int[] _blockSequenceTo;

  private final ServiceIntervalBlock _serviceIntervalBlock;

  public BlockSequenceIndexData(
      List<BlockConfigurationReference> blockConfigReferences,
      int[] blockSequenceFrom, int[] blockSequenceTo,
      ServiceIntervalBlock serviceIntervalBlock) {
    _blockConfigReferences = blockConfigReferences;
    _blockSequenceFrom = blockSequenceFrom;
    _blockSequenceTo = blockSequenceTo;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockConfigurationReference> getBlockConfigReferences() {
    return _blockConfigReferences;
  }

  public int[] getBlockSequenceFrom() {
    return _blockSequenceFrom;
  }

  public int[] getBlockSequenceTo() {
    return _blockSequenceTo;
  }

  public ServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public BlockSequenceIndex createIndex(TransitGraphDao dao) {

    List<BlockSequence> sequences = new ArrayList<BlockSequence>();

    for (int i = 0; i < _blockConfigReferences.size(); i++) {
      BlockConfigurationReference ref = _blockConfigReferences.get(i);
      BlockConfigurationEntry blockConfig = ReferencesLibrary.getReferenceAsBlockConfiguration(
          ref, dao);
      int sequenceFrom = _blockSequenceFrom[i];
      int sequenceTo = _blockSequenceTo[i];
      BlockSequence sequence = new BlockSequence(blockConfig, sequenceFrom,
          sequenceTo);
      sequences.add(sequence);
    }
    return new BlockSequenceIndex(sequences, _serviceIntervalBlock);
  }
}
