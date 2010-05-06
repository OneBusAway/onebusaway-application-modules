/**
 * 
 */
package org.onebusaway.metrokc2gtfs.handlers;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtfs.model.ServicePatternKey;

import java.util.Map;
import java.util.Set;

public class ServicePatternHandler extends
    EntityHandler<ServicePatternKey, MetroKCServicePattern> {

  private static String[] SERVICE_PATTERN_FIELDS = {
      "service_pattern_id", "change_date", "dbModDate", "direction", "ignore",
      "ignore", "route", "ignore", "service_type", "schedule_pattern_id",
      "patternType"};

  private TranslationContext _context;

  private Map<String, Set<MetroKCServicePattern>> _servicePatternsByRoute;

  public ServicePatternHandler(TranslationContext context) {
    super(MetroKCServicePattern.class, SERVICE_PATTERN_FIELDS);
    _context = context;
  }

  public Route getRouteByServicePatternKey(ServicePatternKey key) {
    RoutesHandler routes = _context.getHandler(RoutesHandler.class);
    MetroKCServicePattern sp = getEntity(key);
    return routes.getEntity(sp.getRoute());
  }

  public Set<MetroKCServicePattern> getServicePatternsByRoute(Route route) {
    return _servicePatternsByRoute.get(route.getId());
  }

  @Override
  public void close() {
    super.close();

    _servicePatternsByRoute = CollectionsLibrary.mapToValueSet(getValues(),
        "route", String.class);
  }

}