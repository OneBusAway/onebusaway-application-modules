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
