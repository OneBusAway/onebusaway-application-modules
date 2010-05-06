package org.onebusaway.tripplanner.impl;

import org.onebusaway.tripplanner.services.MinTransitTimeEstimationStrategy;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import java.util.Map;

public class MinTransitTimeDijkstraImplTest extends AbstractMinTransitTimeEstimationStrategy {

  @Override
  public MinTransitTimeEstimationStrategy createStrategy(TripPlannerGraph graph, Map<String, Integer> start,
      double maxVelocity, double walkingVelocity) {
    MinTransitTimeDijkstraImpl m = new MinTransitTimeDijkstraImpl(graph, start);
    m.setWalkingVelocity(walkingVelocity);
    return m;
  }

}
