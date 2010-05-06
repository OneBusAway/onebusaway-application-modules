package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;

public interface MinTransitTimeEstimationStrategy {
  public int getMinTransitTime(String stopId) throws NoPathException;
}
