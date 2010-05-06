package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

import java.util.Collection;

public interface RouteService {
  public Collection<AgencyAndId> getStopsForRouteCollection(AgencyAndId id);
  public AgencyAndId getRouteCollectionIdForRoute(Route route);
}
