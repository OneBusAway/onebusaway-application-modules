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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.services.StopToShapeMatchingService;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Conventional method for generating distance-along-trip values
 * which attempts to map stops to the closest point along the shape,
 * accounting for loops and other irregularities.
 *
 * Requires that shapes be present, and can fail if the shapes are sufficiently
 * malformed or stops are too far from the corresponding shape points.
 *
 */
public class HeuristicStopToShapeMatchingServiceImpl implements StopToShapeMatchingService {

  private final Logger _log = LoggerFactory.getLogger(HeuristicStopToShapeMatchingServiceImpl.class);
  private DistanceAlongShapeLibrary _distanceAlongShapeLibrary;

  @Autowired
  public void setDistanceAlongShapeLibrary(
          DistanceAlongShapeLibrary distanceAlongShapeLibrary) {
    _distanceAlongShapeLibrary = distanceAlongShapeLibrary;
  }

  @Override
  public void ensureStopTimesHaveShapeDistanceTraveledSet(List<StopTimeEntryImpl> stopTimes,
          ShapePoints shapePoints, List<Double> stopTimeDistances) {

    if (shapePoints == null) {
      throw new StopToShapeMatcherStateException(this.getClass().getName() + " requires that shapePoints be non-null");
    }

    try {
      /*
       * Zero out and then reset the shape distance traveled values,
       * as stop-to-shape matching in DistanceAlongShapeLibrary
       * depends on OBA having set them such that the first point is at
       * zero and the distances are in meters.
       */
      shapePoints.setDistTraveled(new double[shapePoints.getSize()]);
      shapePoints.ensureDistTraveled();

      PointAndIndex[] stopTimePoints = _distanceAlongShapeLibrary.getDistancesAlongShape(
              shapePoints, stopTimes);
      for (int i = 0; i < stopTimePoints.length; i++) {
        PointAndIndex pindex = stopTimePoints[i];
        StopTimeEntryImpl stopTime = stopTimes.get(i);
        stopTime.setShapePointIndex(pindex.index);
        stopTime.setShapeDistTraveled(pindex.distanceAlongShape);
      }
    } catch (DistanceAlongShapeLibrary.StopIsTooFarFromShapeException ex) {
      StopTimeEntry stopTime = ex.getStopTime();
      TripEntry trip = stopTime.getTrip();
      StopEntry stop = stopTime.getStop();
      AgencyAndId shapeId = trip.getShapeId();
      CoordinatePoint point = ex.getPoint();
      PointAndIndex pindex = ex.getPointAndIndex();

      _log.warn("Stop is too far from shape: trip=" + trip.getId() + " stop="
              + stop.getId() + " stopLat=" + stop.getStopLat() + " stopLon="
              + stop.getStopLon() + " shapeId=" + shapeId + " shapePoint="
              + point + " index=" + pindex.index + " distance="
              + pindex.distanceFromTarget);

      throw new StopToShapeMatchingException(ex);
    } catch (DistanceAlongShapeLibrary.DistanceAlongShapeException ex) {
      _log.warn(
              "InvalidStopToShapeMappingException thrown; for more information on errors of this kind, see:\n"
              + "  https://github.com/OneBusAway/onebusaway-application-modules/wiki/Stop-to-Shape-Matching", ex);
      throw new StopToShapeMatchingException(ex);
    }
  }
}
