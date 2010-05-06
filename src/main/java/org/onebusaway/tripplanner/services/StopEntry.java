package org.onebusaway.tripplanner.services;

import org.onebusaway.tripplanner.model.StopIdsWithValues;

public interface StopEntry {

  public StopProxy getProxy();

  public StopTimeIndex getStopTimes();

  public StopIdsWithValues getTransfers();

  public StopIdsWithValues getPreviousStopsWithMinTimes();

  public StopIdsWithValues getNextStopsWithMinTimes();
}
