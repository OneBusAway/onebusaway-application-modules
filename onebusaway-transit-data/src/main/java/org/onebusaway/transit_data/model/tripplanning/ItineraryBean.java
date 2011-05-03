package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItineraryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private double probability;

  private List<LegBean> legs = new ArrayList<LegBean>();

  private boolean selected = false;

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

  public double getProbability() {
    return probability;
  }

  public void setProbability(double probability) {
    this.probability = probability;
  }

  public List<LegBean> getLegs() {
    return legs;
  }

  public void setLegs(List<LegBean> legs) {
    this.legs = legs;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
