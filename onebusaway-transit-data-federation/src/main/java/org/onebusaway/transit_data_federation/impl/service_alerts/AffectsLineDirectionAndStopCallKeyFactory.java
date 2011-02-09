package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public class AffectsLineDirectionAndStopCallKeyFactory implements
    AffectsKeyFactory<LineDirectionAndStopCallRef> {

  public static final AffectsLineDirectionAndStopCallKeyFactory INSTANCE = new AffectsLineDirectionAndStopCallKeyFactory();

  @Override
  public Set<LineDirectionAndStopCallRef> getKeysForAffects(
      Situation situation, SituationAffects affects) {

    List<SituationAffectedVehicleJourney> journeys = affects.getVehicleJourneys();

    if (CollectionsLibrary.isEmpty(journeys))
      return Collections.emptySet();

    Set<LineDirectionAndStopCallRef> keys = new HashSet<LineDirectionAndStopCallRef>();

    for (SituationAffectedVehicleJourney journey : journeys) {
      if (isActivated(journey)) {
        for (SituationAffectedCall call : journey.getCalls()) {
          LineDirectionAndStopCallRef ref = new LineDirectionAndStopCallRef(
              journey.getLineId(), journey.getDirection(), call.getStopId());
          keys.add(ref);
        }
      }
    }

    return keys;
  }

  private boolean isActivated(SituationAffectedVehicleJourney journey) {
    return journey.getLineId() != null && journey.getDirection() != null
        && !CollectionsLibrary.isEmpty(journey.getCalls());
  }

}
