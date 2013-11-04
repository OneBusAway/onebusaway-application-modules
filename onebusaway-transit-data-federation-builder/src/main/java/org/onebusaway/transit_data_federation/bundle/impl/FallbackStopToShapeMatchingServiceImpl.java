/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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
package org.onebusaway.transit_data_federation.bundle.impl;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.bundle.services.StopToShapeMatchingService;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;

import java.util.List;

/**
 * Fallback method for generating distance-along-trip values;
 * does not require (in fact, does not make use of) shape points.
 *
 */
public class FallbackStopToShapeMatchingServiceImpl implements StopToShapeMatchingService {

  @Override
  public void ensureStopTimesHaveShapeDistanceTraveledSet(List<StopTimeEntryImpl> stopTimes,
          ShapePoints shapePoints, List<Double> stopTimeDistances) {
    double d = 0;
    StopTimeEntryImpl prev = null;
    for (StopTimeEntryImpl stopTime : stopTimes) {
      if (prev != null) {
        CoordinatePoint from = prev.getStop().getStopLocation();
        CoordinatePoint to = stopTime.getStop().getStopLocation();
        d += SphericalGeometryLibrary.distance(from, to);
      }
      stopTime.setShapeDistTraveled(d);
      prev = stopTime;
    }
  }
}
