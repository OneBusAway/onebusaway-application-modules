package org.onebusaway.metrokc2gtdf.handlers;

import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.metrokc2gtdf.TranslationContext;
import org.onebusaway.metrokc2gtdf.model.MetroKCRoute;

public class RoutesHandler extends EntityHandler<String, Route> {

  private static String[] ROUTES_FIELDS = {
      "id", "number", "code", "ignore=description", "ignore=dbModDate",
      "ignore=effectiveBeginDate", "ignore=effectiveEndDate", "transitAgencyId"};

  private TranslationContext _context;

  public RoutesHandler(TranslationContext context) {
    super(MetroKCRoute.class, ROUTES_FIELDS);
    _context = context;
  }

  @Override
  public void handleEntity(Object bean) {

    MetroKCRoute route = (MetroKCRoute) bean;

    Route r = new Route();
    r.setId(route.getId().toString());
    r.setShortName(Integer.toString(route.getNumber()));
    r.setLongName(""); // We can do this right at some point
    r.setType(3); // What about route 98... the streetcar

    super.handleEntity(r);

  }

  public void writeRoutes() {
    CsvEntityWriter writer = _context.getWriter();
    for (Route route : getValues()) {
      writer.handleEntity(route);
    }
  }
}
