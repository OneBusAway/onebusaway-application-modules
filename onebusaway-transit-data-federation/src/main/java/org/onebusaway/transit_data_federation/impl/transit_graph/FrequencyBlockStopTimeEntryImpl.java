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