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
   * 
   * @param query
   * @return the list of active blocks matching agency query criteria
   */
  public List<BlockLocation> getBlocksForAgency(String agencyId, long time);

  /**
   * 
   * @param query
   * @return the list of active blocks matching the route query criteria
   */
  public List<BlockLocation> getBlocksForRoute(AgencyAndId routeId, long time);

  /**
   * 
   * @param index
   * @param timetamps
   * @return
   */
  public Map<BlockInstance,List<List<BlockLocation>>> getBlocksForIndex(BlockSequenceIndex index,
      List<Date> timetamps);

  /**
   * @param query
   * @return the list of active blocks matching the query criteria
   */
  public List<BlockLocation> getBlocksForBounds(CoordinateBounds bounds,
      long time);
}
