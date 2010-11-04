package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

/**
 * Methods for retrieving the scheduled location of a vehicle traveling along a
 * block of trips.
 * 
 * @author bdferris
 * @see ScheduledBlockLocation
 */
public interface ScheduledBlockLocationService {

  /**
   * If you request a schedule time that is less than the first arrival time of
   * the block, we will still return a {@link ScheduledBlockLocation}. If the
   * distance along the block of the first stop is greater than zero, we attempt
   * to interpolate where the bus currently is along the block based on the
   * relative velocity between the first two stops in the block.
   * 
   * @param stopTimes
   * @param scheduleTime
   * @return the schedule block position
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
      BlockConfigurationEntry blockConfig, int scheduleTime);

  /**
   * 
   * @param stopTimes
   * @param distanceAlongBlock in meters
   * @return the schedule block position, or null if not in service
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromDistanceAlongBlock(
      BlockConfigurationEntry blockConfig, double distanceAlongBlock);
}