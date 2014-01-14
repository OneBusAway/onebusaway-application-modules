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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

class BlockTripLayoverTimeComparator implements
    Comparator<BlockTripEntry> {
  @Override
  public int compare(BlockTripEntry o1, BlockTripEntry o2) {

    int t1 = getLayoverStartTimeForTrip(o1);
    int t2 = getLayoverStartTimeForTrip(o2);

    return t1 - t2;
  }

  public static int getLayoverStartTimeForTrip(BlockTripEntry blockTrip) {
    BlockTripEntry prevTrip = blockTrip.getPreviousTrip();
    if (prevTrip == null)
      throw new IllegalStateException(
          "blockTrip had no incoming trip, thus no layover");
    List<BlockStopTimeEntry> stopTimes = prevTrip.getStopTimes();
    BlockStopTimeEntry blockStopTime = stopTimes.get(stopTimes.size() - 1);
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    return stopTime.getDepartureTime();
  }
  
  public static int getLayoverEndTimeForTrip(BlockTripEntry blockTrip) {
    List<BlockStopTimeEntry> stopTimes = blockTrip.getStopTimes();
    BlockStopTimeEntry blockStopTime = stopTimes.get(0);
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    return stopTime.getArrivalTime();
  }
}