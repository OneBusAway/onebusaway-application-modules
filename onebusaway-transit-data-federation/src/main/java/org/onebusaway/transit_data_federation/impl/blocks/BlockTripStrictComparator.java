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
import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;

/**
 * Check two see if all arrival and departure times from one block sequence are
 * strictly increasing when compared to another block sequence.
 * 
 * @author bdferris
 * @see HasBlockStopTimes
 */
class BlockTripStrictComparator<T extends HasBlockStopTimes> implements
    Comparator<T> {

  private static final BlockStopTimeStrictComparator _c = new BlockStopTimeStrictComparator();

  @Override
  public int compare(T a, T b) {

    List<BlockStopTimeEntry> stopTimesA = a.getStopTimes();
    List<BlockStopTimeEntry> stopTimesB = b.getStopTimes();

    boolean allEqual = true;

    for (int i = 0; i < stopTimesA.size(); i++) {
      int c = _c.compare(stopTimesA.get(i), stopTimesB.get(i));
      if (c > 0)
        return c;
      if (c < 0)
        allEqual = false;
    }

    if (allEqual)
      return 0;

    return -1;
  }

}