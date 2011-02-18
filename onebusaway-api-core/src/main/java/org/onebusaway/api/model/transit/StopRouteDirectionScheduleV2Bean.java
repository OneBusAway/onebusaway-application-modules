package org.onebusaway.api.model.transit;

import java.util.List;

public class StopRouteDirectionScheduleV2Bean {

  private static final long serialVersionUID = 1L;

  private String tripHeadsign;

  private List<ScheduleStopTimeInstanceV2Bean> scheduleStopTimes;
  
  private List<ScheduleFrequencyInstanceV2Bean> scheduleFrequencies;

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

  public List<ScheduleFrequencyInstanceV2Bean> getScheduleFrequencies() {
    return scheduleFrequencies;
  }

  public void setScheduleFrequencies(
      List<ScheduleFrequencyInstanceV2Bean> scheduleFrequencies) {
    this.scheduleFrequencies = scheduleFrequencies;
  }
}
