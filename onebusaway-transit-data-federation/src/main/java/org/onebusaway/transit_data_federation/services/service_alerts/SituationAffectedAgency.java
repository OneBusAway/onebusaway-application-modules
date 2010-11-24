package org.onebusaway.transit_data_federation.services.service_alerts;

import java.io.Serializable;

public class SituationAffectedAgency implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }
}
