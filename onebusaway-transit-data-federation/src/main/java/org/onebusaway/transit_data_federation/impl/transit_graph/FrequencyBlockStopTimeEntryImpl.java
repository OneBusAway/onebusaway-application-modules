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
    if (blockStopTime == null || frequency == null)
      throw new IllegalArgumentException();
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

  @Override
  public int getStopTimeOffset() {
    int d0 = _blockStopTime.getTrip().getDepartureTimeForIndex(0);
    int d1 = _blockStopTime.getStopTime().getDepartureTime();
    int delta = d1 - d0;
    int headway = _frequency.getHeadwaySecs();
    return delta % headway;
  }
}