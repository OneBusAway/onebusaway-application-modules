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
package org.onebusaway.transit_data_federation.impl.shapes;

import java.util.List;

import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.shapes.ProjectedShapePointService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ProjectedShapePointServiceImpl implements ProjectedShapePointService {

  private ShapePointService _shapePointService;

  private ShapePointsLibrary _shapePointsLibrary;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setShapePointsLibrary(ShapePointsLibrary shapePointsLibrary) {
    _shapePointsLibrary = shapePointsLibrary;
  }

  @Cacheable
  @Override
  public T2<List<XYPoint>, double[]> getProjectedShapePoints(
      List<AgencyAndId> shapeIds, int utmZoneId) {

    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeIds(shapeIds);

    if (shapePoints == null || shapePoints.isEmpty())
      return null;

    UTMProjection projection = new UTMProjection(utmZoneId);

    List<XYPoint> projected = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);

    return Tuples.tuple(projected, shapePoints.getDistTraveled());
  }
}

