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
