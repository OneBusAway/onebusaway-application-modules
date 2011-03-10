package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class StartState extends AtLocationState {

  public StartState(long currentTime, CoordinatePoint location) {
    super(currentTime, location);
  }

  public StartState shift(long offset) {
    return new StartState(getCurrentTime() + offset, getLocation());
  }

  @Override
  public String toString() {
    return "start(ts=" + getCurrentTimeString() + ")";
  }
}
