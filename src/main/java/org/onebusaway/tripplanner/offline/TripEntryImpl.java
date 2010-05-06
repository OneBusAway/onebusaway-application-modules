package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class TripEntryImpl implements TripEntry, Serializable {

  private static final long serialVersionUID = 1L;
  private List<StopTimeProxy> _stopTimes;
  private String _prevTripId;
  private String _nextTripId;

  public TripEntryImpl(List<StopTimeProxyImpl> stopTimes) {
    _stopTimes = new ArrayList<StopTimeProxy>(stopTimes);
  }

  public List<StopTimeProxy> getStopTimes() {
    return _stopTimes;
  }

  public String getPrevTripId() {
    return _prevTripId;
  }

  public void setPrevTripId(String prevTripId) {
    _prevTripId = prevTripId;
  }

  public String getNextTripId() {
    return _nextTripId;
  }

  public void setNextTripId(String nextTripId) {
    _nextTripId = nextTripId;
  }
}
