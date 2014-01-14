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

class AffectsTripAndStopKeyFactory implements
    AffectsKeyFactory<TripAndStopCallRef> {

  public static final AffectsTripAndStopKeyFactory INSTANCE = new AffectsTripAndStopKeyFactory();

  @Override
  public Set<TripAndStopCallRef> getKeysForAffects(ServiceAlert serviceAlert) {

    Set<TripAndStopCallRef> refs = new HashSet<TripAndStopCallRef>();

    for (Affects affects : serviceAlert.getAffectsList()) {
      if (affects.hasTripId()
          && affects.hasStopId()
          && !(affects.hasAgencyId() || affects.hasDirectionId() || affects.hasRouteId())) {
        AgencyAndId tripId = ServiceAlertLibrary.agencyAndId(affects.getTripId());
        AgencyAndId stopId = ServiceAlertLibrary.agencyAndId(affects.getStopId());
        TripAndStopCallRef ref = new TripAndStopCallRef(tripId, stopId);
        refs.add(ref);
      }
    }

    return refs;
  }
}
