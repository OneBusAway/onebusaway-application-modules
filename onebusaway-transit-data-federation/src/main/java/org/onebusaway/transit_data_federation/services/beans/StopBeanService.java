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

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import java.util.List;

public interface StopBeanService {

  /**
   * @param stopId see {@link Stop#getId()}
   * @param serviceInterval
   * @return the populated stop bean, or null if a stop with the specified id
   *         was not found
   * @throws NoSuchStopServiceException if the stop with the specified id could
   *           not be found
   */
  public StopBean getStopForId(AgencyAndId stopId, AgencyServiceInterval serviceInterval);

  /**
   * @param stopId see {@link Stop#getId()}
   * @serviceInterval serviceInterval
   * @return the populated stop bean, or null if a stop with the specified id
   *         was not found
   * @throws NoSuchStopServiceException if the stop with the specified id could
   *           not be found
   */
  public StopBean getStopForIdForServiceDate(AgencyAndId stopId, AgencyServiceInterval serviceInterval);

  public boolean matchesRouteTypeFilter(RouteBean route, List<Integer> routeTypesFilter);
}
