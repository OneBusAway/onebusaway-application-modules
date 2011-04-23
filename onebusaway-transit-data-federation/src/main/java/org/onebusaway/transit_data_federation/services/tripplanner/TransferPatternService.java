package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Collection;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferTree;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPatternService {

  /**
   * 
   * @return true is transfer path functionality is enabled
   */
  public boolean isEnabled();

  public Collection<TransferTree> getTransferPatternForStops(StopEntry stopFrom,
      StopEntry stopTo);
  
  public Collection<TransferTree> getTransferPatternForStops(StopEntry stopFrom,
      Iterable<StopEntry> stopsTo);
}
