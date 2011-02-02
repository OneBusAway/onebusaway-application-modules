package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;

public class StreetLegV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String streetName;

  private String path;

  private double distance;

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }
}
