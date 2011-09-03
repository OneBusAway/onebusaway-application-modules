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

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

/**
 * Check two see if arrival and departure times from one block stop time are
 * strictly increasing when compared to another block stop time.
 * 
 * @author bdferris
 */
class BlockStopTimeStrictComparator implements Comparator<BlockStopTimeEntry> {

  @Override
  public int compare(BlockStopTimeEntry o1, BlockStopTimeEntry o2) {

    StopTimeEntry stA = o1.getStopTime();
    StopTimeEntry stB = o2.getStopTime();

    if (stA.getArrivalTime() == stB.getArrivalTime()
        && stA.getDepartureTime() == stB.getDepartureTime()) {
      return 0;
    } else if (stA.getArrivalTime() <= stB.getArrivalTime()
        && stA.getDepartureTime() <= stB.getDepartureTime()) {
      return -1;
    }

    return 1;
  }
}