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
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

class BlockStopTimeLooseComparator<T extends HasBlockStopTimes>
    implements Comparator<T> {

  @Override
  public int compare(T o1, T o2) {

    List<BlockStopTimeEntry> stopTimes1 = o1.getStopTimes();
    List<BlockStopTimeEntry> stopTimes2 = o2.getStopTimes();

    if (stopTimes1.isEmpty())
      throw new IllegalStateException("block trip has no stop times: " + o1);
    if (stopTimes2.isEmpty())
      throw new IllegalStateException("block trip has no stop times: " + o2);

    BlockStopTimeEntry bst1 = stopTimes1.get(0);
    BlockStopTimeEntry bst2 = stopTimes2.get(0);

    StopTimeEntry st1 = bst1.getStopTime();
    StopTimeEntry st2 = bst2.getStopTime();

    return st1.getDepartureTime() - st2.getDepartureTime();
  }
}