package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedVehicleJourneyBean> vehicleJourneys;

  public List<SituationAffectedVehicleJourneyBean> getVehicleJourneys() {
    return vehicleJourneys;
  }

  public void setVehicleJourneys(
      List<SituationAffectedVehicleJourneyBean> vehicleJourneys) {
    this.vehicleJourneys = vehicleJourneys;
  }
}
