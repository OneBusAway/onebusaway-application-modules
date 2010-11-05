package org.onebusaway.transit_data.model.service_alerts;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public class SituationQueryBean {

  private String agencyId;

  private long time;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
