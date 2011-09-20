/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.geospatial.grid;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;

import java.util.ArrayList;
import java.util.List;

public class GridFactory {

  private double _gridLatStep;

  private double _gridLonStep;

  protected Grid<Object> _grid = new MapGrid<Object>();

  public GridFactory(double gridLatStep, double gridLonStep) {
    _gridLatStep = gridLatStep;
    _gridLonStep = gridLonStep;
  }

  public List<EncodedPolygonBean> getBoundary() {
    BoundaryFactory factory = new BoundaryFactory();
    List<Boundary> boundaries = factory.getBoundaries(_grid);
    return getBoundariesAsBeans(boundaries);
  }

  public void addBounds(CoordinateBounds bounds) {

    int latIndexMin = (int) Math.floor(bounds.getMinLat() / _gridLatStep);
    int latIndexMax = (int) Math.ceil(bounds.getMaxLat() / _gridLatStep);
    int lonIndexMin = (int) Math.floor(bounds.getMinLon() / _gridLonStep);
    int lonIndexMax = (int) Math.ceil(bounds.getMaxLon() / _gridLonStep);

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
    double minLat = index.getY() * _gridLatStep;
    double minLon = index.getX() * _gridLonStep;
    double maxLat = minLat + _gridLatStep;
    double maxLon = minLon + _gridLonStep;
    return new CoordinateBounds(minLat, minLon, maxLat, maxLon);
  }

  protected void getIndexAndDirectionAsPoint(GridIndex index,
      EDirection direction, DoublePoint point) {

    double minLat = index.getY() * _gridLatStep;
    double minLon = index.getX() * _gridLonStep;
    double maxLat = minLat + _gridLatStep;
    double maxLon = minLon + _gridLonStep;

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
