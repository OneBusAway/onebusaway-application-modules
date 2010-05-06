package org.onebusaway.metrokc2gtfs.impl;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.handlers.BlockTripHandler;
import org.onebusaway.metrokc2gtfs.handlers.MetroKCShapePoint;
import org.onebusaway.metrokc2gtfs.handlers.RoutesHandler;
import org.onebusaway.metrokc2gtfs.handlers.ServicePatternHandler;
import org.onebusaway.metrokc2gtfs.handlers.ShapePointHandler;
import org.onebusaway.metrokc2gtfs.handlers.TripHandler;
import org.onebusaway.metrokc2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtfs.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtfs.model.MetroKCTrip;
import org.onebusaway.metrokc2gtfs.model.ServicePatternKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetroDao {

  private TranslationContext _context;

  public MetroDao(TranslationContext context) {
    _context = context;
  }

  public Collection<Route> getAllRoutes() {
    RoutesHandler _routesHandler = _context.getHandler(RoutesHandler.class);
    return _routesHandler.getValues();
  }

  public Route getRouteForServicePatternKey(ServicePatternKey key) {
    ServicePatternHandler servicePatternHandler = _context.getHandler(ServicePatternHandler.class);
    MetroKCServicePattern servicePattern = servicePatternHandler.getEntity(key);
    return getRouteForServicePattern(servicePattern);
  }

  public Route getRouteForServicePattern(MetroKCServicePattern servicePattern) {
    RoutesHandler routesHandler = _context.getHandler(RoutesHandler.class);
    return routesHandler.getEntity(servicePattern.getRoute());
  }

  public Set<MetroKCServicePattern> getServicePatternsByRoute(Route route) {
    ServicePatternHandler servicePatternHandler = _context.getHandler(ServicePatternHandler.class);
    return servicePatternHandler.getServicePatternsByRoute(route);
  }

  public MetroKCServicePattern getServicePatternByTrip(MetroKCTrip trip) {
    ServicePatternHandler servicePatternHandler = _context.getHandler(ServicePatternHandler.class);
    return servicePatternHandler.getEntity(trip.getServicePattern());
  }

  public long getTripCount(MetroKCServicePattern pattern) {
    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    return tripHandler.getTripCount(pattern.getId());
  }

  public MetroKCTrip getTripById(int tripId) {
    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    return tripHandler.getEntity(tripId);
  }

  public List<MetroKCBlockTrip> getBlockTripsByServicePattern(MetroKCServicePattern pattern) {

    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    List<MetroKCTrip> trips = tripHandler.getTripsByServicePattern(pattern.getId());
    return blockTripHandler.getTripBlocksForTrips(trips);
  }

  public Map<Integer, List<MetroKCBlockTrip>> getBlockTripsByBlockIds(Set<Integer> ids) {
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    return blockTripHandler.getBlockTripsByBlockIds(ids);
  }

  public MetroKCShapePoint getLastShapePointByServicePattern(MetroKCServicePattern servicePattern) {
    ShapePointHandler handler = _context.getHandler(ShapePointHandler.class);
    return handler.getLastShapePointByServicePattern(servicePattern.getId());
  }

  public Map<Integer, List<MetroKCBlockTrip>> getAllBlockTripsByBlockId() {
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    return blockTripHandler.getAllBlockTripsByBlockId();
  }

  public Collection<MetroKCServicePattern> getAllServicePatterns() {
    ServicePatternHandler servicePatternHandler = _context.getHandler(ServicePatternHandler.class);
    return servicePatternHandler.getValues();
  }

}
