package org.onebusaway.transit_data_federation.services.blocks;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyBlockStopTimeEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyBlockStopTimeIndex extends AbstractBlockStopTimeIndex implements HasIndexedFrequencyBlockTrips {

  private final FrequencyBlockStopTimeList _frequencyStopTimes = new FrequencyBlockStopTimeList();

  private final List<FrequencyEntry> _frequencies;

  public FrequencyBlockStopTimeIndex(List<FrequencyEntry> frequencies,
      List<BlockConfigurationEntry> blockConfigs, int[] stopIndices,
      ServiceInterval serviceInterval) {
    super(blockConfigs, stopIndices, serviceInterval);
    _frequencies = frequencies;
  }
  
  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public int getStartTimeForIndex(int index) {
    return _frequencies.get(index).getStartTime();
  }

  public int getEndTimeForIndex(int index) {
    return _frequencies.get(index).getEndTime();
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
      BlockStopTimeEntry blockStopTime = getStopTimeForIndex(index);
      FrequencyEntry frequency = _frequencies.get(index);
      return new FrequencyBlockStopTimeEntryImpl(blockStopTime, frequency);
    }
  }
}
