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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ShapeBeanServiceImpl implements ShapeBeanService {

  private static Logger _log = LoggerFactory.getLogger(ShapeBeanServiceImpl.class);

  private ShapePointService _shapePointService;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Cacheable
  public EncodedPolylineBean getPolylineForShapeId(AgencyAndId id) {
    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(id);
    if (shapePoints == null)
      return null;
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

      if (shapePoints == null) {
        _log.warn("no shape points for shapeId=" + shapeId);
        continue;
      }

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

        if (prev == null || !prev.equals(loc))
          currentLine.add(loc);

        prev = loc;
      }

      if (currentLine.size() > 1)
        polylines.add(PolylineEncoder.createEncodings(currentLine));
      currentLine.clear();
    }

    return polylines;
  }

  @Override
  public ListBean<String> getShapeIdsForAgencyId(String agencyId) {
    Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();
    for (TripEntry trip : _transitGraphDao.getAllTrips()) {
      AgencyAndId shapeId = trip.getShapeId();
      if (shapeId == null || !shapeId.hasValues())
        continue;
      if (!shapeId.getAgencyId().equals(agencyId))
        continue;
      shapeIds.add(shapeId);
    }
    List<String> ids = new ArrayList<String>();
    for (AgencyAndId shapeId : shapeIds) {
      String id = AgencyAndIdLibrary.convertToString(shapeId);
      ids.add(id);
    }

    Collections.sort(ids);
    return new ListBean<String>(ids, false);
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
