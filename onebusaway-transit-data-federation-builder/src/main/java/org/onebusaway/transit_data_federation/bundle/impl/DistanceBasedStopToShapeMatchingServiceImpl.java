/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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

import org.onebusaway.transit_data_federation.bundle.services.StopToShapeMatchingService;
import org.onebusaway.transit_data_federation.bundle.utilities.IndexedLine;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ListIterator;

/**
 * Distance-based method for generating distance-along-trip values which makes
 * use of shape_dist_traveled values.
 *
 * Will fail if shapes are not present, or if shapes or stop times are lacking
 * shape_dist_traveled values.
 *
 */
public class DistanceBasedStopToShapeMatchingServiceImpl implements StopToShapeMatchingService {

  private final Logger _log = LoggerFactory.getLogger(DistanceBasedStopToShapeMatchingServiceImpl.class);

  @Override
  public void ensureStopTimesHaveShapeDistanceTraveledSet(List<StopTimeEntryImpl> stopTimes,
          ShapePoints shapePoints, List<Double> stopTimeDistances) {

    if (shapePoints == null || stopTimeDistances == null) {
      throw new StopToShapeMatcherStateException(this.getClass().getName()
              + " requires that shapePoints be non-null and stopTimeDistances be non-null");
    }

    IndexedLine il = new IndexedLine();

    for (int i = 0; i < shapePoints.getSize(); i++) {
      if (i > 0 && shapePoints.getDistTraveledForIndex(i) <= shapePoints.getDistTraveledForIndex(i - 1)) {
        throw new StopToShapeMatcherStateException("Shape point distances went backwards: from "
                + shapePoints.getDistTraveledForIndex(i - 1) + " to " + shapePoints.getDistTraveledForIndex(i));
      }

      il.addPoint(i, shapePoints.getDistTraveledForIndex(i), shapePoints.getPointForIndex(i));
    }

    double firstMeasure = stopTimeDistances.get(0);

    ListIterator<StopTimeEntryImpl> stIterator = stopTimes.listIterator();

    while (stIterator.hasNext()) {
      int i = stIterator.nextIndex();
      StopTimeEntryImpl st = stIterator.next();

      double thisMeasure = stopTimeDistances.get(i);
      double distance = il.interpolateDistance(firstMeasure, thisMeasure);
      int index = il.interpolateIndex(thisMeasure);

      st.setShapePointIndex(index);
      st.setShapeDistTraveled(distance);
    }
  }
}
