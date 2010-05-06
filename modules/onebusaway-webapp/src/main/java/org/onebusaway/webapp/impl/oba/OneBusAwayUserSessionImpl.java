package org.onebusaway.webapp.impl.oba;

import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.services.oba.OneBusAwayUserSession;

class OneBusAwayUserSessionImpl implements OneBusAwayUserSession {

  private String _requestId;

  private OneBusAwayConstraintsBean _constraints;

  private int _timeSegmentSize;

  private MinTravelTimeToStopsBean _minTravelTimeToStops;

  public void setOneBusAwayResults(String key,
      OneBusAwayConstraintsBean constraints, int timeSegmentSize,
      MinTravelTimeToStopsBean minTravelTimeToStops) {
    _requestId = key;
    _constraints = constraints;
    _timeSegmentSize = timeSegmentSize;
    _minTravelTimeToStops = minTravelTimeToStops;
  }

  public void clear() {
    _requestId = null;
    _constraints = null;
    _timeSegmentSize = 0;
    _minTravelTimeToStops = null;
  }

  public String getResultId() {
    return _requestId;
  }

  public OneBusAwayConstraintsBean getConstraints() {
    return _constraints;
  }

  public int getTimeSegmentSize() {
    return _timeSegmentSize;
  }

  public MinTravelTimeToStopsBean getMinTravelTimeToStops() {
    return _minTravelTimeToStops;
  }
}