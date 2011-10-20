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

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyComparator implements Comparator<FrequencyEntry> {

  @Override
  public int compare(FrequencyEntry entryA, FrequencyEntry entryB) {

    int rc = entryA.getStartTime() - entryB.getStartTime();

    if (rc != 0)
      return rc;

    rc = entryA.getEndTime() - entryB.getEndTime();

    if (rc != 0)
      return rc;

    return entryA.getHeadwaySecs() - entryB.getHeadwaySecs();
  }
}
