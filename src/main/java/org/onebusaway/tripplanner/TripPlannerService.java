package org.onebusaway.tripplanner;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.Trips;

import java.util.Map;

public interface TripPlannerService {

  public Trips getTrips(CoordinatePoint from, CoordinatePoint to,
      long startTime, TripPlannerConstraints constraints);

  public Map<Stop, Long> getTrips(CoordinatePoint from, long startTime,
      long endTime);

}
