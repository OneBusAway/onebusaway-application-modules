package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class SituationAffectedCallBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
}
