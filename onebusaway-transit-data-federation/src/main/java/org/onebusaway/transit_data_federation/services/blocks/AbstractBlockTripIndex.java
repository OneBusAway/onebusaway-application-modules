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

import org.onebusaway.transit_data_federation.impl.transit_graph.BlockTripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

/**
 * Abstract support class for building searchable indices over
 * {@link BlockTripEntry} elements. The class has checks to ensure that each
 * block trip has the same {@link ServiceIdActivation}.
 * 
 * @author bdferris
 * 
 */
public abstract class AbstractBlockTripIndex implements HasBlockTrips {

  protected final List<BlockTripEntry> _trips;

  public AbstractBlockTripIndex(List<BlockTripEntry> trips) {
    if (trips == null)
      throw new IllegalArgumentException("trips is null");
    if (trips.isEmpty())
      throw new IllegalArgumentException("trips is empty");

    checkTripsHaveSameServiceids(trips);

    _trips = trips;
    for (BlockTripEntry trip : trips) {
      BlockTripEntryImpl tripImpl = (BlockTripEntryImpl) trip;
      tripImpl.setPattern(this);
    }
  }

  public List<BlockTripEntry> getTrips() {
    return _trips;
  }

  public ServiceIdActivation getServiceIds() {
    return _trips.get(0).getBlockConfiguration().getServiceIds();
  }

  public int size() {
    return _trips.size();
  }

  private static void checkTripsHaveSameServiceids(List<BlockTripEntry> trips) {
    ServiceIdActivation expected = trips.get(0).getBlockConfiguration().getServiceIds();
    for (int i = 1; i < trips.size(); i++) {
      ServiceIdActivation actual = trips.get(i).getBlockConfiguration().getServiceIds();
      if (!expected.equals(actual))
        throw new IllegalArgumentException("serviceIds mismatch: expected="
            + expected + " actual=" + actual);
    }
  }
}
