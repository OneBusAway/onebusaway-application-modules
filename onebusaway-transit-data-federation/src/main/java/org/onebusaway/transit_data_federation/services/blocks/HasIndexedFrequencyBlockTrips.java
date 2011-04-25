package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.transit_data_federation.services.transit_graph.HasBlockStopTimes;

public interface HasIndexedFrequencyBlockTrips {

  public int getStartTimeForIndex(int index);

  public int getEndTimeForIndex(int index);
}
