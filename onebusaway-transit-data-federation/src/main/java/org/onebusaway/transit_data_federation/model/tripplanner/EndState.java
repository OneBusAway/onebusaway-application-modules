package org.onebusaway.transit_data_federation.model.tripplanner;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class EndState extends AtLocationState {

  public EndState(long currentTime, CoordinatePoint location) {
    super(currentTime, location);
  }

  @Override
  public String toString() {
    return "end(ts=" + getCurrentTimeString() + ")";
  }
}
