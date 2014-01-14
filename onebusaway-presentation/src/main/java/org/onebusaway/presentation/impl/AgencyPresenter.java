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
package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

public class AgencyPresenter {

  private static final AgencyNameComparator _agencyNameComparator = new AgencyNameComparator();

  public static List<AgencyBean> getAgenciesForArrivalAndDepartures(
      List<ArrivalAndDepartureBean> arrivalsAndDepartures) {

    Map<String, AgencyBean> agenciesById = new HashMap<String, AgencyBean>();
    for (ArrivalAndDepartureBean aad : arrivalsAndDepartures) {
      AgencyBean agency = aad.getTrip().getRoute().getAgency();
      agenciesById.put(agency.getId(), agency);
    }

    List<AgencyBean> agencies = new ArrayList<AgencyBean>();
    agencies.addAll(agenciesById.values());
    Collections.sort(agencies, _agencyNameComparator);
    return agencies;
  }
}
