package org.onebusaway.transit_data_federation.services.realtime;

import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;

public interface ActiveCalendarService {

  public List<BlockInstance> getActiveBlocksForAgencyInTimeRange(
      String agencyId, Date timeFrom, Date timeTo);

  public List<BlockInstance> getActiveBlocksForRouteInTimeRange(
      AgencyAndId routeId, Date timeFrom, Date timeTo);

  public List<BlockInstance> getActiveBlocksInTimeRange(
      List<BlockIndex> indices, Date timeFrom, Date timeTo);
}
