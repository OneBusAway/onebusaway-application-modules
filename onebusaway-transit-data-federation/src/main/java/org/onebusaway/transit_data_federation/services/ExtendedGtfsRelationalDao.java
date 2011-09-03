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
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

/**
 * Adds a number of additional methods to the {@link GtfsRelationalDao}.
 * 
 * @author bdferris
 * 
 */
public interface ExtendedGtfsRelationalDao extends GtfsRelationalDao {

  /**
   * For each agency in a transit bundle, computes the coordinate bounds of all
   * the stops operated by that agency.
   * 
   * @return the coordinate bounds of all stops operated by each agency, keyed
   *         by agency id
   */
  public Map<String, CoordinateBounds> getAgencyIdsAndBounds();

  /**
   * @param agencyId the target agency
   * 
   * @return the list of all stop ids operated by the specified agency
   */
  public List<AgencyAndId> getStopIdsForAgencyId(String agencyId);

  /**
   * 
   * @param agencyId the target agency
   * @return the list of all route ids operated by the specified agency
   */
  public List<AgencyAndId> getRouteIdsForAgencyId(String agencyId);

  /**
   * @param stop the target stop
   * @return the list of all routes that service a particular stop
   */
  public List<Route> getRoutesForStop(Stop stop);

  /**
   * @param blockId a trip sequence block id
   * @return the list of all stop times that are linked to trips with the
   *         specified block id
   */
  public List<StopTime> getStopTimesForBlockId(AgencyAndId blockId);

  /**
   * @param routes a collection of routes
   * @return the list of all shape point shape ids for all trips linked to the
   *         specified routes
   */
  public List<AgencyAndId> getShapePointIdsForRoutes(Collection<Route> routes);
}
