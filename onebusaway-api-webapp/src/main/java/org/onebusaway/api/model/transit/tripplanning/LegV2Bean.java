package org.onebusaway.api.model.transit.tripplanning;

import java.util.List;

public class LegV2Bean {

  private long startTime;

  private long endTime;

  private double distance;

  private String mode;

  private TransitLegV2Bean transitLeg;

  private List<StreetLegV2Bean> streetLegs;

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

  public TransitLegV2Bean getTransitLeg() {
    return transitLeg;
  }

  public void setTransitLeg(TransitLegV2Bean transitLeg) {
    this.transitLeg = transitLeg;
  }

  public List<StreetLegV2Bean> getStreetLegs() {
    return streetLegs;
  }

  public void setStreetLegs(List<StreetLegV2Bean> streetLegs) {
    this.streetLegs = streetLegs;
  }
}
