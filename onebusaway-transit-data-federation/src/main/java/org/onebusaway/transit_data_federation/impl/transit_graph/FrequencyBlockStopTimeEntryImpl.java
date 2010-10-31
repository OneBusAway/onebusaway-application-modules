package org.onebusaway.transit_data_federation.impl.transit_graph;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyBlockStopTimeEntryImpl implements
    FrequencyBlockStopTimeEntry {

  private final BlockStopTimeEntry _blockStopTime;

  private final FrequencyEntry _frequency;

  public FrequencyBlockStopTimeEntryImpl(BlockStopTimeEntry blockStopTime,
      FrequencyEntry frequency) {
    _blockStopTime = blockStopTime;
    _frequency = frequency;
  }

  @Override
  public BlockStopTimeEntry getStopTime() {
    return _blockStopTime;
  }

  @Override
  public FrequencyEntry getFrequency() {
    return _frequency;
  }
}