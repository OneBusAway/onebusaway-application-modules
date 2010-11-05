package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedRouteBean> routesAndDirections;

  public List<SituationAffectedRouteBean> getRoutesAndDirections() {
    return routesAndDirections;
  }

  public void setRoutesAndDirections(
      List<SituationAffectedRouteBean> routesAndDirections) {
    this.routesAndDirections = routesAndDirections;
  }
}
