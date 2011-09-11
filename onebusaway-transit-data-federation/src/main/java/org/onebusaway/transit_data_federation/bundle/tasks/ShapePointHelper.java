/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShapePointHelper {

  private GtfsRelationalDao _gtfsDao;

  private Map<AgencyAndId, ShapePoints> _cache = new HashMap<AgencyAndId, ShapePoints>();

  @Autowired
  public void setGtfsRelationalDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId) {

    ShapePoints shapePoints = _cache.get(shapeId);
    if (shapePoints == null) {
      shapePoints = getShapePointsForShapeIdNonCached(shapeId);
      _cache.put(shapeId, shapePoints);
    }
    return shapePoints;
  }

  private ShapePoints getShapePointsForShapeIdNonCached(AgencyAndId shapeId) {

    List<ShapePoint> shapePoints = _gtfsDao.getShapePointsForShapeId(shapeId);

    shapePoints = deduplicateShapePoints(shapePoints);

    if (shapePoints.isEmpty())
      return null;

    int n = shapePoints.size();

    double[] lat = new double[n];
    double[] lon = new double[n];
    double[] distTraveled = new double[n];

    int i = 0;
    for (ShapePoint shapePoint : shapePoints) {
      lat[i] = shapePoint.getLat();
      lon[i] = shapePoint.getLon();
      i++;
    }

    ShapePoints result = new ShapePoints();
    result.setShapeId(shapeId);
    result.setLats(lat);
    result.setLons(lon);
    result.setDistTraveled(distTraveled);

    result.ensureDistTraveled();

    return result;
  }

  private List<ShapePoint> deduplicateShapePoints(List<ShapePoint> shapePoints) {

    List<ShapePoint> deduplicated = new ArrayList<ShapePoint>();
    ShapePoint prev = null;

    for (ShapePoint shapePoint : shapePoints) {
      if (prev == null
          || !(prev.getLat() == shapePoint.getLat() && prev.getLon() == shapePoint.getLon())) {
        deduplicated.add(shapePoint);
      }
      prev = shapePoint;
    }

    return deduplicated;
  }
}
