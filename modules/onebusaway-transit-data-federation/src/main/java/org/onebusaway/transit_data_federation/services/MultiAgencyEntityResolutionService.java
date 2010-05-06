package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;

import java.util.List;

public interface MultiAgencyEntityResolutionService {
  public List<Stop> resolveStops(List<Stop> stops, MultiAgencyResolutionHints hints);
  public List<Route> resolveRoutes(List<Route> stops, MultiAgencyResolutionHints hints);
}
