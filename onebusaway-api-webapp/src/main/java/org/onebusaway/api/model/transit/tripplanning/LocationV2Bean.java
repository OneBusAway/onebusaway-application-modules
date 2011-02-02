package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class LocationV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  private CoordinatePoint location;

  private String stopId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
}
