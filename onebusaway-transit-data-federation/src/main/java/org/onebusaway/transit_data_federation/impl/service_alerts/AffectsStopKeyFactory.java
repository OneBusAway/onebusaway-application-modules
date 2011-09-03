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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsStopKeyFactory implements AffectsKeyFactory<AgencyAndId> {

  public static final AffectsStopKeyFactory INSTANCE = new AffectsStopKeyFactory();

  @Override
  public Set<AgencyAndId> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    List<SituationAffectedStop> stops = affects.getStops();

    if (CollectionsLibrary.isEmpty(stops))
      return Collections.emptySet();

    Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();

    for (SituationAffectedStop stop : stops)
      stopIds.add(stop.getStopId());
    return stopIds;
  }
}
