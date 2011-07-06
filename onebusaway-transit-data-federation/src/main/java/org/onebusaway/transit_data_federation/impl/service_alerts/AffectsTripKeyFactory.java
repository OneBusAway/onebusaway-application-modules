package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsTripKeyFactory implements AffectsKeyFactory<AgencyAndId> {

  public static final AffectsTripKeyFactory INSTANCE = new AffectsTripKeyFactory();

  @Override
  public Set<AgencyAndId> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    List<SituationAffectedVehicleJourney> journeys = affects.getVehicleJourneys();

    if (CollectionsLibrary.isEmpty(journeys))
      return Collections.emptySet();

    Set<AgencyAndId> tripIds = new HashSet<AgencyAndId>();

    for (SituationAffectedVehicleJourney journey : journeys) {
      if (isActivated(journey))
        tripIds.addAll(journey.getTripIds());
    }

    return tripIds;
  }

  private boolean isActivated(SituationAffectedVehicleJourney journey) {
    return journey.getLineId() == null && journey.getDirectionId() == null
        && CollectionsLibrary.isEmpty(journey.getCalls())
        && !CollectionsLibrary.isEmpty(journey.getTripIds());
  }
}
