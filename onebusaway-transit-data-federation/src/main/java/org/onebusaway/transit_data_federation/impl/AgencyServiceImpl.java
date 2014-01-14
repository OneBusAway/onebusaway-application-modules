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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.exceptions.NoSuchAgencyServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgencyServiceImpl implements AgencyService {

  private static Logger _log = LoggerFactory.getLogger(AgencyServiceImpl.class);

  @Autowired
  private TransitGraphDao _graph;

  @Autowired
  private NarrativeService _narrativeService;

  @Cacheable
  public TimeZone getTimeZoneForAgencyId(String agencyId) {
    AgencyNarrative narrative = _narrativeService.getAgencyForId(agencyId);
    if (narrative == null)
      throw new NoSuchAgencyServiceException(agencyId);
    return TimeZone.getTimeZone(narrative.getTimezone());
  }

  @Cacheable
  @Override
  public List<String> getAllAgencyIds() {

    List<String> agencyIds = new ArrayList<String>();

    for (AgencyEntry agency : _graph.getAllAgencies()) {
      agencyIds.add(agency.getId());
    }

    return new ArrayList<String>(agencyIds);
  }

  @Cacheable
  public Map<String, CoordinatePoint> getAgencyIdsAndCenterPoints() {

    Map<String, CoordinatePoint> centersByAgencyId = new HashMap<String, CoordinatePoint>();

    for (AgencyEntry agency : _graph.getAllAgencies()) {
      
      StopsCenterOfMass centerOfMass = new StopsCenterOfMass();

      for( RouteCollectionEntry routeCollection : agency.getRouteCollections()) {
        for( RouteEntry route : routeCollection.getChildren() ) {
          for (TripEntry trip : route.getTrips() ) {
            for( StopTimeEntry stopTime : trip.getStopTimes() ) {
              StopEntry stop = stopTime.getStop();
              centerOfMass.lats += stop.getStopLat();
              centerOfMass.lons += stop.getStopLon();
              centerOfMass.count++;
            }
          }
        }
      }
      
      if (centerOfMass.count == 0) {
        _log.warn("Agency has no service: " + agency);
      } else {
        double lat = centerOfMass.lats / centerOfMass.count;
        double lon = centerOfMass.lons / centerOfMass.count;
        centersByAgencyId.put(agency.getId(), new CoordinatePoint(lat, lon));
      }
    }

    return centersByAgencyId;
  }

  @Cacheable
  public Map<String, CoordinateBounds> getAgencyIdsAndCoverageAreas() {

    Map<String, CoordinateBounds> boundsByAgencyId = new HashMap<String, CoordinateBounds>();

    for (AgencyEntry agency : _graph.getAllAgencies()) {

      CoordinateBounds bounds = new CoordinateBounds();
      
      for( RouteCollectionEntry routeCollection : agency.getRouteCollections()) {
        for( RouteEntry route : routeCollection.getChildren() ) {
          for (TripEntry trip : route.getTrips() ) {
            for( StopTimeEntry stopTime : trip.getStopTimes() ) {
              StopEntry stop = stopTime.getStop();
              bounds.addPoint(stop.getStopLat(), stop.getStopLon());
            }
          }
        }
      }

      boundsByAgencyId.put(agency.getId(), bounds);
    }

    return boundsByAgencyId;
  }

  private static class StopsCenterOfMass {
    public double lats = 0;
    public double lons = 0;
    public double count = 0;
  }

}
