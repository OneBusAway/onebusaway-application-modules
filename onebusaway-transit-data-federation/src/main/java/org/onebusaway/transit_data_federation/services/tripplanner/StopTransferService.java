package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

public interface StopTransferService {
  public List<StopTransfer> getTransfersForStop(StopEntry stop);
}
