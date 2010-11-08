package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectsV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedVehicleJourneyV2Bean> vehicleJourneys;

  public List<SituationAffectedVehicleJourneyV2Bean> getVehicleJourneys() {
    return vehicleJourneys;
  }

  public void setVehicleJourneys(
      List<SituationAffectedVehicleJourneyV2Bean> vehicleJourneys) {
    this.vehicleJourneys = vehicleJourneys;
  }
}
