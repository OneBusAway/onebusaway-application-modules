/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Removed scheduled information from arrivals-and-departures results.
 */
public class ArrivalAndDepartureFilterByRealtime extends ArrivalAndDepartureFilter {

  List<String> realtimeOnlyAgencyIds;
  public ArrivalAndDepartureFilterByRealtime(List<String> realtimeOnlyAgencies) {
    realtimeOnlyAgencyIds = new ArrayList<>();
    realtimeOnlyAgencyIds.addAll(realtimeOnlyAgencies);
  }

  @Override
  public boolean matches(ArrivalAndDepartureBean bean) {
    if (realtimeOnlyAgencyIds == null || realtimeOnlyAgencyIds.isEmpty()) return true;
    if (bean.getTrip() == null) return false; // no data
    AgencyAndId tripAgencyId = AgencyAndIdLibrary.convertFromString(bean.getTrip().getId());

    for (String testAgencyId : realtimeOnlyAgencyIds) {
      if (tripAgencyId.getAgencyId().equals(testAgencyId)) {
        if (isCancelled(bean) || hasRealtime(bean)) {
          return true;
        } else {
          return false;
        }
      }
    }
    // we didn't match the agency, let it flow through
    return true;
  }

  private boolean isCancelled(ArrivalAndDepartureBean bean) {
    return TransitDataConstants.STATUS_CANCELED.equals(bean.getStatus());
  }

  private boolean hasRealtime(ArrivalAndDepartureBean bean) {
    return (bean.getPredictedArrivalTime() > 0 || bean.getPredictedDepartureTime() > 0)
              && bean.isPredicted();
  }
}
