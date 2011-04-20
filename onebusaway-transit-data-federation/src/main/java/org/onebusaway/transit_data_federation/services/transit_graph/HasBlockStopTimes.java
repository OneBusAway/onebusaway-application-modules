package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

public interface HasBlockStopTimes {
  public List<BlockStopTimeEntry> getStopTimes();
}
