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

class BlockStopTimeComparator implements Comparator<BlockStopTimeEntry> {

  @Override
  public int compare(BlockStopTimeEntry o1, BlockStopTimeEntry o2) {

    int at1 = o1.getStopTime().getArrivalTime();
    int at2 = o2.getStopTime().getArrivalTime();

    int c = at1 - at2;

    if (c != 0)
      return c;

    int dt1 = o1.getStopTime().getDepartureTime();
    int dt2 = o2.getStopTime().getDepartureTime();

    return dt1 - dt2;
  }
}
