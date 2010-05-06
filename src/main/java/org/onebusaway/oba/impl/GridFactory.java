package org.onebusaway.oba.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.oba.web.common.client.model.LocationBounds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridFactory {

  private ProjectionService _projection;

  private double _gridSize;

  private Set<IntPoint> _gridCells = new HashSet<IntPoint>();

  public GridFactory(ProjectionService projection, double gridSize) {
    _projection = projection;
    _gridSize = gridSize;
  }

  public void addPoint(Point point, double radius) {
    int xMin = (int) Math.floor((point.getX() - radius) / _gridSize);
    int xMax = (int) Math.floor((point.getX() + radius) / _gridSize);
    int yMin = (int) Math.floor((point.getY() - radius) / _gridSize);
    int yMax = (int) Math.floor((point.getY() + radius) / _gridSize);
    for (int x = xMin; x <= xMax; x++) {
      for (int y = yMin; y <= yMax; y++) {
        IntPoint index = new IntPoint(x, y);
        addCell(index);
      }
    }
  }

  public List<LocationBounds> getGrid() {
    List<LocationBounds> results = new ArrayList<LocationBounds>();
    for (IntPoint cell : _gridCells) {
      LocationBounds bounds = getCellAsLocationBounds(cell);
      results.add(bounds);
    }
    return results;
  }

  protected LocationBounds getCellAsLocationBounds(IntPoint cell) {

    DoublePoint min = new DoublePoint();
    DoublePoint max = new DoublePoint();
    getCellAsPoints(cell, min, max);
    CoordinatePoint pMin = _projection.getXYAsLatLong(min.x, min.y);
    CoordinatePoint pMax = _projection.getXYAsLatLong(max.x, max.y);

    LocationBounds bounds = new LocationBounds();

    bounds.setLatMin(pMin.getLat());
    bounds.setLonMin(pMin.getLon());
    bounds.setLatMax(pMax.getLat());
    bounds.setLonMax(pMax.getLon());
    return bounds;
  }

  protected void getCellAsPoints(IntPoint cell, DoublePoint minPoint,
      DoublePoint maxPoint) {
    minPoint.x = cell.x * _gridSize;
    minPoint.y = cell.y * _gridSize;
    maxPoint.x = minPoint.x + _gridSize;
    maxPoint.y = minPoint.y + _gridSize;
  }

  protected void addCell(IntPoint index) {
    _gridCells.add(index);
  }

  protected static class IntPoint {
    int x;
    int y;

    public IntPoint(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof IntPoint))
        return false;
      IntPoint ip = (IntPoint) obj;
      return this.x == ip.x && this.y == ip.y;
    }

    @Override
    public int hashCode() {
      return 7 * this.x + 13 * this.y;
    }
  }

  protected static class DoublePoint {
    double x;
    double y;
  }
}
