package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItineraryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private List<LegBean> legs = new ArrayList<LegBean>();

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public List<LegBean> getLegs() {
    return legs;
  }

  public void setLegs(List<LegBean> legs) {
    this.legs = legs;
  }
}
