package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

public class StopTransfers {

  private final List<StopTransfer> transfersFromStop;

  private final List<StopTransfer> transfersToStop;

  private final List<StopHop> hopsFromStop;

  private final List<StopHop> hopsToStop;

  public StopTransfers(List<StopTransfer> transfersFromStop,
      List<StopTransfer> transfersToStop, List<StopHop> hopsFromStop,
      List<StopHop> hopsToStop) {
    this.transfersFromStop = transfersFromStop;
    this.transfersToStop = transfersToStop;
    this.hopsFromStop = hopsFromStop;
    this.hopsToStop = hopsToStop;
  }

  public List<StopTransfer> getTransfersFromStop() {
    return transfersFromStop;
  }

  public List<StopTransfer> getTransfersToStop() {
    return transfersToStop;
  }

  public List<StopHop> getHopsFromStop() {
    return hopsFromStop;
  }

  public List<StopHop> getHopsToStop() {
    return hopsToStop;
  }
}
