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
package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;

/**
 * Service for retrieving {@link RouteCollectionEntry} objects as
 * {@link RouteBean} summary objects. Recall that the underlying representation
 * of a route in a transit data federation bundle is a
 * {@link RouteCollectionEntry}, not a {@link Route}.
 * 
 * @author bdferris
 * @see RouteCollectionEntry
 * @see RouteBean
 */
public interface RouteBeanService {

  /**
   * @param routeId see {@link RouteCollectionEntry#getId()}
   * @return the route bean representation of the route with the specified id,
   *         or null if not found
   */
  public RouteBean getRouteForId(AgencyAndId routeId);

  /**
   * @param routeId see {@link RouteCollectionEntry#getId()}
   * @return the stops for the route with the specified bean, or null if not
   *         found
   */
  public StopsForRouteBean getStopsForRoute(AgencyAndId routeId);

  /**
   * @param routeId see {@link RouteCollectionEntry#getId()}
   * @param serviceDate
   * @return the stops for the route with the specified bean and service date, or null if not
   *         found
   */
  public StopsForRouteBean getStopsForRouteForServiceDate(AgencyAndId routeId, ServiceDate serviceDate);
}
