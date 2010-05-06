package org.onebusaway.geospatial.grid;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TimedGridFactoryTest {

  // That's 2.5 mph
  private static final double WALKING_SPEED_METERS_PER_MS = 0.0011176;

  @Test
  public void test() {

    CoordinatePoint center = new CoordinatePoint(47.653247216758494, -122.30838775634766);
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        center.getLat(),center.getLon(), 400);
    double delta = (bounds.getMaxLat() - bounds.getMinLat()) / 2;
    System.out.println(delta);

    TimedGridFactory factory = new TimedGridFactory(delta, WALKING_SPEED_METERS_PER_MS);

    factory.addPoint(center.getLat(),center.getLon(), 0, 300*1000);
    
    System.out.println(WALKING_SPEED_METERS_PER_MS*120*1000);

    Map<Integer, List<EncodedPolygonBean>> polygonsByTime = factory.getPolygonsByTime(1);

    for (Map.Entry<Integer, List<EncodedPolygonBean>> entry : polygonsByTime.entrySet()) {
      System.out.println("t=" + entry.getKey());
      for (EncodedPolygonBean bean : entry.getValue()) {
        EncodedPolylineBean outerRing = bean.getOuterRing();
        System.out.println(outerRing.getPoints() + " " + outerRing.getLength());
      }
    }
  }
}
