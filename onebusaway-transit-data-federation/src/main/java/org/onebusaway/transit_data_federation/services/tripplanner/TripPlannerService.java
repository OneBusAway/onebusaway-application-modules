package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Collection;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;

public interface TripPlannerService {

  public Map<StopEntry, Long> getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints);
  
  public void getMinTravelTimeToStopsFrom(CoordinatePoint from, TripPlannerConstraints constraints, MinTravelTimeToStopsListener listener);

  public Collection<TripPlan> getTripsBetween(CoordinatePoint from, CoordinatePoint to, TripPlannerConstraints constraints);
}
