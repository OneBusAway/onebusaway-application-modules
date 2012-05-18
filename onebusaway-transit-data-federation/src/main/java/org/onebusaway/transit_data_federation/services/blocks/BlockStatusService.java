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
package org.onebusaway.transit_data_federation.services.blocks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;

/**
 * Service methods for querying the real-time status and position of a
 * particular block of trips.
 * 
 * @author bdferris
 * @see BlockLocation
 */
public interface BlockStatusService {

  /**
   * When searching for blocks to apply real-time information, we will look back
   * the specified number of seconds for vehicles that are potentially running
   * late.
   * 
   * @return time, in seconds
   */
  public int getRunningLateWindow();

  /**
   * When searching for blocks to apply real-time information, we will look
   * ahead the specified number of seconds for vehicles that are potentially
   * running early.
   * 
   * @return time, in seconds
   */
  public int getRunningEarlyWindow();

  /**
   * @param blockId see {@link Trip#getBlockId()}
   * @param serviceDate the service date the block is operating under
   *          (Unix-time)
   * @param vehicleId TODO
   * @param time the time of operation to query
   * @return the status info for a particular block operating on the specified
   *         service date and time
   */
  public Map<BlockInstance, List<BlockLocation>> getBlocks(AgencyAndId blockId,
      long serviceDate, AgencyAndId vehicleId, long time);

  /**
   * 
   * @param vehicleId
   * @param time
   * @param detailsInclusionBean controls what will be included in the response
   * @return trip details for the trip operated by the specified vehicle at the
   *         specified time, or null if not found
   */
  public BlockLocation getBlockForVehicle(AgencyAndId vehicleId, long time);

  /**
   * Returns all active blocks, both with and without real-time information.
   * 
   * @param time only blocks active at the specified time will be returned
   * @return the list of active blocks at the specified time
   */
  public List<BlockLocation> getAllActiveBlocks(long time);

  /**
   * Returns all active blocks for the specified agency, both with and without
   * real-time information.
   * 
   * @param agencyId only blocks with the specified agency id will be returned
   * @param time only blocks active at the specified time will be returned
   * @return the list of active blocks matching agency query criteria
   */
  public List<BlockLocation> getActiveBlocksForAgency(String agencyId, long time);

  /**
   * Returns all active blocks for the specified route, both with and without
   * real-time information.
   * 
   * @param routeId only blocks with the specified route id will be returned
   * @param time only blocks active at the specified time will be returned
   * @return the list of active blocks matching the route query criteria
   */
  public List<BlockLocation> getBlocksForRoute(AgencyAndId routeId, long time);

  /**
   * 
   * @param index
   * @param timetamps
   * @return
   */
  public Map<BlockInstance, List<List<BlockLocation>>> getBlocksForIndex(
      BlockSequenceIndex index, List<Date> timetamps);

  /**
   * @param query
   * @return the list of active blocks matching the query criteria
   */
  public List<BlockLocation> getBlocksForBounds(CoordinateBounds bounds,
      long time);
}
