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

import java.util.HashSet;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsSituationAgencyKeyFactory implements AffectsKeyFactory<String> {

  public static final AffectsSituationAgencyKeyFactory INSTANCE = new AffectsSituationAgencyKeyFactory();

  @Override
  public Set<String> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    Set<String> v = new HashSet<String>();
    if( situation != null)
      v.add(situation.getId().getAgencyId());
    return v;
  }

}
