/**
 * 
 */
package org.onebusaway.metrokc2gtfs.handlers;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.csv.CsvEntityWriter;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.metrokc2gtfs.TranslationContext;
import org.onebusaway.metrokc2gtfs.impl.StopNameStrategy;
import org.onebusaway.metrokc2gtfs.model.MetroKCStop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopHandler extends InputHandler {

  private static final String[] STOP_LOCATION_FIELDS = {
      "bay", "ignore", "ignore", "ignore=createdBy", "cross_street_name_id", "ignore=dateCreated", "ignore=dateMapped",
      "ignore=dateModified", "displacement", "effective_begin_date", "effectiveEndDate", "fromCrossCurb",
      "fromIntersectionCenter", "gisJurisdictionCode", "gisZipCode", "ignore", "ignore", "ignore", "ignore", "ignore",
      "mappedLinkLen", "mappedPercentFrom", "mappedTransNodeFrom", "ignore", "ignore=modifiedBy",
      "ignore=rfaManualOverride", "rideFreeArea", "side", "sideCross", "sideOn", "id", "ignore",
      "ignore=streetAddress", "streetAddressComment", "trans_link", "street_x", "x", "street_y", "y"};

  private TranslationContext _context;

  private Map<Integer, MetroKCStop> _stops = new HashMap<Integer, MetroKCStop>();

  private ProjectionService _projection;

  private List<StopNameStrategy> _strategies = new ArrayList<StopNameStrategy>();

  public StopHandler(TranslationContext context) {
    super(MetroKCStop.class, STOP_LOCATION_FIELDS);

    _context = context;
    _projection = context.getProjectionService();
  }

  public void addStopNameStrategy(StopNameStrategy strategy) {
    _strategies.add(strategy);
  }

  public MetroKCStop getEntity(int id) {
    return _stops.get(id);
  }

  public void writeResults(Set<Integer> activeStops) throws IOException {

    CsvEntityWriter writer = _context.getWriter();

    // String direction = getDirection(xStreet, yStreet, stop.getX(),
    // stop.getY());
    // _stopToDirection.put(stop.getId(), direction);

    for (MetroKCStop stop : _stops.values()) {
      if (activeStops.contains(stop.getId())) {
        Stop s = new Stop();
        s.setId(stop.getId().toString());
        s.setName(getStopName(stop));
        s.setLocationType(0);
        s.setParentStation(0);
        s.setLat(stop.getLat());
        s.setLon(stop.getLon());
        s.setDirection(getDirection(stop));
        writer.handleEntity(s);
      }
    }
  }

  public void handleEntity(Object bean) {

    MetroKCStop stop = (MetroKCStop) bean;

    CoordinatePoint point = _projection.getXYAsLatLong(stop.getX(), stop.getY());
    stop.setLat(point.getLat());
    stop.setLon(point.getLon());

    if (!_stops.containsKey(stop.getId())
        || stop.getEffectiveBeginDate().after(_stops.get(stop.getId()).getEffectiveBeginDate()))
      _stops.put(stop.getId(), stop);
  }

  private String getStopName(MetroKCStop stop) {

    for (StopNameStrategy strategy : _strategies) {
      if (strategy.hasNameForStop(stop))
        return strategy.getNameForStop(stop);
    }

    StreetNameHandler streetNames = _context.getHandler(StreetNameHandler.class);
    TransLinkHandler transLinks = _context.getHandler(TransLinkHandler.class);

    int mainStreetId = transLinks.getStreetNameId(stop.getTransLink());
    int crossStreetId = stop.getCrossStreetNameId();
    String main = streetNames.getStreetName(mainStreetId);
    String cross = streetNames.getStreetName(crossStreetId);

    boolean hasMain = isStreetNameValid(main);
    boolean hasCross = isStreetNameValid(cross);

    if (hasMain && hasCross)
      return main + " & " + cross;
    if (hasMain)
      return main;
    if (hasCross)
      return cross;
    return "Stop # " + stop.getId();
  }

  private boolean isStreetNameValid(String main) {
    boolean hasMain = main != null && main.length() > 0 && !main.equals("Unnamed");
    return hasMain;
  }

  public double getDirection(MetroKCStop stop) {

    double x = stop.getX();
    double y = stop.getY();

    double xStreet = stop.getStreetX();
    double yStreet = stop.getStreetY();

    double theta = Math.atan2(y - yStreet, x - xStreet);
    theta += Math.PI / 2;
    while (theta > Math.PI)
      theta -= Math.PI * 2;
    while (theta <= -Math.PI)
      theta += Math.PI * 2;
    return theta;
  }
}