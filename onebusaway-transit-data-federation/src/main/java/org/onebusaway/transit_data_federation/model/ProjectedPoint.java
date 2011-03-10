package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.io.Serializable;

/**
 * Simple point class that keeps both the global lat-lon representation and the
 * projected x-y representation for faster distance calculations.
 * 
 * @author bdferris
 * 
 */
public class ProjectedPoint implements Serializable {

  private static final long serialVersionUID = 1L;

  private final double lat;

  private final double lon;

  private final double x;

  private final double y;

  private final int srid;

  public ProjectedPoint(double lat, double lon, double x, double y, int srid) {
    this.lat = lat;
    this.lon = lon;
    this.x = x;
    this.y = y;
    this.srid = srid;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public CoordinatePoint toCoordinatePoint() {
    return new CoordinatePoint(lat, lon);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public int getSrid() {
    return srid;
  }

  public double distance(ProjectedPoint p) {
    if (this.srid == p.srid)
      return Math.sqrt(p2(p.x - this.x) + p2(p.y - this.y));
    return SphericalGeometryLibrary.distance(this.lat, this.lon, p.lat, p.lon);
  }

  private static final double p2(double x) {
    return x * x;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(lat);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + srid;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProjectedPoint other = (ProjectedPoint) obj;
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
      return false;
    if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
      return false;
    if (srid != other.srid)
      return false;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
      return false;
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return lat + " " + lon + " " + x + " " + y + " " + srid;
  }
}
