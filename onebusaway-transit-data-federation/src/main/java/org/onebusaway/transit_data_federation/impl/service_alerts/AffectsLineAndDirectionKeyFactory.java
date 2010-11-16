package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsLineAndDirectionKeyFactory implements
    AffectsKeyFactory<LineAndDirectionRef> {

  public static final AffectsLineAndDirectionKeyFactory INSTANCE = new AffectsLineAndDirectionKeyFactory();

  @Override
  public Set<LineAndDirectionRef> getKeysForAffects(Situation situation,
      SituationAffects affects) {

    List<SituationAffectedVehicleJourney> journeys = affects.getVehicleJourneys();

    if (CollectionsLibrary.isEmpty(journeys))
      return Collections.emptySet();

    Set<LineAndDirectionRef> lineIds = new HashSet<LineAndDirectionRef>();

    for (SituationAffectedVehicleJourney journey : journeys) {
      if (journey.getLineId() == null || journey.getDirection() == null)
        continue;
      LineAndDirectionRef ref = new LineAndDirectionRef(journey.getLineId(),
          journey.getDirection());
      lineIds.add(ref);
    }

    return lineIds;
  }
}
