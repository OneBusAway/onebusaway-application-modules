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
