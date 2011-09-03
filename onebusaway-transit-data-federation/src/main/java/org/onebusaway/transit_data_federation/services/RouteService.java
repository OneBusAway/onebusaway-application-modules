/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.model.RouteCollection;

import java.util.Collection;
import java.util.Set;

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

  /**
   * Return the set of route collection ids serving the specified stop.
   * 
   * @param stopId
   * @return the set of of route collection ids
   */
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId);
}
