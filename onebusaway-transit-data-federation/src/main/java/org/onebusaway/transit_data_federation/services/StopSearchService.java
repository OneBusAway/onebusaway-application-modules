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

import org.apache.lucene.queryparser.classic.ParseException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.model.SearchResult;


import java.io.IOException;

/**
 * Service interface for searching for {@link Stop} ids by stop code and stop
 * name.
 * 
 * @author bdferris
 */
public interface StopSearchService {

  /**
   * Search for stop ids by stop code (see {@link Stop#getCode()}). Typically
   * default to a search against {@link Stop#getId()} if no code is specified
   * for a stop.
   * 
   * @param code the stop code query
   * @param maxResultCount maximum number of results to return
   * @param minScoreToKeep implementation-specific score cutoff for search
   *          results
   * @return a search result for matching stop ids
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForStopsByCode(String code,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException;

  /**
   * Search for stop ids by stop name (see {@link Stop#getName()})
   * 
   * @param name the stop code query
   * @param maxResultCount maximum number of results to return
   * @param minScoreToKeep implementation-specific score cutoff for search
   *          results
   * @return a search result for matching stop ids
   * @throws IOException
   * @throws ParseException
   */
  public SearchResult<AgencyAndId> searchForStopsByName(String name,
      int maxResultCount, double minScoreToKeep) throws IOException,
      ParseException;
}
