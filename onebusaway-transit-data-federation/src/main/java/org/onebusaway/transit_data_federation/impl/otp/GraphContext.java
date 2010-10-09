package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.StopTimeService;

public class GraphContext {

  private StopTimeService stopTimeService;

  public StopTimeService getStopTimeService() {
    return stopTimeService;
  }

  public void setStopTimeService(StopTimeService stopTimeService) {
    this.stopTimeService = stopTimeService;
  }
}
