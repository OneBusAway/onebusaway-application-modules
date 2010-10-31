package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyBlockStopTimeEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyBlockStopTimeIndex extends
    AbstractBlockStopTimeIndex<FrequencyBlockIndex> {

  private final FrequencyBlockStopTimeList _frequencyStopTimes = new FrequencyBlockStopTimeList();

  public static FrequencyBlockStopTimeIndex create(
      FrequencyBlockIndex blockIndex, int blockSequence) {

    ServiceInterval serviceInterval = computeServiceInterval(blockIndex,
        blockSequence);
    return new FrequencyBlockStopTimeIndex(blockIndex, blockSequence,
        serviceInterval);
  }

  public FrequencyBlockStopTimeIndex(FrequencyBlockIndex blockIndex,
      int blockSequence, ServiceInterval serviceInterval) {
    super(blockIndex, blockSequence, serviceInterval);
  }

  public double getStartTimeForIndex(int index) {
    return _blockIndex.getFrequencies().get(index).getStartTime();
  }

  public double getEndTimeForIndex(int index) {
    return _blockIndex.getFrequencies().get(index).getEndTime();
  }

  public List<FrequencyBlockStopTimeEntry> getFrequencyStopTimes() {
    return _frequencyStopTimes;
  }

  private class FrequencyBlockStopTimeList extends
      AbstractList<FrequencyBlockStopTimeEntry> {

    @Override
    public int size() {
      return getStopTimes().size();
    }

    @Override
    public FrequencyBlockStopTimeEntry get(int index) {
      BlockStopTimeEntry blockStopTime = getStopTimes().get(index);
      FrequencyEntry frequency = getBlockIndex().getFrequencies().get(index);
      return new FrequencyBlockStopTimeEntryImpl(blockStopTime, frequency);
    }

  }


}
