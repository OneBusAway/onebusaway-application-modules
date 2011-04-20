package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPatternService {

  /**
   * 
   * @return true is transfer path functionality is enabled
   */
  public boolean isServiceEnabled();

  List<List<Pair<StopEntry>>> getTransferPatternForStops(StopEntry stopFrom,
      StopEntry stopTo);
}
