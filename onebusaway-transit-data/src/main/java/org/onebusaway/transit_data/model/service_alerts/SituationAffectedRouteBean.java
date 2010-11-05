package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public final class SituationAffectedRouteBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String routeId;

  private String directionId;

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }
}
