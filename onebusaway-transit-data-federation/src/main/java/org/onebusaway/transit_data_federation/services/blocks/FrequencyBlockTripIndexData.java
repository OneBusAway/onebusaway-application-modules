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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class FrequencyBlockTripIndexData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final List<BlockTripReference> _trips;

  private final List<FrequencyEntry> _frequencies;

  private final FrequencyServiceIntervalBlock _serviceIntervalBlock;

  public FrequencyBlockTripIndexData(List<BlockTripReference> trips,
      List<FrequencyEntry> frequencies,
      FrequencyServiceIntervalBlock serviceIntervalBlock) {
    _trips = trips;
    _frequencies = frequencies;
    _serviceIntervalBlock = serviceIntervalBlock;
  }

  public List<BlockTripReference> getTrips() {
    return _trips;
  }

  public List<FrequencyEntry> getFrequencies() {
    return _frequencies;
  }

  public FrequencyServiceIntervalBlock getServiceIntervalBlock() {
    return _serviceIntervalBlock;
  }

  public FrequencyBlockTripIndex createIndex(TransitGraphDao dao) {

    ArrayList<BlockTripEntry> trips = new ArrayList<BlockTripEntry>();

    for (int i = 0; i < _trips.size(); i++) {

      BlockTripReference tripReference = _trips.get(i);
      BlockTripEntry blockTrip = ReferencesLibrary.getReferenceAsTrip(tripReference, dao);
      trips.add(blockTrip);
    }

    trips.trimToSize();

    return new FrequencyBlockTripIndex(trips, _frequencies,
        _serviceIntervalBlock);
  }
}
