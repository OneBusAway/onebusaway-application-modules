package org.onebusaway.kcmetro2gtfs.handlers;

import org.onebusaway.gtfs.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.kcmetro2gtfs.TranslationContext;
import org.onebusaway.kcmetro2gtfs.calendar.RouteModificationsStrategy;
import org.onebusaway.kcmetro2gtfs.model.MetroKCRoute;
import org.onebusaway.utility.text.NaturalStringOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class RoutesHandler extends EntityHandler<AgencyAndId, Route> {

  private static String[] ROUTES_FIELDS = {
      "id", "number", "code", "ignore=description", "ignore=dbModDate",
      "ignore=effectiveBeginDate", "ignore=effectiveEndDate", "transitAgencyId"};

  private TranslationContext _context;

  private Map<String, Route> _routeById = new HashMap<String, Route>();

  public RoutesHandler(TranslationContext context) {
    super(MetroKCRoute.class, ROUTES_FIELDS);
    _context = context;
  }

  public Route getRouteForId(String id) {
    return _routeById.get(id);
  }

  @Override
  public void handleEntity(Object bean) {

    MetroKCRoute route = (MetroKCRoute) bean;

    AgencyAndId id = new AgencyAndId(_context.getAgencyId(),
        route.getId().toString());

    Route r = new Route();
    r.setId(id);
    r.setShortName(Integer.toString(route.getNumber()));
    r.setLongName(""); // We can do this right at some point
    r.setType(3); // What about route 98... the streetcar

    RouteModificationsStrategy modifications = _context.getRouteModifications();
    if (modifications != null)
      modifications.modifyRoute(_context, r);

    // Update the agency linkage
    id = r.getId();
    r.setAgency(_context.getAgencyForId(id.getAgencyId()));

    super.handleEntity(r);

    _routeById.put(route.getId().toString(), r);
  }

  public void writeRoutes() {
    CsvEntityWriter writer = _context.getWriter();
    List<Route> routes = new ArrayList<Route>(getValues());
    Collections.sort(routes, new RouteComparator());
    for (Route route : routes)
      writer.handleEntity(route);
  }

  private static class RouteComparator implements Comparator<Route> {
    public int compare(Route o1, Route o2) {
      return NaturalStringOrder.compareNatural(o1.getShortName(), o2.getShortName());
    }
  }
}
