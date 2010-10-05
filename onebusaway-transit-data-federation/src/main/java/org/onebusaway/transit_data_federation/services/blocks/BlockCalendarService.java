package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

/**
 * Methods for determining which {@link BlockInstance} instances are active for
 * given time ranges and other criteria. Note that this only considers SCHEDULE
 * data, not real-time data, when determining which blocks are active at a
 * particular point in time.
 * 
 * @author bdferris
 * @see BlockInstance
 * @see BlockIndex
 */
public interface BlockCalendarService {

  /**
   * Returns the {@link BlockInstance} for the block active on the specified
   * service date. Note that this function assumes an EXACT service date match.
   * If you aren't quite sure what your service date is, try the
   * {@link #getActiveBlock(AgencyAndId, long, long)} or
   * {@link #getActiveBlocks(AgencyAndId, long, long)} methods.
   * 
   * @param blockId
   * @param serviceDate
   * @return the block instance, or null if not found
   */
  public BlockInstance getBlockInstance(AgencyAndId blockId, long serviceDate);

  public BlockInstance getActiveBlock(AgencyAndId blockId, long serviceDate,
      long time);

  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksInTimeRange(
      Iterable<BlockIndex> indices, long timeFrom, long timeTo);
}
