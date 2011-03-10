package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.List;

public class StopScheduleV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long date;

  private String stopId;

  private List<StopRouteScheduleV2Bean> stopRouteSchedules;

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public List<StopRouteScheduleV2Bean> getStopRouteSchedules() {
    return stopRouteSchedules;
  }

  public void setStopRouteSchedules(
      List<StopRouteScheduleV2Bean> stopRouteSchedules) {
    this.stopRouteSchedules = stopRouteSchedules;
  }
}
