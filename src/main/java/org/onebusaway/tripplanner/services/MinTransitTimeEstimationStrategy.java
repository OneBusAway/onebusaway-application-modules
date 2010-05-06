package org.onebusaway.tripplanner.services;

public interface MinTransitTimeEstimationStrategy {
  public int getMinTransitTime(String stopId) throws NoPathException;
}
