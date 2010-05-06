package org.onebusaway.geospatial.model;

import java.io.Serializable;

public final class CoordinatePoint implements Serializable {

  private static final long serialVersionUID = 1L;

  private double lat;

  private double lon;
  
  CoordinatePoint() {
    
  }

  public CoordinatePoint(double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  @Override
  public String toString() {
    return lat + " " + lon;
  }

  @Override
  public int hashCode() {
    return new Double(lat).hashCode() + new Double(lon).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o == null || !(o instanceof CoordinatePoint))
      return false;
    CoordinatePoint p = (CoordinatePoint) o;
    return this.lat == p.lat && this.lon == p.lon;
  }
}
