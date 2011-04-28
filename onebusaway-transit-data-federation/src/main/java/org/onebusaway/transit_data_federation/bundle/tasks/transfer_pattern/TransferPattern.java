package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPattern {

  public StopEntry getOriginStop();

  public Collection<TransferParent> getTransfersForStops(TransferParent root,
      List<StopEntry> stops);
  
  public Collection<TransferParent> getTransfersForAllStops(TransferParent root);

  public Map<StopEntry, List<TransferParent>> getTransfersForHubStops(
      TransferParent root);
}
