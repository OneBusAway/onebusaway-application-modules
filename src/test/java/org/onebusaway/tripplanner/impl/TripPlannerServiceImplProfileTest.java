package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.Trips;
import org.onebusaway.tripplanner.services.TripPlannerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TripPlannerServiceImplProfileTest extends BaseTest {

  @Autowired
  private TripPlannerService _tripPlanner;

  @Test
  public void go() {

    CoordinatePoint from = new CoordinatePoint(47.668777, -122.290094);
    CoordinatePoint to = new CoordinatePoint(47.609678, -122.337806);

    long startTime = 1224721115008L;
    System.out.println(new Date(startTime));

    TripPlannerConstraints constraints = new TripPlannerConstraints();
    long start = System.currentTimeMillis();
    Trips trips = _tripPlanner.getTrips(from, to, startTime, constraints);
    long stop = System.currentTimeMillis();
    System.out.println(stop - start);

    for (Map.Entry<RouteKey, List<List<TripState>>> entry : trips.getTrips().entrySet()) {
      RouteKey key = entry.getKey();
      System.out.println(key);
    }
  }

}
