package org.onebusaway.tripplanner.impl;

import org.onebusaway.BaseTest;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WalkToStopState;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class TripPlannerServiceImplTest extends BaseTest {
  
  private static DateFormat _format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  @Autowired
  private TripPlannerServiceImpl _tripPlanner;

  @Test
  public void go() throws ParseException {

    CoordinatePoint from = new CoordinatePoint(47.668777, -122.290094);
    CoordinatePoint to = new CoordinatePoint(47.665502, -122.309546);

    Date time = _format.parse("10/22/08 5:18 PM");

    TripPlannerConstraints constraints = new TripPlannerConstraints();
    constraints.setMinDepartureTime(time.getTime());
    constraints.setMaxDepartureTime(time.getTime() + 30 * 60 * 1000);
    constraints.setMaxSingleWalkDistance(5280 / 2);
    constraints.setMaxTransferCount(1);
    constraints.setMaxTripsPerHour(10);

    long timeStart = System.currentTimeMillis();
    List<TripPlan> plans = _tripPlanner.getTripsBetween(from, to, constraints);
    long timeStop = System.currentTimeMillis();
    System.out.println("total time=" + (timeStop - timeStart) + " stops=" + plans.size());

    for (TripPlan plan : plans) {
      System.out.println(getTripAsStringDescription(plan));
    }
  }

  private String getTripAsStringDescription(TripPlan trip) {
    StringBuilder b = new StringBuilder();
    for (TripState state : trip.getStates()) {
      String token = getTripStateDescription(state);
      if (token != null && token.length() > 0) {
        if (b.length() > 0)
          b.append(", ");
        b.append(token);
      }
    }
    return b.toString();
  }

  private String getTripStateDescription(TripState state) {
    if (state instanceof VehicleDepartureState)
      return state.toString();
    if (state instanceof WalkToStopState)
      return state.toString();
    if (state instanceof VehicleArrivalState)
      return state.toString();
    return null;
  }
}
