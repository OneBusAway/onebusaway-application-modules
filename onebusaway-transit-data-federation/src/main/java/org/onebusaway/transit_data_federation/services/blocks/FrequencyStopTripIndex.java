/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.HasFrequencyBlockStopTimes;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

@TransitTimeIndex
public class FrequencyStopTripIndex implements HasIndexedFrequencyBlockTrips,
    HasFrequencyBlockStopTimes {

  private final FrequencyBlockTripIndex _index;

  private final int _offset;

  private final FrequencyStopTimeList _frequencyStopTimes = new FrequencyStopTimeList();

  public FrequencyStopTripIndex(FrequencyBlockTripIndex index, int offset) {
    _index = index;
    _offset = offset;
  }

  public FrequencyBlockTripIndex getIndex() {
    return _index;
  }

  public int getOffset() {
    return _offset;
  }

  public ServiceIdActivation getServiceIds() {
    return _index.getServiceIds();
  }

  public ServiceInterval getServiceInterval() {
    return _index.getServiceIntervalBlock().getRange();
  }

  public int size() {
    return _index.size();
  }

  /****
   * {@link HasIndexedFrequencyBlockTrips} Interface
   ****/

  @Override
  public int getStartTimeForIndex(int index) {
    return _index.getStartTimeForIndex(index);
  }

  @Override
  public int getEndTimeForIndex(int index) {
    return _index.getEndTimeForIndex(index);
  }

  /****
   * {@link HasFrequencyBlockStopTimes} Interface
   ****/

  @Override
  public List<FrequencyBlockStopTimeEntry> getFrequencyStopTimes() {
    return _frequencyStopTimes;
  }

  /****
   * Private
   ****/

  private class FrequencyStopTimeList extends
      AbstractList<FrequencyBlockStopTimeEntry> {

    @Override
    public int size() {
      return _index.size();
    }

    @Override
    public FrequencyBlockStopTimeEntry get(int index) {
      List<BlockTripEntry> trips = _index.getTrips();
      BlockTripEntry trip = trips.get(index);
      BlockStopTimeEntry blockStopTime = trip.getStopTimes().get(_offset);
      List<FrequencyEntry> frequencies = _index.getFrequencies();
      FrequencyEntry frequency = frequencies.get(index);
      return new FrequencyBlockStopTimeEntryImpl(blockStopTime, frequency);
    }
  }

}
