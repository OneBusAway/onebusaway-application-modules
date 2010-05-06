package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class ShapeBeanServiceImpl implements ShapeBeanService {

  private ShapePointService _shapePointService;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Cacheable
  public EncodedPolylineBean getPolylineForShapeId(AgencyAndId id) {
    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(id);
    return PolylineEncoder.createEncodings(shapePoints.getLats(),
        shapePoints.getLons(), -1);
  }

  @Cacheable
  public List<EncodedPolylineBean> getMergedPolylinesForShapeIds(
      Collection<AgencyAndId> shapeIds) {

    List<EncodedPolylineBean> polylines = new ArrayList<EncodedPolylineBean>();

    if (shapeIds.isEmpty())
      return polylines;

    List<CoordinatePoint> currentLine = new ArrayList<CoordinatePoint>();
    Set<Edge> edges = new HashSet<Edge>();

    for (AgencyAndId shapeId : shapeIds) {

      ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);
      double[] lats = shapePoints.getLats();
      double[] lons = shapePoints.getLons();

      CoordinatePoint prev = null;

      for (int i = 0; i < shapePoints.getSize(); i++) {

        CoordinatePoint loc = new CoordinatePoint(lats[i], lons[i]);
        if (prev != null && !prev.equals(loc)) {
          Edge edge = new Edge(prev, loc);
          if (!edges.add(edge)) {
            if (currentLine.size() > 1)
              polylines.add(PolylineEncoder.createEncodings(currentLine));
            currentLine.clear();
          }
        }
        
        currentLine.add(loc);
        prev = loc;
      }

      if (currentLine.size() > 1)
        polylines.add(PolylineEncoder.createEncodings(currentLine));
      currentLine.clear();
    }

    return polylines;
  }

  /****
   * Private Methods
   ****/

  private static int compare(CoordinatePoint a, CoordinatePoint b) {
    int rc = Double.compare(a.getLat(), b.getLat());
    if (rc == 0)
      rc = Double.compare(a.getLon(), b.getLon());
    return rc;
  }

  private static class Edge {
    private final CoordinatePoint _a;

    private final CoordinatePoint _b;

    public Edge(CoordinatePoint a, CoordinatePoint b) {
      int rc = compare(a, b);
      if (rc <= 0) {
        _a = a;
        _b = b;
      } else {
        _a = b;
        _b = a;
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_a == null) ? 0 : _a.hashCode());
      result = prime * result + ((_b == null) ? 0 : _b.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Edge other = (Edge) obj;
      if (_a == null) {
        if (other._a != null)
          return false;
      } else if (!_a.equals(other._a))
        return false;
      if (_b == null) {
        if (other._b != null)
          return false;
      } else if (!_b.equals(other._b))
        return false;
      return true;
    }

  }
}
