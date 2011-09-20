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
package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.Comparator;
import java.util.List;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;

public class ServiceIdListComparator implements
    Comparator<List<LocalizedServiceId>> {

  @Override
  public int compare(List<LocalizedServiceId> o1, List<LocalizedServiceId> o2) {

    // Recall that we want longer lists to come first
    int rc = o2.size() - o1.size();

    if (rc != 0)
      return rc;

    for (int i = 0; i < o1.size(); i++) {
      LocalizedServiceId lsid1 = o1.get(i);
      LocalizedServiceId lsid2 = o2.get(i);
      rc = lsid1.compareTo(lsid2);
      if (rc != 0)
        return rc;
    }

    return 0;
  }

}
