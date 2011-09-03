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
package org.onebusaway.transit_data_federation.services.realtime;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;

/**
 * Service methods for accessing/interpolating the position of a transit vehicle
 * give a trip instance and target time.
 * 
 * @author bdferris
 * 
 */
public interface BlockLocationService {

  /**
   * Given a block instance and a target time, determine the vehicle position at
   * that time, using real-time data when available. If multiple vehicles are
   * currently active for a block, we just return the first instance.
   * 
   * @param blockInstance the block instance to query
   * @param time the target time (Unix-time)
   * @return the block location, or null if the block instance is not active at
   *         the specified time
   */
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance,
      TargetTime time);

  public Map<AgencyAndId, List<BlockLocation>> getLocationsForBlockInstance(
      BlockInstance blockInstance, List<Date> times, long currentTime);

  public BlockLocation getLocationForBlockInstanceAndScheduledBlockLocation(
      BlockInstance blockInstance, ScheduledBlockLocation scheduledLocation,
      long targetTime);

  /**
   * Given a block instance and a target time, determine the vehicle locations
   * at that time using real-time data when available.
   * 
   * @param blockInstance the trip instance to query
   * @param time TODO
   * @return the block locations, or empty if no block locations are active at
   *         the specified time
   */
  public List<BlockLocation> getLocationsForBlockInstance(
      BlockInstance blockInstance, TargetTime time);

  /**
   * Determines the scheduled vehicle location of a given block instance at the
   * specified time.
   * 
   * @param blockInstance
   * @param targetTime
   * @return the scheduled location of a particular block instance, or null if
   *         not active at the specified time
   */
  public BlockLocation getScheduledLocationForBlockInstance(
      BlockInstance blockInstance, long targetTime);

  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
      TargetTime time);
}
