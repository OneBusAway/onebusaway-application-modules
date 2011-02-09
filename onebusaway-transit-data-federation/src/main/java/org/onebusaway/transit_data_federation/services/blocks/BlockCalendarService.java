package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Methods for determining which {@link BlockInstance} instances are active for
 * given time ranges and other criteria. Note that this only considers SCHEDULE
 * data, not real-time data, when determining which blocks are active at a
 * particular point in time.
 * 
 * @author bdferris
 * @see BlockInstance
 * @see BlockTripIndex
 * @see FrequencyBlockTripIndex
 */
public interface BlockCalendarService {

  /**
   * Returns the {@link BlockInstance} for the block active on the specified
   * service date. Note that this function assumes an EXACT service date match.
   * If you aren't quite sure what your service date is, try the
   * {@link #getActiveBlocks(AgencyAndId, long, long)} method.
   * 
   * @param blockId
   * @param serviceDate
   * @return the block instance, or null if not found
   */
  public BlockInstance getBlockInstance(AgencyAndId blockId, long serviceDate);

  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo);

  public List<BlockInstance> getClosestActiveBlocks(AgencyAndId blockId,
      long time);

  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksInTimeRange(
      Iterable<BlockTripIndex> indices,
      Iterable<BlockLayoverIndex> layoverIndices, Iterable<FrequencyBlockTripIndex> frequencyIndices,
      long timeFrom, long timeTo);
}
