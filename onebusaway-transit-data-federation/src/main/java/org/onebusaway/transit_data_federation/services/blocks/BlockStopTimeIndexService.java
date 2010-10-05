package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;

public interface BlockStopTimeIndexService {
  public List<BlockStopTimeIndex> getStopTimeIndicesForStop(StopEntry stopEntry);
}
