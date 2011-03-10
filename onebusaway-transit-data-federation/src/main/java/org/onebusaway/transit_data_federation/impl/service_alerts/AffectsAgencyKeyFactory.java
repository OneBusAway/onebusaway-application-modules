package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedAgency;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsAgencyKeyFactory implements AffectsKeyFactory<String> {

  public static final AffectsAgencyKeyFactory INSTANCE = new AffectsAgencyKeyFactory();

  @Override
  public Set<String> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    List<SituationAffectedAgency> agencies = affects.getAgencies();

    if (CollectionsLibrary.isEmpty(agencies))
      return Collections.emptySet();

    Set<String> agencyIds = new HashSet<String>();

    for (SituationAffectedAgency agency : agencies)
      agencyIds.add(agency.getAgencyId());
    return agencyIds;
  }
}
