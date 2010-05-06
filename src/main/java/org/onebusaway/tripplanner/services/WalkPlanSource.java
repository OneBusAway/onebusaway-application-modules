package org.onebusaway.tripplanner.services;

import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.WalkPlan;

public interface WalkPlanSource {

  public boolean hasWalkPlan(TripState from, TripState to);

  public WalkPlan getWalkPlan(TripState from, TripState to);

  public boolean hasWalkDistance(TripState from, TripState to);

  public double getWalkDistance(TripState from, TripState to);
}
