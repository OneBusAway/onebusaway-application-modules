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
package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

/**
 * A memory-optimize structure for holding a list of {@link StopTransfer}
 * objects
 * 
 * @author bdferris
 */
public class StopHopList extends AbstractList<StopHop> {

  private final StopEntry[] stops;

  private final int[] minTravelTimes;

  public StopHopList(List<StopHop> stopHops) {

    int n = stopHops.size();

    stops = new StopEntry[n];
    minTravelTimes = new int[n];

    for (int i = 0; i < n; i++) {
      StopHop stopTransfer = stopHops.get(i);
      stops[i] = stopTransfer.getStop();
      minTravelTimes[i] = stopTransfer.getMinTravelTime();
    }
  }

  @Override
  public int size() {
    return stops.length;
  }

  @Override
  public StopHop get(int index) {
    StopEntry stop = stops[index];
    int minTravelTime = minTravelTimes[index];
    return new StopHop(stop, minTravelTime);
  }
}
