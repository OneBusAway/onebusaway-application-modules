package org.onebusaway.geospatial.grid;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;

import java.util.ArrayList;
import java.util.List;

public class GridFactory {

  private double _gridSize;

  protected Grid<Object> _grid = new MapGrid<Object>();

  public GridFactory(double gridSize) {
    _gridSize = gridSize;
  }

  public List<EncodedPolygonBean> getBoundary() {
    BoundaryFactory factory = new BoundaryFactory();
    List<Boundary> boundaries = factory.getBoundaries(_grid);
    return getBoundariesAsBeans(boundaries);
  }

  public void addBounds(CoordinateBounds bounds) {

    int latIndexMin = (int) Math.floor(bounds.getMinLat() / _gridSize);
    int latIndexMax = (int) Math.ceil(bounds.getMaxLat() / _gridSize);
    int lonIndexMin = (int) Math.floor(bounds.getMinLon() / _gridSize);
    int lonIndexMax = (int) Math.ceil(bounds.getMaxLon() / _gridSize);

    for (int latIndex = latIndexMin; latIndex < latIndexMax; latIndex++) {
      for (int lonIndex = lonIndexMin; lonIndex < lonIndexMax; lonIndex++) {
        GridIndex index = new GridIndex(lonIndex, latIndex);
        addCell(index, this);
      }
    }
  }

  public List<CoordinateBounds> getGrid() {
    List<CoordinateBounds> results = new ArrayList<CoordinateBounds>();
    for (Grid.Entry<Object> entry : _grid.getEntries()) {
      GridIndex index = entry.getIndex();
      CoordinateBounds bounds = getIndexAsBounds(index);
      results.add(bounds);
    }
    return results;
  }

  protected CoordinateBounds getIndexAsBounds(GridIndex index) {
    double minLat = index.getY() * _gridSize;
    double minLon = index.getX() * _gridSize;
    double maxLat = minLat + _gridSize;
    double maxLon = minLon + _gridSize;
    return new CoordinateBounds(minLat, minLon, maxLat, maxLon);
  }

  protected void getIndexAndDirectionAsPoint(GridIndex index,
      EDirection direction, DoublePoint point) {

    double minLat = index.getY() * _gridSize;
    double minLon = index.getX() * _gridSize;
    double maxLat = minLat + _gridSize;
    double maxLon = minLon + _gridSize;

    switch (direction) {
      case UP:
        point.lat = maxLat;
        point.lon = minLon;
        break;
      case RIGHT:
        point.lat = maxLat;
        point.lon = maxLon;
        break;
      case DOWN:
        point.lat = minLat;
        point.lon = maxLon;
        break;
      case LEFT:
        point.lat = minLat;
        point.lon = minLon;
        break;
      default:
        throw new IllegalStateException();
    }
  }

  protected void addCell(GridIndex index, Object value) {
    _grid.set(index, value);
  }

  protected List<EncodedPolygonBean> getBoundariesAsBeans(
      List<Boundary> boundaries) {
    List<EncodedPolygonBean> beans = new ArrayList<EncodedPolygonBean>(
        boundaries.size());
    for (Boundary boundary : boundaries) {
      EncodedPolygonBean bean = getBoundaryAsPolygonBean(boundary);
      beans.add(bean);
    }
    return beans;
  }

  protected EncodedPolygonBean getBoundaryAsPolygonBean(Boundary boundary) {
    EncodedPolygonBean bean = new EncodedPolygonBean();
    EncodedPolylineBean outerRing = getPathAsEncodedPath(boundary.getOuterBoundary());
    bean.setOuterRing(outerRing);
    for (BoundaryPath path : boundary.getInnerBoundaries()) {
      EncodedPolylineBean innerRing = getPathAsEncodedPath(path);
      bean.addInnerRing(innerRing);
    }
    return bean;
  }

  protected EncodedPolylineBean getPathAsEncodedPath(BoundaryPath path) {
    DoublePoint p = new DoublePoint();
    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>(path.size());
    for (int i = 0; i < path.size(); i++) {
      getIndexAndDirectionAsPoint(path.getIndex(i), path.getDirection(i), p);
      CoordinatePoint cp = new CoordinatePoint(p.lat, p.lon);
      points.add(cp);
    }

    // Re-add the first point to close the loop
    // I don't think this is actually right
    // points.add(points.get(0));

    return PolylineEncoder.createEncodings(points);
  }

  protected static class DoublePoint {
    double lat;
    double lon;
  }
}
