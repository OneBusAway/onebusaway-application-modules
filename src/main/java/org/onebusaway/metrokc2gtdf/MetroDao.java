package org.onebusaway.metrokc2gtdf;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.metrokc2gtdf.handlers.BlockTripHandler;
import org.onebusaway.metrokc2gtdf.handlers.MetroKCShapePoint;
import org.onebusaway.metrokc2gtdf.handlers.RoutesHandler;
import org.onebusaway.metrokc2gtdf.handlers.ServicePatternHandler;
import org.onebusaway.metrokc2gtdf.handlers.ShapePointHandler;
import org.onebusaway.metrokc2gtdf.handlers.TripHandler;
import org.onebusaway.metrokc2gtdf.model.MetroKCBlockTrip;
import org.onebusaway.metrokc2gtdf.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtdf.model.MetroKCTrip;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetroDao {

  // private RoutesHandler _routesHandler;

  // private ServicePatternHandler _servicePatternHandler;

  // private TripHandler _tripHandler;

  // private BlockTripHandler _blockTripHandler;

  private TranslationContext _context;

  public MetroDao(TranslationContext context) {
    _context = context;
  }

  public Collection<Route> getAllRoutes() {
    RoutesHandler _routesHandler = _context.getHandler(RoutesHandler.class);
    return _routesHandler.getValues();
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

  public List<MetroKCBlockTrip> getBlockTripsByServicePattern(
      MetroKCServicePattern pattern) {

    TripHandler tripHandler = _context.getHandler(TripHandler.class);
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    List<MetroKCTrip> trips = tripHandler.getTripsByServicePattern(pattern.getId());
    return blockTripHandler.getTripBlocksForTrips(trips);
  }

  public Map<Integer, List<MetroKCBlockTrip>> getBlockTripsByBlockIds(Set<Integer> ids) {
    BlockTripHandler blockTripHandler = _context.getHandler(BlockTripHandler.class);
    return blockTripHandler.getBlockTripsByBlockIds(ids);
  }

  public MetroKCShapePoint getLastShapePointByServicePattern(
      MetroKCServicePattern servicePattern) {
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
