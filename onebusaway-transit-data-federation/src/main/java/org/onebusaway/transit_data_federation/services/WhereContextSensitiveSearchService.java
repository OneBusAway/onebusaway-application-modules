package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;

import java.util.List;

public interface WhereContextSensitiveSearchService {
  public List<Stop> getStopsById(String id);
  public List<Route> getRoutesByName(String name);
}
