package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public class SituationQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

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
