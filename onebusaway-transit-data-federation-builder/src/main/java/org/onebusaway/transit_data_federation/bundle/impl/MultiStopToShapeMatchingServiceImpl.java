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
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author kurt
 */
public class MultiStopToShapeMatchingServiceImpl implements StopToShapeMatchingService {

  private final Logger _log = LoggerFactory.getLogger(MultiStopToShapeMatchingServiceImpl.class);
  private List<StopToShapeMatchingService> _matchingServiceImplementations;

  public MultiStopToShapeMatchingServiceImpl() {

  }

  public void setMatchingServiceImplementations(List<StopToShapeMatchingService> matchingServiceImplementations) {
    _matchingServiceImplementations = matchingServiceImplementations;
  }

  @Override
  public void ensureStopTimesHaveShapeDistanceTraveledSet(List<StopTimeEntryImpl> stopTimes,
          ShapePoints shapePoints, List<Double> stopTimeDistances) {

    List<Double> workingStopTimeDistances = (stopTimeDistances != null) ? Collections.unmodifiableList(stopTimeDistances) : null;

    for (StopToShapeMatchingService matchingServiceImplementation : _matchingServiceImplementations) {

      List<StopTimeEntryImpl> workingStopTimes = new ArrayList<StopTimeEntryImpl>();
      for (StopTimeEntryImpl ste: stopTimes) {
        workingStopTimes.add(new StopTimeEntryImpl(ste));
      }

      ShapePoints workingShapePoints = (shapePoints != null) ? new ShapePoints(shapePoints) : null;

      try {
        matchingServiceImplementation.ensureStopTimesHaveShapeDistanceTraveledSet(workingStopTimes, workingShapePoints, workingStopTimeDistances);
      } catch (StopToShapeMatcherStateException ex) {
        _log.info("Matching service " + matchingServiceImplementation.getClass().getName() + " unable to run: {}", ex.getMessage());
        continue;
      } catch (Exception ex) {
        _log.warn("Matching service " + matchingServiceImplementation.getClass().getName() + " failed with exception", ex);
        continue;
      }

      stopTimes.clear();
      stopTimes.addAll(workingStopTimes);
      return;
    }

    throw new IllegalStateException("No stop-to-shape matching service implementation completed successfully.");
  }
}
