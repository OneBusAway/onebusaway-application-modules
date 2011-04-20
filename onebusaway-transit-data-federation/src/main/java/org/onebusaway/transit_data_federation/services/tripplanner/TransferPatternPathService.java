package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPatternPathService {
  public void leg(StopEntry fromStop, StopEntry toStop, long targetTime);
}
