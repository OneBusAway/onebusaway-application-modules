package org.onebusaway.tripplanner.services;

import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.Collection;
import java.util.Map;

public interface TripPlannerService {

  public Map<StopProxy, Long> getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints);

  public Collection<TripPlan> getTripsBetween(CoordinatePoint from, CoordinatePoint to, TripPlannerConstraints constraints);
}
