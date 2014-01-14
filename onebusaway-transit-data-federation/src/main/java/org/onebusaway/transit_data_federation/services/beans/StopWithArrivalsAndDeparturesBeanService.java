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

import java.util.Set;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;

/**
 * Service methods for retrieving information about a stop or stops along with
 * the arrival and departure information for that stop in a specified time
 * range.
 * 
 * @author bdferris
 * @see StopWithArrivalsAndDeparturesBean
 * @see StopsWithArrivalsAndDeparturesBean
 */
public interface StopWithArrivalsAndDeparturesBeanService {

  /**
   * Retrieve information about a stop along with the arrival and departure
   * information for that stop in a specified time range
   * 
   * @param stopId see {@link Stop#getId()}
   * @param timeFrom time range lower bound
   * @param timeTo time range upper bound
   * @return stop with arrival and departure information, or null if not stop is
   *         found
   */
  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(
      AgencyAndId stopId, ArrivalsAndDeparturesQueryBean query);

  /**
   * Retrieve information about stops along with the arrival and departure
   * information for that stop in a specified time range
   * 
   * @param stopIds see {@link Stop#getId()}
   * @param timeFrom time range lower bound
   * @param timeTo time range upper bound
   * @return stops with arrival and departure information
   * @throws NoSuchStopServiceException if one of the specified stops could not
   *           be found
   */
  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> stopIds, ArrivalsAndDeparturesQueryBean query)
      throws NoSuchStopServiceException;
}
