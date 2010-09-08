package org.onebusaway.transit_data_federation.services.realtime;

import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockEntry;

public interface ActiveCalendarService {
  public List<BlockInstance> getActiveBlocksInTimeRange(List<BlockEntry> blocks, Date timeFrom, Date timeTo);
}
