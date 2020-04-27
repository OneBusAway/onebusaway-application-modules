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

import java.util.HashSet;
import java.util.Set;

class AffectsAgencyKeyFactory implements AffectsKeyFactory<String> {

  public static final AffectsAgencyKeyFactory INSTANCE = new AffectsAgencyKeyFactory();

  @Override
  public Set<String> getKeysForAffects(ServiceAlertRecord serviceAlert) {

    Set<String> agencyIds = new HashSet<String>();

    for (ServiceAlertsSituationAffectsClause affects : serviceAlert.getAllAffects()) {
      // this logic ensure this is an agency only affect, no other affects are set
      if (affects.getAgencyId() != null
          && !(affects.getDirectionId() != null || affects.getRouteId() != null
              || affects.getStopId() != null || affects.getTripId() != null)) {
        agencyIds.add(affects.getAgencyId());
      }
    }

    return agencyIds;
  }
}
