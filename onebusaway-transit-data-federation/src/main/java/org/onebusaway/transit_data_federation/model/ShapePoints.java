package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

public class ShapePoints implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId shapeId;

  private double[] lats;

  private double[] lons;

  private double[] distTraveled;

  public int getSize() {
    return lats.length;
  }
  
  public boolean isEmpty() {
    return getSize() == 0;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  public double[] getLats() {
    return lats;
  }

  public void setLats(double[] lat) {
    this.lats = lat;
  }

  public double[] getLons() {
    return lons;
  }

  public void setLons(double[] lon) {
    this.lons = lon;
  }

  public double[] getDistTraveled() {
    return distTraveled;
  }

  public void setDistTraveled(double[] distTraveled) {
    this.distTraveled = distTraveled;
  }

  public void ensureDistTraveled() {

    if (distTraveled == null || distTraveled.length == 0)
      return;

    int n = distTraveled.length;

    if (distTraveled[n-1] > 0)
      return;
    
    double totalDistanceTraveled = 0;
    double prevLat = lats[0];
    double prevLon = lons[0];
    
    for( int i=1; i<n; i++) {
      double curLat = lats[i];
      double curLon = lons[i];
      totalDistanceTraveled += SphericalGeometryLibrary.distance(prevLat, prevLon, curLat, curLon);
      distTraveled[i] = totalDistanceTraveled;
      prevLat = curLat;
      prevLon = curLon;
    }
  }
}
