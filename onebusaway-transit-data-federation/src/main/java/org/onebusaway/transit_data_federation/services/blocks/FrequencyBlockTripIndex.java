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

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

/**
 * A FrequencyBlockTripIndex is a collection of {@link BlockTripEntry} elements
 * that have the following properties in common:
 * 
 * 1) Each {@link BlockTripEntry} refers to the same stop sequence pattern and
 * underlying shape of travel.
 * 
 * 2) Each {@link BlockTripEntry} has the same set of service ids (see
 * {@link BlockConfigurationEntry#getServiceIds()}
 * 
 * 3) Each {@link BlockTripEntry} refers to a frequency-based trip with a
 * {@link FrequencyEntry}. Since the same {@link BlockTripEntry} can have
 * multiple {@link FrequencyEntry}, a trip may appear more than once in the
 * index.
 * 
 * 4) The list of {@link FrequencyEntry} elements is sorted by start time and no
 * frequency block ever overtakes another block.
 * 
 * 4) The {@link ServiceIntervalBlock} additionally captures the min and max
 * arrival and departure times for each trip in the list, in the same sorted
 * order as the block list.
 * 
 * These assumptions allow us to do efficient searches for blocks that are
 * active at a particular time.
 * 
 * @author bdferris
 * @see BlockCalendarService
 */
@TransitTimeIndex
public class FrequencyBlockTripIndex extends AbstractBlockTripIndex implements
    HasIndexedFrequencyBlockTrips {

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  /**
   * See the requirements in the class documentation.
   * 
   * @param trips
   * @param serviceIdIntervals
   * @param serviceIntervalBlock
   */
  public FrequencyBlockTripIndex(List<BlockTripEntry> trips,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    super(trips);
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
    return "FrequencyBlockTripIndex [trips=" + _trips
        + ", serviceIntervalBlock=" + _serviceIntervalBlock + "]";
  }

  /****
   * {@link HasIndexedFrequencyBlockTrips}
   ****/

  @Override
  public int getStartTimeForIndex(int index) {
    return _frequencies.get(index).getStartTime();
  }

  @Override
  public int getEndTimeForIndex(int index) {
    return _frequencies.get(index).getEndTime();
  }
}
