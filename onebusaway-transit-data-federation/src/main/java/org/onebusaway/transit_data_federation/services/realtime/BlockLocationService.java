package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;

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
   * that time. Use real-time data when available, but otherwise provide
   * schedule position data. If multiple vehicles are currently active for a
   * block, we just return the first instance.
   * 
   * @param blockInstance the block instance to query
   * @param targetTime the target time (Unix-time)
   * @return the block location
   */
  public BlockLocation getLocationForBlockInstance(BlockInstance blockInstance,
      long targetTime);

  /**
   * Given a block instance and a target time, determine the vehicle locations
   * at that time. Use real-time data when available, but otherwise provide
   * schedule location data.
   * 
   * @param blockInstance the trip instance to query
   * @param targetTime the target time (Unix-time)
   * @return the block locations
   */
  public List<BlockLocation> getLocationsForBlockInstance(
      BlockInstance blockInstance, long targetTime);

  public BlockLocation getLocationForVehicleAndTime(AgencyAndId vehicleId,
      long targetime);
}
