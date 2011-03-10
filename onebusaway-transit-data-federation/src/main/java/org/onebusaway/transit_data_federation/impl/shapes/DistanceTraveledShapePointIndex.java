/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

import java.util.Arrays;

public class DistanceTraveledShapePointIndex implements ShapePointIndex {

  private double _shapeDistanceTraveled;

  public DistanceTraveledShapePointIndex(double shapeDistanceTraveled) {
    _shapeDistanceTraveled = shapeDistanceTraveled;
  }

  @Override
  public int getIndex(ShapePoints points) {
    points.ensureDistTraveled();
    int index = Arrays.binarySearch(points.getDistTraveled(),
        _shapeDistanceTraveled);
    if (index < 0)
      index = -(index + 1);
    return index;
  }

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    
    int n = points.getSize();
    
    if( n == 0)
      throw new IndexOutOfBoundsException();
    
    int index = getIndex(points);
    
    double[] lats = points.getLats();
    double[] lons = points.getLons();
    double[] dist = points.getDistTraveled();
    
    if( index == 0)
      return new CoordinatePoint(lats[0],lons[0]);
    if( index == n)
      return new CoordinatePoint(lats[n-1],lons[n-1]);
    
    double ratio = (_shapeDistanceTraveled - dist[index-1]) / (dist[index] - dist[index-1]);
    double lat = ratio * (lats[index]-lats[index-1]) + lats[index-1];
    double lon = ratio * (lons[index]-lons[index-1]) + lons[index-1];
    return new CoordinatePoint(lat,lon);
  }
}