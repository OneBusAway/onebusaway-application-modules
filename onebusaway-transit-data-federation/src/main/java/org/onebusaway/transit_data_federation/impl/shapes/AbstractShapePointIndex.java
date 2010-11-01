package org.onebusaway.transit_data_federation.impl.shapes;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public abstract class AbstractShapePointIndex implements ShapePointIndex {

  @Override
  public CoordinatePoint getPoint(ShapePoints points) {
    return getPointAndOrientation(points).getPoint();
  }

  protected PointAndOrientation computePointAndOrientation(
      ShapePoints shapePoints, int pointIndex, int orientationIndexFrom,
      int orientationIndexTo) {

    double lat = shapePoints.getLatForIndex(pointIndex);
    double lon = shapePoints.getLonForIndex(pointIndex);

    double orientation = computeOrientation(shapePoints, orientationIndexFrom,
        orientationIndexTo);

    return new PointAndOrientation(lat, lon, orientation);
  }

  protected double computeOrientation(ShapePoints shapePoints, int indexFrom,
      int indexTo) {

    if (indexFrom == indexTo)
      return Double.NaN;

    double latFrom = shapePoints.getLatForIndex(indexFrom);
    double lonFrom = shapePoints.getLonForIndex(indexFrom);
    double latTo = shapePoints.getLatForIndex(indexTo);
    double lonTo = shapePoints.getLonForIndex(indexTo);
    return SphericalGeometryLibrary.getOrientation(latFrom, lonFrom, latTo,
        lonTo);
  }
}
