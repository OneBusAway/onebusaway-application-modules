/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
