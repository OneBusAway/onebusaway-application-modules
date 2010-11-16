package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.Set;

import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;

public interface AffectsKeyFactory<T> {
  public Set<T> getKeysForAffects(Situation situation, SituationAffects affects);
}
