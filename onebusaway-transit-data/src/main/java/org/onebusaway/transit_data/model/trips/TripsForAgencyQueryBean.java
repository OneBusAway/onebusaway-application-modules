package org.onebusaway.transit_data.model.trips;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripsForAgencyQueryBean extends AbstractTripsQueryBean {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }
}
