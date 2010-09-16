package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;

public interface ActiveCalendarService {

  public BlockInstance getActiveBlock(AgencyAndId blockId, long serviceDate,
      long time);

  public List<BlockInstance> getActiveBlocks(AgencyAndId blockId,
      long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, long timeFrom, long timeTo);

  public List<BlockInstance> getActiveBlocksInTimeRange(
      List<BlockIndex> indices, long timeFrom, long timeTo);
}
