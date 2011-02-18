package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

public class StopRouteScheduleV2Bean {

  private static final long serialVersionUID = 1L;

  private String routeId;

  private List<StopRouteDirectionScheduleV2Bean> stopRouteDirectionSchedules = new ArrayList<StopRouteDirectionScheduleV2Bean>();

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public List<StopRouteDirectionScheduleV2Bean> getStopRouteDirectionSchedules() {
    return stopRouteDirectionSchedules;
  }

  public void setStopRouteDirectionSchedules(List<StopRouteDirectionScheduleV2Bean> stopRouteDirectionSchedules) {
    this.stopRouteDirectionSchedules = stopRouteDirectionSchedules;
  }
}
