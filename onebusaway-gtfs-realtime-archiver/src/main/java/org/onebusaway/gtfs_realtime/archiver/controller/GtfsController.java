package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
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
  
  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }
  
  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }
  
  @RequestMapping(value="/agency")
  public @ResponseBody List<String> getAgencies() {
    
    List<AgencyEntry> entries = _transitGraphDao.getAllAgencies();
    ArrayList<String> agencies = new ArrayList<String>();
    
    for (AgencyEntry entry : entries) {
      String id = entry.getId();
      agencies.add(id);
    }
    
    return agencies;
  }
  
  // Get list of routes by agency
  @RequestMapping(value="/routes/{agencyId}")
  public @ResponseBody List<String> getRoutesByAgency(@PathVariable String agencyId) {
    
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
  @RequestMapping(value="/route/{agencyId}/{id}")
  public @ResponseBody ShapePoints getRoute(@PathVariable String agencyId, @PathVariable String id) {
    AgencyAndId routeId = new AgencyAndId(agencyId, id);
    RouteEntry route = _transitGraphDao.getRouteForId(routeId);
    return getShapePointsForRoute(route);
  }
  
  private ShapePoints getShapePointsForRoute(RouteEntry route) {
    TripEntry trip = route.getTrips().get(0); // BAD - just uses first trip.
    AgencyAndId shapeId = trip.getShapeId();
    return _shapePointService.getShapePointsForShapeId(shapeId);
  }
  
}
