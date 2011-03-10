package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.model.RouteCollection;

import java.util.Collection;

/**
 * Service methods for querying {@link Route} and {@link RouteCollection}
 * objects.
 * 
 * @author bdferris
 * 
 */
public interface RouteService {

  /**
   * Joins over the relation RouteCollection -> Route -> Trip -> StopTime ->
   * Stop, constructing the unique set of stops.
   * 
   * @param routeCollectionId the {@link RouteCollection} id
   * @return the set of all stop ids for stops servicing the particular route
   *         collection
   */
  public Collection<AgencyAndId> getStopsForRouteCollection(
      AgencyAndId routeCollectionId);

  /**
   * Returns the {@link RouteCollection} id for the collection containing the
   * specified {@link Route}
   * 
   * @param route the target route
   * @return the route collection containing the specified route
   */
  public AgencyAndId getRouteCollectionIdForRoute(Route route);
}
