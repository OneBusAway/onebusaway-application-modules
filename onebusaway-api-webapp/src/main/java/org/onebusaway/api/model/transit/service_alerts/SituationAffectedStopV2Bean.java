package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;

public final class SituationAffectedStopV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
}
