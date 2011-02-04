package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;

public class GraphContext {

  private ArrivalAndDepartureService arrivalAndDepartureService;

  private StopTransferService stopTransferService;

  private int stopTimeSearchInterval = 10;

  public StopTransferService getStopTransferService() {
    return stopTransferService;
  }

  public void setStopTransferService(StopTransferService stopTransferService) {
    this.stopTransferService = stopTransferService;
  }

  public ArrivalAndDepartureService getArrivalAndDepartureService() {
    return arrivalAndDepartureService;
  }

  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    this.arrivalAndDepartureService = arrivalAndDepartureService;
  }

  /**
   * 
   * @return time, in minutes
   */
  public int getStopTimeSearchInterval() {
    return stopTimeSearchInterval;
  }

  public void setStopTimeSearchInterval(int stopTimeSearchInterval) {
    this.stopTimeSearchInterval = stopTimeSearchInterval;
  }
}
