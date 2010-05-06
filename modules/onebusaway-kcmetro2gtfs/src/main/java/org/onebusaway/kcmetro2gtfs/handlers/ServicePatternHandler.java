/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.model.MetroKCServicePattern;
import org.onebusaway.kcmetro2gtfs.model.ServicePatternKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServicePatternHandler extends EntityHandler<ServicePatternKey, MetroKCServicePattern> {

  private static String[] SERVICE_PATTERN_FIELDS = {
      "service_pattern_id", "change_date", "dbModDate", "direction", "ignore", "ignore", "route", "ignore",
      "service_type", "schedule_pattern_id", "patternType"};

  private TranslationContext _context;

  private Map<AgencyAndId, Set<MetroKCServicePattern>> _servicePatternsByRoute = new HashMap<AgencyAndId, Set<MetroKCServicePattern>>();

  public ServicePatternHandler(TranslationContext context) {
    super(MetroKCServicePattern.class, SERVICE_PATTERN_FIELDS);
    _context = context;
  }

  public Route getRouteByServicePatternKey(ServicePatternKey key) {
    RoutesHandler routes = _context.getHandler(RoutesHandler.class);
    MetroKCServicePattern sp = getEntity(key);
    return routes.getRouteForId(sp.getRoute());
  }

  public Set<MetroKCServicePattern> getServicePatternsByRoute(Route route) {
    return _servicePatternsByRoute.get(route.getId());
  }

  @Override
  public void close() {
    super.close();

    RoutesHandler routeHandler = _context.getHandler(RoutesHandler.class);
    
    for (MetroKCServicePattern servicePattern : getValues()) {
      String routeId = servicePattern.getRoute();
      Route route = routeHandler.getRouteForId(routeId);
      Set<MetroKCServicePattern> patterns = _servicePatternsByRoute.get(route.getId());
      if (patterns == null) {
        patterns = new HashSet<MetroKCServicePattern>();
        _servicePatternsByRoute.put(route.getId(), patterns);
      }
      patterns.add(servicePattern);
    }
  }

}