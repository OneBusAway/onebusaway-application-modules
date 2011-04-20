package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;

public interface HasIndexedBlockStopTimes extends HasBlockStopTimes {

  public int getArrivalTimeForIndex(int index);

  public int getDepartureTimeForIndex(int index);
}
