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

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;

/**
 * Service interface for searching for {@link RouteCollectionEntry} ids by route
 * short names and other parameters.
 * 
 * @author bdferris
 * @see RouteCollectionEntry
 */
public interface RouteCollectionSearchService {

  /**
   * 
   * @param nameQuery the route short name query
   * @param maxResultCount maximum number of results to keep
   * @param minScoreToKeep score tuning metric to prune result (implementation
   *          dependent)
   * @return a search result for {@link RouteCollectionEntry} ids matching the
   *         specified short name query
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForRoutesByName(String nameQuery,
      int maxResultCount, double minScoreToKeep) throws IOException,
          ParseException;
}
