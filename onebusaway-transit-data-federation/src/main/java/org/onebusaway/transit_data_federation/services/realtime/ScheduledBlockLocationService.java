package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

public interface ScheduledBlockLocationService {

  /**
   * 
   * @param stopTimes
   * @param scheduleTime
   * @return the schedule block position, or null if not in service
   */
  public ScheduledBlockLocation getScheduledBlockPositionFromScheduledTime(
      List<StopTimeEntry> stopTimes, int scheduleTime);

  /**
   * 
   * @param stopTimes
   * @param distanceAlongBlock in meters
   * @return the schedule block position, or null if not in service
   */
  public ScheduledBlockLocation getScheduledBlockPositionFromDistanceAlongBlock(
      List<StopTimeEntry> stopTimes, double distanceAlongBlock);
}