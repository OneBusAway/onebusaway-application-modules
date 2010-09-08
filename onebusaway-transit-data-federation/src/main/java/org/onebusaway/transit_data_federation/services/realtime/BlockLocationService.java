package org.onebusaway.transit_data_federation.services.realtime;

/**
 * Service methods for accessing/interpolating the position of a transit vehicle
 * give a trip instance and target time.
 * 
 * @author bdferris
 * 
 */
public interface BlockLocationService {

  /**
   * Given a trip instance and a target time, determine the vehicle position at
   * that time. Use real-time data when available, but otherwise provide
   * schedule position data.
   * 
   * @param tripInstance the trip instance to query
   * @param targetTime the target time (Unix-time)
   * @return the trip position
   */
  public BlockLocation getPositionForBlockInstance(BlockInstance blockInstance,
      long targetTime);
}
