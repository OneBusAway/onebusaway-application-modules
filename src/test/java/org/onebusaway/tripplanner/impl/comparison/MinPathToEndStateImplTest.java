package org.onebusaway.tripplanner.impl.comparison;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.TripStats;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.offline.TripPlannerGraphsForTesting;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MinPathToEndStateImplTest {

  // That's ~ 2.045 mph
  private static final double WALKING_VELOCITY = 3.2e-03;

  // Min Transit Time
  private static final long MIN_TRANSFER_TIME = 3 * 60 * 1000;

  @Test
  public void testWithJustOneExitPoint() {

    long serviceDate = new Date().getTime();

    TripPlannerGraph graph = TripPlannerGraphsForTesting.createGraphA();
    ScoringImpl scoring = new ScoringImpl();

    double walkingDistance = 200.0;
    long walkingTime = (long) (walkingDistance / WALKING_VELOCITY);

    Map<String, Double> initial = new HashMap<String, Double>();
    initial.put("N22", walkingDistance);
    // initial.put("W22", walkingDistance);
    // initial.put("S22", walkingDistance);
    // initial.put("E22", walkingDistance);

    MinPathToEndStateImpl m = new MinPathToEndStateImpl(graph, scoring, initial, WALKING_VELOCITY, MIN_TRANSFER_TIME);

    try {
      StopProxy stop = graph.getStopEntryByStopId("N22").getProxy();
      EndState state = new EndState(0L, stop.getStopLocation());

      assertEquals(0, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(0, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(0, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(0, stats.getVehicleTime());
      assertEquals(0, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {
      StopProxy stop = graph.getStopEntryByStopId("N22").getProxy();
      WalkFromStopState state = new WalkFromStopState(0, stop);
      assertEquals(walkingTime, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(200, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(0, stats.getVehicleTime());
      assertEquals(0, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      TripEntry trip = graph.getTripEntryByTripId("T00");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(0), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(12) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      TripEntry trip = graph.getTripEntryByTripId("T00");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(1), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(9) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(9), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      TripEntry trip = graph.getTripEntryByTripId("T10");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(0), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(23) + 2 * MIN_TRANSFER_TIME + walk(630);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(230, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(630, stats.getTotalWalkingDistance(), 0);
      assertEquals(2 * MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(23), stats.getVehicleTime());
      assertEquals(3, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E00").getProxy();
      WaitingAtStopState state = new WaitingAtStopState(0, stop);
      double expectedScore = mins(12) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E00").getProxy();
      WalkToStopState state = new WalkToStopState(0, stop);
      double expectedScore = mins(12) + 2 * MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(2 * MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E20").getProxy();
      BlockTransferState state = new BlockTransferState(0, stop.getStopLocation(), "T00", "T01", serviceDate);

      double expectedScore = mins(6) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(200, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(mins(6), stats.getVehicleTime());
      assertEquals(1, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }
  }

  @Test
  public void testWithMultipleExitPoints() {

    long serviceDate = new Date().getTime();

    TripPlannerGraph graph = TripPlannerGraphsForTesting.createGraphA();
    ScoringImpl scoring = new ScoringImpl();

    double walkingDistance = 200.0;
    long walkingTime = (long) (walkingDistance / WALKING_VELOCITY);

    // Note the multiple exit points
    Map<String, Double> initial = new HashMap<String, Double>();
    initial.put("N22", walkingDistance);
    initial.put("W22", walkingDistance);
    initial.put("S22", walkingDistance);
    initial.put("E22", walkingDistance);

    MinPathToEndStateImpl m = new MinPathToEndStateImpl(graph, scoring, initial, WALKING_VELOCITY, MIN_TRANSFER_TIME);

    try {
      StopProxy stop = graph.getStopEntryByStopId("N22").getProxy();
      EndState state = new EndState(0L, stop.getStopLocation());

      assertEquals(0, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(0, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(0, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(0, stats.getVehicleTime());
      assertEquals(0, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {
      StopProxy stop = graph.getStopEntryByStopId("N22").getProxy();
      WalkFromStopState state = new WalkFromStopState(0, stop);
      assertEquals(walkingTime, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(200, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(0, stats.getVehicleTime());
      assertEquals(0, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      TripEntry trip = graph.getTripEntryByTripId("T00");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(0), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(12) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      TripEntry trip = graph.getTripEntryByTripId("T00");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(1), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(9) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(9), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      // The biggest difference between the two test methods is here
      TripEntry trip = graph.getTripEntryByTripId("T10");
      StopTimeInstanceProxy sti = new StopTimeInstanceProxy(trip.getStopTimes().get(0), serviceDate);
      VehicleDepartureState state = new VehicleDepartureState(sti);
      double expectedScore = mins(16) + walk(200);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(200, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(mins(16), stats.getVehicleTime());
      assertEquals(1, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E00").getProxy();
      WaitingAtStopState state = new WaitingAtStopState(0, stop);
      double expectedScore = mins(12) + MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E00").getProxy();
      WalkToStopState state = new WalkToStopState(0, stop);
      double expectedScore = mins(12) + 2 * MIN_TRANSFER_TIME + walk(200) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(400, stats.getTotalWalkingDistance(), 0);
      assertEquals(2 * MIN_TRANSFER_TIME, stats.getTransferWaitingTime());
      assertEquals(mins(12), stats.getVehicleTime());
      assertEquals(2, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }

    try {

      StopProxy stop = graph.getStopEntryByStopId("E20").getProxy();
      BlockTransferState state = new BlockTransferState(0, stop.getStopLocation(), "T00", "T01", serviceDate);

      double expectedScore = mins(6) + walk(walkingDistance);

      assertEquals(expectedScore, m.getMinScoreForTripState(state), 0);

      TripStats stats = m.getMinStatsForTripState(state);

      assertEquals(200, stats.getMaxSingleWalkDistance(), 0);
      assertEquals(200, stats.getTotalWalkingDistance(), 0);
      assertEquals(0, stats.getTransferWaitingTime());
      assertEquals(mins(6), stats.getVehicleTime());
      assertEquals(1, stats.getVehicleCount());
      assertEquals(WALKING_VELOCITY, stats.getWalkingVelocity(), 0);

    } catch (NoPathException e) {
      fail();
    }
  }

  private static class ScoringImpl implements TripStatsScoringStrategy {
    public double getTripScore(TripStats stats) {
      return stats.getTripDuration();
    }
  }

  private static final long mins(int mins) {
    return mins * 60 * 1000;
  }

  private static final double walk(double distance) {
    return distance / WALKING_VELOCITY;
  }
}
