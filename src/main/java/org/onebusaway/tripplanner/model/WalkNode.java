/**
 * 
 */
package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;

public class WalkNode implements Serializable {

  private static final long serialVersionUID = 1L;

  private CoordinatePoint latLon;

  private Point location;

  public WalkNode(CoordinatePoint latLon, Point location) {
    this.latLon = latLon;
    this.location = location;
  }

  public CoordinatePoint getLatLon() {
    return latLon;
  }

  public void setLatLon(CoordinatePoint latLon) {
    this.latLon = latLon;
  }

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

}