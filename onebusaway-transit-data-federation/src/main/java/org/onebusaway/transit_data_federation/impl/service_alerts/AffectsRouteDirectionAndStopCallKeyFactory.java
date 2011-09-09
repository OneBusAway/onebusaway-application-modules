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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.HashSet;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Affects;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;

class AffectsRouteDirectionAndStopCallKeyFactory implements
    AffectsKeyFactory<RouteDirectionAndStopCallRef> {

  public static final AffectsRouteDirectionAndStopCallKeyFactory INSTANCE = new AffectsRouteDirectionAndStopCallKeyFactory();

  @Override
  public Set<RouteDirectionAndStopCallRef> getKeysForAffects(
      ServiceAlert serviceAlert) {

    Set<RouteDirectionAndStopCallRef> keys = new HashSet<RouteDirectionAndStopCallRef>();

    for (Affects affects : serviceAlert.getAffectsList()) {
      if (affects.hasRouteId() && affects.hasDirectionId()
          && affects.hasStopId()
          && !(affects.hasAgencyId() || affects.hasTripId())) {

        AgencyAndId routeId = ServiceAlertLibrary.agencyAndId(affects.getRouteId());
        String directionId = affects.getDirectionId();
        AgencyAndId stopId = ServiceAlertLibrary.agencyAndId(affects.getStopId());
        RouteDirectionAndStopCallRef ref = new RouteDirectionAndStopCallRef(
            routeId, directionId, stopId);
        keys.add(ref);
      }
    }

    return keys;
  }
}
