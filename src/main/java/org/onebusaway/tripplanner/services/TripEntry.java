package org.onebusaway.tripplanner.services;

import java.util.List;

public interface TripEntry {

  public List<StopTimeProxy> getStopTimes();

  public String getPrevTripId();

  public String getNextTripId();
}
