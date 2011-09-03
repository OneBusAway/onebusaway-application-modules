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
package org.onebusaway.webapp.impl.oba;

import java.util.List;
import java.util.Map;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.grid.GridFactory;
import org.onebusaway.geospatial.grid.TimedGridFactory;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.services.oba.OneBusAwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OneBusAwayServiceImpl implements OneBusAwayService {

  private TransitDataService _transitDataService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {

    MinTravelTimeToStopsBean minTravelTimeToStops = _transitDataService.getMinTravelTimeToStopsFrom(
        location, time, constraints);

    ConstraintsBean c = constraints.getConstraints();

    double maxWalkDistance = c.getMaxWalkingDistance();
    double walkingVelocity = minTravelTimeToStops.getWalkingVelocity() / 1000;

    CoordinateBounds b = SphericalGeometryLibrary.bounds(location, 800);
    double latStep = b.getMaxLat() - b.getMinLat();
    double lonStep = b.getMaxLon() - b.getMinLon();
    GridFactory gridFactory = new GridFactory(latStep, lonStep);
    TimedGridFactory timedGridFactory = new TimedGridFactory(latStep / 4,
        lonStep / 4, walkingVelocity);

    long maxTripLength = c.getMaxTripDuration() * 1000;

    for (int i = 0; i < minTravelTimeToStops.getSize(); i++) {

      double stopLat = minTravelTimeToStops.getStopLat(i);
      double stopLon = minTravelTimeToStops.getStopLon(i);

      long duration = minTravelTimeToStops.getTravelTime(i);

      double remainingWalkingDistance = (maxTripLength - duration)
          * walkingVelocity;
      remainingWalkingDistance = Math.min(remainingWalkingDistance,
          maxWalkDistance);

      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(stopLat,
          stopLon, remainingWalkingDistance);
      gridFactory.addBounds(bounds);

      long remainingWalkingTime = (long) (remainingWalkingDistance / walkingVelocity);
      if (remainingWalkingTime > 0)
        timedGridFactory.addPoint(stopLat, stopLon, duration,
            remainingWalkingTime);
    }

    MinTransitTimeResult result = new MinTransitTimeResult();
    result.setComplete(true);
    result.setMinTravelTimeToStops(minTravelTimeToStops);
    if (timeSegmentSize < 1)
      timeSegmentSize = 5;

    Map<Integer, List<EncodedPolygonBean>> polygonsByTime = timedGridFactory.getPolygonsByTime(timeSegmentSize);
    for (Map.Entry<Integer, List<EncodedPolygonBean>> entry : polygonsByTime.entrySet()) {
      int t = entry.getKey();
      for (EncodedPolygonBean bean : entry.getValue()) {
        result.getTimePolygons().add(bean);
        result.getTimes().add(t * timeSegmentSize);
      }
    }

    result.setSearchGrid(gridFactory.getGrid());
    return result;
  }

  public List<TimedPlaceBean> getLocalPaths(ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {

    return _transitDataService.getLocalPaths(travelTimes.getAgencyId(),
        constraints, travelTimes, localResults);
  }
}
