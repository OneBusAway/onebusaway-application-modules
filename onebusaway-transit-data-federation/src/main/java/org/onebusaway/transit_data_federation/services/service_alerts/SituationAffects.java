package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffects implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedAgency> agencies;

  private List<SituationAffectedStop> stops;

  private List<SituationAffectedVehicleJourney> vehicleJourneys;

  private List<SituationAffectedApplication> applications;

  public List<SituationAffectedAgency> getAgencies() {
    return agencies;
  }

  public void setAgencies(List<SituationAffectedAgency> agencies) {
    this.agencies = agencies;
  }

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

  public List<SituationAffectedApplication> getApplications() {
    return applications;
  }

  public void setApplications(List<SituationAffectedApplication> applications) {
    this.applications = applications;
  }
}
