package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class LegBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private CoordinatePoint from;

  private CoordinatePoint to;

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

  public CoordinatePoint getFrom() {
    return from;
  }

  public void setFrom(CoordinatePoint from) {
    this.from = from;
  }

  public CoordinatePoint getTo() {
    return to;
  }

  public void setTo(CoordinatePoint to) {
    this.to = to;
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
