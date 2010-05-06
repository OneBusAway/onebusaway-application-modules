package org.onebusaway.oba.impl;

import org.onebusaway.oba.services.OneBusAwayUserSession;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.tripplanner.services.StopProxy;

import java.util.Map;

class OneBusAwayUserSessionImpl implements OneBusAwayUserSession {

  private String _requestId;

  private OneBusAwayConstraintsBean _constraints;

  private Map<StopProxy, Long> _transitTimes;

  public String getResultId() {
    return _requestId;
  }

  public OneBusAwayConstraintsBean getConstraints() {
    return _constraints;
  }

  public void setOneBusAwayResults(String key, OneBusAwayConstraintsBean constraints, Map<StopProxy, Long> transitTimes) {
    _requestId = key;
    _constraints = constraints;
    _transitTimes = transitTimes;

  }

  public Map<StopProxy, Long> getTransitTimes() {
    return _transitTimes;
  }
}
