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

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;

/**
 * Service methods for retrieving lists of routes, usually with a search query.
 * Note that like {@like RouteService}, the underlying representation of a route
 * in a transit data federation bundle is a {@link RouteCollectionEntry}, not a
 * {@link Route}.
 * 
 * @author bdferris
 */
public interface RoutesBeanService {

  /**
   * TODO: Convert this to use {@list ListBean} at some point?
   * 
   * @param query the route search query
   * @return routes that match the specified query
   * @throws ServiceException
   */
  public RoutesBean getRoutesForQuery(SearchQueryBean query)
      throws ServiceException;

  /**
   * 
   * @param agencyId see {@link Agency#getId()}
   * @return the list of all routes for the specified agency
   */
  public ListBean<String> getRouteIdsForAgencyId(String agencyId);

  public ListBean<RouteBean> getRoutesForAgencyId(String agencyId);
}
