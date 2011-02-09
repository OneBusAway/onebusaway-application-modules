package org.onebusaway.transit_data_federation.services.walkplanner;

import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;

public interface WalkPlanSource {

  public boolean hasWalkPlan(TripState from, TripState to);

  public WalkPlan getWalkPlan(TripState from, TripState to);

  public boolean hasWalkDistance(TripState from, TripState to);

  public double getWalkDistance(TripState from, TripState to);
}
