package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.util.Collection;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPattern {

  public StopEntry getOriginStop();

  public Set<StopEntry> getStops();

  public Collection<TransferTreeNode> getTransfersForStop(StopEntry stop,
      TransferTreeNode root);

  public Set<StopEntry> getHubStops();

  public Collection<TransferTreeNode> getTransfersForHubStop(StopEntry stop,
      TransferTreeNode root);
}
