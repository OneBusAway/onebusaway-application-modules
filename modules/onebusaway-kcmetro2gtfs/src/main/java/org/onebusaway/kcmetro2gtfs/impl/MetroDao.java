package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.handlers.BlockTripHandler;
import org.onebusaway.kcmetro2gtfs.handlers.OrderedPatternStopsHandler;
import org.onebusaway.kcmetro2gtfs.handlers.RoutesHandler;
import org.onebusaway.kcmetro2gtfs.handlers.ServicePatternHandler;
import org.onebusaway.kcmetro2gtfs.handlers.ShapePointHandler;
import org.onebusaway.kcmetro2gtfs.handlers.StopHandler;
import org.onebusaway.kcmetro2gtfs.handlers.TripHandler;
import org.onebusaway.kcmetro2gtfs.model.MetroKCBlockTrip;
import org.onebusaway.kcmetro2gtfs.model.MetroKCOrderedPatternStop;
import org.onebusaway.kcmetro2gtfs.model.MetroKCServicePattern;
import org.onebusaway.kcmetro2gtfs.model.MetroKCShapePoint;
import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;
import org.onebusaway.kcmetro2gtfs.model.MetroKCTrip;
import org.onebusaway.kcmetro2gtfs.model.RouteSchedulePatternId;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;

import java.util.ArrayList;
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
    return servicePatternHandler.getRouteByServicePatternKey(key);
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

  public MetroKCTrip getTripById(ServicePatternKey tripId) {
    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    return tripHandler.getEntity(tripId);
  }

  public List<MetroKCBlockTrip> getBlockTripsByServicePattern(
      MetroKCServicePattern pattern) {

    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    List<MetroKCTrip> trips = tripHandler.getTripsByServicePattern(pattern.getId());
    return blockTripHandler.getTripBlocksForTrips(trips);
  }

  public Map<Integer, List<MetroKCBlockTrip>> getBlockTripsByBlockIds(
      Set<Integer> ids) {
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    return blockTripHandler.getBlockTripsByBlockIds(ids);
  }

  public List<MetroKCShapePoint> getLastShapePointByServicePattern(
      MetroKCServicePattern servicePattern) {
    ShapePointHandler handler = _context.getHandler(ShapePointHandler.class);
    return handler.getShapePointsByServicePattern(servicePattern.getId());
  }

  public List<MetroKCStop> getStopsByServicePattern(
      MetroKCServicePattern servicePattern) {

    OrderedPatternStopsHandler handler = _context.getHandler(OrderedPatternStopsHandler.class);
    StopHandler stopHandler = _context.getHandler(StopHandler.class);

    RouteSchedulePatternId rspid = new RouteSchedulePatternId(
        servicePattern.getRoute(), servicePattern.getSchedulePatternId());
    List<MetroKCOrderedPatternStop> opss = handler.getOrderedPatternStopsByRouteSchedulePatternId(rspid);
    List<MetroKCStop> stops = new ArrayList<MetroKCStop>();
    for (MetroKCOrderedPatternStop ops : opss) {
      int stopId = ops.getStop();
      stops.add(stopHandler.getEntity(stopId));
    }

    return stops;
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
