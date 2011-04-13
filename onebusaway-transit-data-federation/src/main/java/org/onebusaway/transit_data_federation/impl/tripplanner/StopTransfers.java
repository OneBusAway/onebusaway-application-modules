package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

public class StopTransfers {

  private final List<StopTransfer> transfersFromStop;

  private final List<StopTransfer> transfersToStop;

  public StopTransfers(List<StopTransfer> transfersFromStop,
      List<StopTransfer> transfersToStop) {
    this.transfersFromStop = transfersFromStop;
    this.transfersToStop = transfersToStop;
  }

  public List<StopTransfer> getTransfersFromStop() {
    return transfersFromStop;
  }

  public List<StopTransfer> getTransfersToStop() {
    return transfersToStop;
  }
}
