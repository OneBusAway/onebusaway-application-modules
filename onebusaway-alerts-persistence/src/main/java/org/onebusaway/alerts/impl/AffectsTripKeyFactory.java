/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.alerts.impl;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.HashSet;
import java.util.Set;

class AffectsTripKeyFactory implements AffectsKeyFactory<AgencyAndId> {

  public static final AffectsTripKeyFactory INSTANCE = new AffectsTripKeyFactory();

  @Override
  public Set<AgencyAndId> getKeysForAffects(ServiceAlertRecord serviceAlert) {

    Set<AgencyAndId> tripIds = new HashSet<AgencyAndId>();

    for (ServiceAlertsSituationAffectsClause affects : serviceAlert.getAllAffects()) {
      if (affects.getTripId() != null
          && !(affects.getDirectionId() != null
              || affects.getRouteId() != null || affects.getStopId() != null)) {
        AgencyAndId tripId = ServiceAlertLibrary.agencyAndIdAndId(serviceAlert.getAgencyId(), affects.getTripId());
        tripIds.add(tripId);
      }
    }

    return tripIds;
  }
}
