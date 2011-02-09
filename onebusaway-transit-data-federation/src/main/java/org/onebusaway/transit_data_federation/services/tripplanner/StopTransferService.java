package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface StopTransferService {
  public List<StopTransfer> getTransfersFromStop(StopEntry stop);
  public List<StopTransfer> getTransfersToStop(StopEntry stop);
}
