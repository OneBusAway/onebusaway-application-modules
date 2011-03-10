package org.onebusaway.api.model.transit;

import java.util.ArrayList;
import java.util.List;

public class StopRouteDirectionScheduleV2Bean {

  private static final long serialVersionUID = 1L;

  private String tripHeadsign;

  private List<ScheduleStopTimeInstanceV2Bean> scheduleStopTimes = new ArrayList<ScheduleStopTimeInstanceV2Bean>();

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public List<ScheduleStopTimeInstanceV2Bean> getScheduleStopTimes() {
    return scheduleStopTimes;
  }

  public void setScheduleStopTimes(List<ScheduleStopTimeInstanceV2Bean> stopTimes) {
    this.scheduleStopTimes = stopTimes;
  }
}
