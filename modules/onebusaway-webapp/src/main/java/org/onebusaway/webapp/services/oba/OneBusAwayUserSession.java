package org.onebusaway.webapp.services.oba;

import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;

public interface OneBusAwayUserSession {

  public void setOneBusAwayResults(String key,
      OneBusAwayConstraintsBean constraints, int timeSegmentSize,
      MinTravelTimeToStopsBean minTravelTimeToStops);

  public String getResultId();

  public OneBusAwayConstraintsBean getConstraints();

  public int getTimeSegmentSize();

  public MinTravelTimeToStopsBean getMinTravelTimeToStops();

  public void clear();
}
