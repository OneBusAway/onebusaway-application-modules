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

import java.util.Collection;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

/**
 * Service methods for querying {@link RouteEntry} and
 * {@link RouteCollectionEntry} objects.
 * 
 * @author bdferris
 * 
 */
public interface RouteService {

  /**
   * Joins over the relation {@link RouteCollectionEntry} -> {@link RouteEntry}
   * -> {@link TripEntry} -> {@link StopTimeEntry} -> {@link StopEntry},
   * constructing the unique set of stops.
   * 
   * @param routeCollectionId the {@link RouteCollectionEntry} id
   * @return the set of all stop ids for stops servicing the particular route
   *         collection
   */
  public Collection<AgencyAndId> getStopsForRouteCollection(
      AgencyAndId routeCollectionId);

  /**
   * Joins over the relation {@link RouteCollectionEntry} -> {@link RouteEntry}
   * -> {@link TripEntry} -> {@link StopTimeEntry} -> {@link StopEntry},
   * constructing the unique set of stops.
   *
   * @param routeCollectionId the {@link RouteCollectionEntry} id
   * @serviceDate
   * @return the set of all stop ids for stops servicing the particular route
   *         collection and service date
   */
  public Collection<AgencyAndId> getStopsForRouteCollectionForServiceDate(
          AgencyAndId routeCollectionId, ServiceDate serviceDate);

  /**
   * Return the set of route collection ids serving the specified stop.
   * 
   * @param stopId
   * @return the set of of route collection ids
   */
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId);

  /**
   * Return the set of route collection ids serving the specified stop and service date.
   *
   * @param stopId
   * @param serviceDate
   * @return the set of of route collection ids
   */
  public Set<AgencyAndId> getRouteCollectionIdsForStopForServiceDate(AgencyAndId stopId, ServiceDate serviceDate);
}
