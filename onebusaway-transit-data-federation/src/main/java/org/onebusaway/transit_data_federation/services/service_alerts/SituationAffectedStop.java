package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class SituationAffectedStop implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId stopId;

  public AgencyAndId getStopId() {
    return stopId;
  }

  public void setStopId(AgencyAndId stopId) {
    this.stopId = stopId;
  }
}
