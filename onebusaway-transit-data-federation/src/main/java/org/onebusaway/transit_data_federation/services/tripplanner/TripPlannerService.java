package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.Collection;
import java.util.Map;

public interface TripPlannerService {

  public Map<StopEntry, Long> getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints);
  
  public void getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints, MinTravelTimeToStopsListener listener);

  public Collection<TripPlan> getTripsBetween(CoordinatePoint from, CoordinatePoint to, TripPlannerConstraints constraints);
}
