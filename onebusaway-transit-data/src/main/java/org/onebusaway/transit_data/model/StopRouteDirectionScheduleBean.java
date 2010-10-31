package org.onebusaway.transit_data.model;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.schedule.FrequencyInstanceBean;

public class StopRouteDirectionScheduleBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private String tripHeadsign;

  private List<StopTimeInstanceBean> stopTimes = new ArrayList<StopTimeInstanceBean>();

  private List<FrequencyInstanceBean> frequencies = new ArrayList<FrequencyInstanceBean>();

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public List<StopTimeInstanceBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<StopTimeInstanceBean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public List<FrequencyInstanceBean> getFrequencies() {
    return frequencies;
  }

  public void setFrequencies(List<FrequencyInstanceBean> frequencies) {
    this.frequencies = frequencies;
  }

}
