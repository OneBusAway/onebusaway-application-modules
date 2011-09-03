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
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsStopCallKeyFactory implements
    AffectsKeyFactory<LineAndDirectionRef> {

  public static final AffectsStopCallKeyFactory INSTANCE = new AffectsStopCallKeyFactory();

  @Override
  public Set<LineAndDirectionRef> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    List<SituationAffectedVehicleJourney> journeys = affects.getVehicleJourneys();

    if (CollectionsLibrary.isEmpty(journeys))
      return Collections.emptySet();

    Set<LineAndDirectionRef> lineIds = new HashSet<LineAndDirectionRef>();

    for (SituationAffectedVehicleJourney journey : journeys) {
      if (journey.getLineId() == null || journey.getDirectionId() == null)
        continue;
      LineAndDirectionRef ref = new LineAndDirectionRef(journey.getLineId(),
          journey.getDirectionId());
      lineIds.add(ref);
    }

    return lineIds;
  }
}
