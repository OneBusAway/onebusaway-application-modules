package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.utility.InterpolationLibrary;

public class ShapeSupport {

  public static String getFullPath(ShapePoints shapePoints) {
    EncodedPolylineBean bean = PolylineEncoder.createEncodings(
        shapePoints.getLats(), shapePoints.getLons());
    return bean.getPoints();
  }

  public static String getPartialPathToStop(ShapePoints shapePoints,
      StopTimeEntry toStop) {

    int indexTo = toStop.getShapePointIndex();

    double[] lats = new double[indexTo + 2];
    double[] lons = new double[indexTo + 2];

    System.arraycopy(shapePoints.getLats(), 0, lats, 0, indexTo + 1);
    System.arraycopy(shapePoints.getLons(), 0, lons, 0, indexTo + 1);

    CoordinatePoint to = interpolate(shapePoints, toStop);
    lats[indexTo + 1] = to.getLat();
    lons[indexTo + 1] = to.getLon();

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  public static String getPartialPathFromStop(ShapePoints shapePoints,
      StopTimeEntry fromStop) {
    
    int indexFrom = fromStop.getShapePointIndex();
    int indexTo = shapePoints.getSize();
    int size = indexTo - indexFrom;

    double[] lats = new double[size];
    double[] lons = new double[size];

    System.arraycopy(shapePoints.getLats(), indexFrom + 1, lats, 1, size - 1);
    System.arraycopy(shapePoints.getLons(), indexFrom + 1, lons, 1, size - 1);

    CoordinatePoint from = interpolate(shapePoints, fromStop);
    lats[0] = from.getLat();
    lons[0] = from.getLon();

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  public static String getPartialPathBetweenStops(ShapePoints shapePoints,
      StopTimeEntry fromStop, StopTimeEntry toStop) {

    int indexFrom = fromStop.getShapePointIndex();
    int indexTo = toStop.getShapePointIndex();
    int size = indexTo - indexFrom + 2;

    double[] lats = new double[size];
    double[] lons = new double[size];

    System.arraycopy(shapePoints.getLats(), indexFrom + 1, lats, 1, size - 2);
    System.arraycopy(shapePoints.getLons(), indexFrom + 1, lons, 1, size - 2);

    CoordinatePoint from = interpolate(shapePoints, fromStop);
    lats[0] = from.getLat();
    lons[0] = from.getLon();

    CoordinatePoint to = interpolate(shapePoints, toStop);
    lats[size - 1] = to.getLat();
    lons[size - 1] = to.getLon();

    EncodedPolylineBean bean = PolylineEncoder.createEncodings(lats, lons);
    return bean.getPoints();
  }

  private static CoordinatePoint interpolate(ShapePoints shapePoints,
      StopTimeEntry stop) {

    double[] lats = shapePoints.getLats();
    double[] lons = shapePoints.getLons();
    double[] distances = shapePoints.getDistTraveled();

    int index = stop.getShapePointIndex();

    if (index + 1 == lats.length)
      return new CoordinatePoint(lats[index], lons[index]);

    double d1 = distances[index];
    double d2 = distances[index + 1];

    if (d1 == d2)
      return new CoordinatePoint(lats[index], lons[index]);

    double ratio = (stop.getShapeDistTraveled() - d1) / (d2 - d1);

    double lat = InterpolationLibrary.interpolatePair(lats[index],
        lats[index + 1], ratio);
    double lon = InterpolationLibrary.interpolatePair(lons[index],
        lons[index + 1], ratio);

    return new CoordinatePoint(lat, lon);
  }
}
