package org.onebusaway.oba.services;

import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.tripplanner.services.StopProxy;

import java.util.Map;

public interface OneBusAwayUserSession {

  public void setOneBusAwayResults(String key, OneBusAwayConstraintsBean constraints, Map<StopProxy, Long> transitTimes);

  public String getResultId();

  public OneBusAwayConstraintsBean getConstraints();

  public Map<StopProxy, Long> getTransitTimes();

}
