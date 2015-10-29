package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
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
  
  
  // Get list of routes by agency
  // AgencyAndId will be processed by Jackson to: {"agencyId":"<agency>","id":"<id>"}
  @RequestMapping(value="/routes")
  public @ResponseBody List<AgencyAndId> getRoutes() {
    List<RouteEntry> routes = _transitGraphDao.getAllRoutes();
   
    ArrayList<AgencyAndId> routeNames = new ArrayList<AgencyAndId>();
    
    Iterator<RouteEntry> iter = routes.iterator();
    while(iter.hasNext()) {
      AgencyAndId id = iter.next().getId();
      routeNames.add(id);
    }
    
    return routeNames;
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
