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

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.blocks.BlockDetailsBean;
import org.onebusaway.transit_data.model.blocks.BlockStatusBean;

/**
 * Service methods for querying the real-time status and position of a
 * particular block of trips.
 * 
 * @author bdferris
 * @see StatusBean
 * @see BlockDetailsBean
 */
public interface BlockStatusBeanService {

  /**
   * 
   * @param vehicleId
   * @param time
   * @param detailsInclusionBean controls what will be included in the response
   * @return trip details for the trip operated by the specified vehicle at the
   *         specified time, or null if not found
   */
  public BlockStatusBean getBlockForVehicle(AgencyAndId vehicleId, long time);

  /**
   * 
   * @param query
   * @return the list of active blocks matching agency query criteria
   */
  public ListBean<BlockStatusBean> getBlocksForAgency(String agencyId, long time);

  /**
   * 
   * @param query
   * @return the list of active blocks matching the route query criteria
   */
  public ListBean<BlockStatusBean> getBlocksForRoute(AgencyAndId routeId,
      long time);

  /**
   * @param query
   * @return the list of active blocks matching the query criteria
   */
  public ListBean<BlockStatusBean> getBlocksForBounds(CoordinateBounds bounds,
      long time);
}
