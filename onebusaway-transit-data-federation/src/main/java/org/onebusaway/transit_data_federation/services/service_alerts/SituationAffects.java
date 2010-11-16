package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffects implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedStop> stops;

  private List<SituationAffectedVehicleJourney> vehicleJourneys;

  public List<SituationAffectedStop> getStops() {
    return stops;
  }

  public void setStops(List<SituationAffectedStop> stops) {
    this.stops = stops;
  }

  public List<SituationAffectedVehicleJourney> getVehicleJourneys() {
    return vehicleJourneys;
  }

  public void setVehicleJourneys(
      List<SituationAffectedVehicleJourney> vehicleJourneys) {
    this.vehicleJourneys = vehicleJourneys;
  }
}
