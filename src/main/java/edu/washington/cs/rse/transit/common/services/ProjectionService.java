package edu.washington.cs.rse.transit.common.services;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.geospatial.CoordinateProjections;
import edu.washington.cs.rse.geospatial.GeoPoint;
import edu.washington.cs.rse.geospatial.ICoordinateProjection;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.List;

public class ProjectionService {

  private static GeometryFactory _factory = new GeometryFactory(
      new PrecisionModel(PrecisionModel.FLOATING), 2285);

  private PointToGeopointAdapter _pointToGeoPointAdapter = new PointToGeopointAdapter();

  private ICoordinateProjection _projection = CoordinateProjections.WA_NORTH_NAD83_4061_FEET;

  /****
   * 
   ****/

  public void setProjection(ICoordinateProjection projection) {
    _projection = projection;
  }

  /*****************************************************************************
   * Location Projection
   ****************************************************************************/

  public Point getLatLonAsPoint(double lat, double lon) {
    CoordinatePoint fcp = new CoordinatePoint(lat, lon, 0);
    IGeoPoint p = _projection.forward(fcp);
    return getGeoPointAsPoint(p);

  }

  public Point getGeoPointAsPoint(IGeoPoint point) {
    return _factory.createPoint(new Coordinate(point.getX(), point.getY(),
        point.getZ()));
  }

  public List<IGeoPoint> getLatLonsAsPoints(List<CoordinatePoint> points) {
    return _projection.forward(points, new ArrayList<IGeoPoint>(points.size()),
        points.size());
  }

  public List<IGeoPoint> getLatLonsAsPoints(Iterable<CoordinatePoint> points,
      int size) {
    return _projection.forward(points, new ArrayList<IGeoPoint>(size), size);
  }

  public IGeoPoint getPointAsGeoPoint(Point point) {
    return _pointToGeoPointAdapter.adapt(point);
  }

  public IGeoPoint getLocationAsGeoPoint(double x, double y) {
    return new GeoPoint(_projection, x, y, 0.0);
  }

  public CoordinatePoint getPointAsLatLong(Point p) {
    IGeoPoint gp = getLocationAsGeoPoint(p.getX(), p.getY());
    return gp.getCoordinates();
  }

  public List<CoordinatePoint> getPointsAsLatLongs(Iterable<Point> points,
      int size) {
    Iterable<IGeoPoint> geoPoints = AdapterLibrary.adapt(points,
        _pointToGeoPointAdapter);
    List<CoordinatePoint> cPoints = new ArrayList<CoordinatePoint>(size);
    _projection.reverse(geoPoints, cPoints, size);
    return cPoints;
  }

  /****
   * 
   ****/

  private final class PointToGeopointAdapter implements
      IAdapter<Point, IGeoPoint> {
    public IGeoPoint adapt(Point source) {
      return getLocationAsGeoPoint(source.getX(), source.getY());
    }
  }
}
