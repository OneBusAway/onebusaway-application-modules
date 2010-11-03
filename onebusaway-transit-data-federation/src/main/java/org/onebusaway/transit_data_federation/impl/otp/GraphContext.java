package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerPreferences;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;

public class GraphContext {

  private StopTimeService stopTimeService;

  private StopTransferService stopTransferService;

  private int stopTimeSearchInterval = 10;

  private TripPlannerPreferences _preferences = new TripPlannerPreferences();

  public StopTimeService getStopTimeService() {
    return stopTimeService;
  }

  public void setStopTimeService(StopTimeService stopTimeService) {
    this.stopTimeService = stopTimeService;
  }

  public StopTransferService getStopTransferService() {
    return stopTransferService;
  }

  public void setStopTransferService(StopTransferService stopTransferService) {
    this.stopTransferService = stopTransferService;
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

  public TripPlannerPreferences getPreferences() {
    return _preferences;
  }

  public void setPreferences(TripPlannerPreferences preferences) {
    _preferences = preferences;
  }
}
