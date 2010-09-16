package org.onebusaway.geospatial.services;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

public class SphericalGeometryLibrary {

  public static final double RADIUS_OF_EARTH_IN_KM = 6371.01;

  public static final double COS_MAX_LAT = Math.cos(46 * Math.PI / 180);

  public static final double METERS_PER_DEGREE_AT_EQUATOR = 111319.9;

  /**
   * This method is fast but not very accurate
   * 
   * @param lat1
   * @param lon1
   * @param lat2
   * @param lon2
   * @return
   */
  public static double distanceFaster(double lat1, double lon1, double lat2,
      double lon2) {
    double lonDelta = lon2 - lon1;
    double latDelta = lat2 - lat1;
    return Math.sqrt(lonDelta * lonDelta + latDelta * latDelta)
        * METERS_PER_DEGREE_AT_EQUATOR * COS_MAX_LAT;
  }

  public static final double distance(double lat1, double lon1, double lat2,
      double lon2) {
    return distance(lat1, lon1, lat2, lon2, RADIUS_OF_EARTH_IN_KM * 1000);
  }

  public static final double distance(CoordinatePoint a, CoordinatePoint b) {
    return distance(a.getLat(), a.getLon(), b.getLat(), b.getLon());
  }

  public static final double distance(double lat1, double lon1, double lat2,
      double lon2, double radius) {

    // http://en.wikipedia.org/wiki/Great-circle_distance
    lat1 = toRadians(lat1); // Theta-s
    lon1 = toRadians(lon1); // Lambda-s
    lat2 = toRadians(lat2); // Theta-f
    lon2 = toRadians(lon2); // Lambda-f

    double deltaLon = lon2 - lon1;

    double y = sqrt(p2(cos(lat2) * sin(deltaLon))
        + p2(cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)));
    double x = sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(deltaLon);

    return radius * atan2(y, x);
  }

  public static final CoordinateBounds bounds(CoordinatePoint point,
      double distance) {
    return bounds(point.getLat(), point.getLon(), distance);
  }

  public static CoordinateBounds bounds(CoordinateBounds b, double distance) {
    CoordinateBounds b2 = bounds(b.getMinLat(), b.getMinLon(), distance);
    CoordinateBounds b3 = bounds(b.getMaxLat(), b.getMaxLon(), distance);
    b2.addBounds(b3);
    return b2;
  }

  public static final CoordinateBounds bounds(double lat, double lon,
      double distance) {
    return bounds(lat, lon, distance, distance);
  }

  public static final CoordinateBounds bounds(double lat, double lon,
      double latDistance, double lonDistance) {

    double radiusOfEarth = RADIUS_OF_EARTH_IN_KM * 1000;

    double latRadians = toRadians(lat);
    double lonRadians = toRadians(lon);

    double latRadius = radiusOfEarth;
    double lonRadius = Math.cos(latRadians) * radiusOfEarth;

    double latOffset = latDistance / latRadius;
    double lonOffset = lonDistance / lonRadius;

    double latFrom = toDegrees(latRadians - latOffset);
    double latTo = toDegrees(latRadians + latOffset);

    double lonFrom = toDegrees(lonRadians - lonOffset);
    double lonTo = toDegrees(lonRadians + lonOffset);

    return new CoordinateBounds(latFrom, lonFrom, latTo, lonTo);
  }

  /**
   * 
   * @param lat
   * @param lon
   * @param latOffset
   * @param lonOffset
   * @return 
   *         CoordinateBounds(lat-latOffser,lon-lonOffset,lat+latOffset,lon+lonOffset
   *         )
   */
  public static final CoordinateBounds boundsFromLatLonOffset(double lat,
      double lon, double latOffset, double lonOffset) {
    double latFrom = lat - latOffset;
    double latTo = lat + latOffset;
    double lonFrom = lon - lonOffset;
    double lonTo = lon + lonOffset;
    return new CoordinateBounds(latFrom, lonFrom, latTo, lonTo);
  }

  public static final CoordinateBounds boundsFromLatLonSpan(double lat,
      double lon, double latSpan, double lonSpan) {
    return boundsFromLatLonOffset(lat, lon, latSpan / 2, lonSpan / 2);
  }

  public static CoordinatePoint getCenterOfBounds(CoordinateBounds b) {
    return new CoordinatePoint((b.getMinLat() + b.getMaxLat()) / 2,
        (b.getMinLon() + b.getMaxLon()) / 2);
  }

  private static final double p2(double a) {
    return a * a;
  }

}
