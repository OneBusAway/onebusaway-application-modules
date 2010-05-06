package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.StopEntry;

import java.io.Serializable;

class StopEntryImpl implements StopEntry, Serializable {

  private static final long serialVersionUID = 1L;

  private StopProxyImpl _stop;

  private StopTimeIndexImpl _index;

  private StopIdsWithValuesImpl _transfers = new StopIdsWithValuesImpl();

  private StopIdsWithValuesImpl _prevStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  private StopIdsWithValuesImpl _nextStopsWithMinTravelTime = new StopIdsWithValuesImpl();

  public StopEntryImpl(StopProxyImpl stop, StopTimeIndexImpl index) {
    _stop = stop;
    _index = index;
  }

  public StopProxyImpl getProxy() {
    return _stop;
  }

  public StopTimeIndexImpl getStopTimes() {
    return _index;
  }

  public StopIdsWithValuesImpl getTransfers() {
    return _transfers;
  }

  public void addTransfer(String stopId, double distance) {
    _transfers.setValue(stopId, (int) distance);
  }

  public StopIdsWithValuesImpl getPreviousStopsWithMinTimes() {
    return _prevStopsWithMinTravelTime;
  }

  public void addPreviousStopWithMinTravelTime(String stopId, int travelTime) {
    _prevStopsWithMinTravelTime.setMinValue(stopId, travelTime);
  }

  public StopIdsWithValuesImpl getNextStopsWithMinTimes() {
    return _nextStopsWithMinTravelTime;
  }

  public void addNextStopWithMinTravelTime(String stopId, int travelTime) {
    _nextStopsWithMinTravelTime.setMinValue(stopId, travelTime);
  }
}
