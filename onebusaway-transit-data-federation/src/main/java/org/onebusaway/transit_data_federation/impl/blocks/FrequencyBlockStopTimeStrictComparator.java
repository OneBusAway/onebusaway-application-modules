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

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyBlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

class FrequencyBlockStopTimeStrictComparator implements
    Comparator<FrequencyBlockStopTimeEntry> {

  @Override
  public int compare(FrequencyBlockStopTimeEntry o1,
      FrequencyBlockStopTimeEntry o2) {

    FrequencyEntry f1 = o1.getFrequency();
    FrequencyEntry f2 = o2.getFrequency();

    if (f1.getStartTime() == f2.getStartTime()
        && f1.getEndTime() == f2.getEndTime()) {
      return 0;
    } else if (f1.getStartTime() < f2.getStartTime()
        && f1.getEndTime() < f2.getEndTime()) {
      return -1;
    } else {
      return 1;
    }
  }
}
