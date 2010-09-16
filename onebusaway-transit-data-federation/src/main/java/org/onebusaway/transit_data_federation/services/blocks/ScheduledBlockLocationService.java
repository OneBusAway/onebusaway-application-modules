package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

/**
 * Methods for retrieving the scheduled location of a vehicle traveling along a
 * block of trips.
 * 
 * @author bdferris
 * @see ScheduledBlockLocation
 */
public interface ScheduledBlockLocationService {

  /**
   * 
   * @param stopTimes
   * @param scheduleTime
   * @return the schedule block position, or null if not in service
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromScheduledTime(
      List<StopTimeEntry> stopTimes, int scheduleTime);

  /**
   * 
   * @param stopTimes
   * @param distanceAlongBlock in meters
   * @return the schedule block position, or null if not in service
   */
  public ScheduledBlockLocation getScheduledBlockLocationFromDistanceAlongBlock(
      List<StopTimeEntry> stopTimes, double distanceAlongBlock);
}