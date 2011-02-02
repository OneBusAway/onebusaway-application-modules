package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.List;

public class LegBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private double distance;

  private String mode;

  private TransitLegBean transitLeg;

  private List<StreetLegBean> streetLegs;

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

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public TransitLegBean getTransitLeg() {
    return transitLeg;
  }

  public void setTransitLeg(TransitLegBean transitLeg) {
    this.transitLeg = transitLeg;
  }

  public List<StreetLegBean> getStreetLegs() {
    return streetLegs;
  }

  public void setStreetLegs(List<StreetLegBean> streetLegs) {
    this.streetLegs = streetLegs;
  }
}
