/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GtfsController {

  private TransitGraphDao _transitGraphDao;
  private ShapePointService _shapePointService;
  private AgencyService _agencyService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @RequestMapping(value = "/agency")
  public @ResponseBody Map<String, CoordinateBounds> getAgencies() {
    return _agencyService.getAgencyIdsAndCoverageAreas();
  }

  // Get list of routes by agency
  @RequestMapping(value = "/routes/{agencyId}")
  public @ResponseBody List<String> getRoutesByAgency(
      @PathVariable String agencyId) {

    AgencyEntry agency = _transitGraphDao.getAgencyForId(agencyId);
    List<RouteCollectionEntry> collections = agency.getRouteCollections();

    List<String> routes = new ArrayList<String>();

    for (RouteCollectionEntry entry : collections) {
      for (RouteEntry route : entry.getChildren()) {
        String id = route.getId().getId();
        routes.add(id);
      }
    }

    return routes;
  }

  /* Expose shape points for route */
  @RequestMapping(value = "/route/{agencyId}/{id}")
  public @ResponseBody ShapePoints getRoute(@PathVariable String agencyId,
      @PathVariable String id) {
    AgencyAndId routeId = new AgencyAndId(agencyId, id);
    RouteEntry route = _transitGraphDao.getRouteForId(routeId);

    TripEntry trip = routeToTrip(route); // BAD - just uses first trip.
    AgencyAndId shapeId = trip.getShapeId();

    return _shapePointService.getShapePointsForShapeId(shapeId);
  }

  @RequestMapping(value = "/stops/{agencyId}/{id}")
  public @ResponseBody List<CoordinatePoint> getStops(
      @PathVariable String agencyId, @PathVariable String id) {

    AgencyAndId routeId = new AgencyAndId(agencyId, id);
    RouteEntry route = _transitGraphDao.getRouteForId(routeId);

    TripEntry trip = routeToTrip(route);
    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();

    for (StopTimeEntry entry : stopTimes) {
      StopEntry stop = entry.getStop();
      points.add(stop.getStopLocation());
    }

    return points;
  }

  // Get a trip given a route
  // Naive - uses first trip in route.
  private TripEntry routeToTrip(RouteEntry route) {
    return route.getTrips().get(0);
  }

}
