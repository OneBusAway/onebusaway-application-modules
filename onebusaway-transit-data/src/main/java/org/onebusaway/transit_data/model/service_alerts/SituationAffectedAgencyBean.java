package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class SituationAffectedAgencyBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }  
}
