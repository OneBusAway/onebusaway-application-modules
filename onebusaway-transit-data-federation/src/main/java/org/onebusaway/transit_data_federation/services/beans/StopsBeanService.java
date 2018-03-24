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
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;

/**
 * Service methods for retrieving lists of stops, usually with a search query.
 * Note that a {@link StopBean} is our portable representation of a {@link Stop}
 * object.
 * 
 * @author bdferris
 * @see StopBean
 * @see Stop
 */
public interface StopsBeanService {

  /**
   * TODO: Convert this to use {@list ListBean} at some point?
   * 
   * @param query the stop search query
   * @return stops that match the specified query
   * @throws ServiceException
   */
  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  /**
   * 
   * @param agencyId see {@link Agency#getId()}
   * @return the list of all stops for the specified agency
   */
  public ListBean<String> getStopsIdsForAgencyId(String agencyId);

  /**
   * search for a stop based on name.
   * @param stopName
   * @return the list of all stops that are close to the given name
   * @throws ServiceException
   */
  public StopsBean getStopsByName(String stopName) throws ServiceException;
}
