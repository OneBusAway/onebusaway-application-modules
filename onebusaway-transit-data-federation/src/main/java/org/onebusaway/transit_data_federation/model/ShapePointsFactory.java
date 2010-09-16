package org.onebusaway.transit_data_federation.model;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;

public class ShapePointsFactory {

  private AgencyAndId _shapeId;

  private List<CoordinatePoint> _points = new ArrayList<CoordinatePoint>();

  public void setShapeId(AgencyAndId shapeId) {
    _shapeId = shapeId;
  }

  public void addPoint(double lat, double lon) {
    CoordinatePoint point = new CoordinatePoint(lat, lon);
    /*
     * if( ! _points.isEmpty() ) { CoordinatePoint prev =
     * _points.get(_points.size()-1); if( prev.equals(point)) return; }
     */
    _points.add(point);
  }

  public void addPoints(ShapePoints shapePoints) {
    double[] lats = shapePoints.getLats();
    double[] lons = shapePoints.getLons();
    for (int i = 0; i < lats.length; i++)
      addPoint(lats[i], lons[i]);
  }

  public ShapePoints create() {
    ShapePoints shapePoints = new ShapePoints();
    shapePoints.setShapeId(_shapeId);

    double[] lats = new double[_points.size()];
    double[] lons = new double[_points.size()];
    double[] distances = new double[_points.size()];
    for (int i = 0; i < _points.size(); i++) {
      CoordinatePoint p = _points.get(i);
      lats[i] = p.getLat();
      lons[i] = p.getLon();
    }
    shapePoints.setLats(lats);
    shapePoints.setLons(lons);
    shapePoints.setDistTraveled(distances);
    shapePoints.ensureDistTraveled();
    return shapePoints;
  }

}
