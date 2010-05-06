package org.onebusaway.tripplanner.impl;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.common.impl.UtilityLibrary.point;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WaitingAtStopState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.offline.TripPlannerGraphsForTesting;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import com.vividsolutions.jts.geom.Point;

import org.junit.Test;

import java.util.List;

public class TripPlanStatsMethodsTest {

  private TripContext _context = new TripContext();

  private TripPlannerConstants _constants = new TripPlannerConstants();

  @Test
  public void testPlanA() {

    TripPlanStatsMethods m = new TripPlanStatsMethods(_context);

    WalkPlansImpl walkPlans = new WalkPlansImpl();
    TripPlannerGraph graph = TripPlannerGraphsForTesting.createGraphA();

    long serviceDate = 24 * 60 * 60 * 1000;

    TripEntry tripEntry = graph.getTripEntryByTripId("T00");
    List<StopTimeProxy> stopTimes = tripEntry.getStopTimes();
    StopTimeProxy stopTimeFrom = stopTimes.get(0);
    StopTimeProxy stopTimeTo = stopTimes.get(2);

    StopTimeInstanceProxy stiFrom = new StopTimeInstanceProxy(stopTimeFrom, serviceDate);
    StopTimeInstanceProxy stiTo = new StopTimeInstanceProxy(stopTimeTo, serviceDate);

    StopProxy fromStop = stiFrom.getStop();
    StopProxy toStop = stiTo.getStop();

    Point fromLocation = fromStop.getStopLocation();
    Point toLocation = toStop.getStopLocation();

    double walkDistanceA = 220;
    double walkDistanceB = 440;

    long walkTimeA = (long) (walkDistanceA / _constants.getWalkingVelocity());
    long walkTimeB = (long) (walkDistanceB / _constants.getWalkingVelocity());
    long extraInitialWait = 60 * 1000;

    StartState startState = new StartState(stiFrom.getDepartureTime() - _constants.getMinTransferTime()
        - extraInitialWait - walkTimeA, point(fromLocation.getX(), fromLocation.getX() - walkDistanceA));
    WalkToStopState walkToStopState = new WalkToStopState(stiFrom.getDepartureTime() - _constants.getMinTransferTime()
        - extraInitialWait, fromStop);

    WalkFromStopState walkFromStopState = new WalkFromStopState(stiTo.getArrivalTime(), stiTo.getStop());
    EndState endState = new EndState(stiTo.getArrivalTime() + walkTimeB, point(toLocation.getX(), toLocation.getX()
        + walkDistanceB));

    TripPlan trip = new TripPlan(endState.getCurrentTime(), walkPlans);

    trip.addLastState(startState);
    trip.addLastState(walkToStopState);
    trip.addLastState(new WaitingAtStopState(stiFrom.getDepartureTime() - extraInitialWait, fromStop));
    trip.addLastState(new VehicleDepartureState(stiFrom));
    trip.addLastState(new VehicleArrivalState(stiTo));

    trip.addLastState(walkFromStopState);
    trip.addLastState(endState);

    walkPlans.putWalkDistance(startState, walkToStopState, walkDistanceA);
    walkPlans.putWalkDistance(walkFromStopState, endState, walkDistanceB);

    m.updateTripPlanStatistics(trip);

    assertEquals(walkDistanceA + walkDistanceB, trip.getTotalWalkingDistance(), 0);
    assertEquals(walkDistanceB, trip.getMaxSingleWalkDistance(), 0);

    assertEquals(0, trip.getTransferWaitingTime());
    assertEquals(_constants.getMinTransferTime() + extraInitialWait, trip.getInitialWaitingTime());
    assertEquals(stiTo.getArrivalTime() - stiFrom.getDepartureTime(), trip.getVehicleTime());
    assertEquals(1, trip.getVehicleCount());

    assertEquals(startState.getCurrentTime(), trip.getTripStartTime());
    assertEquals(endState.getCurrentTime(), trip.getTripEndTime());
    assertEquals(endState.getCurrentTime() - startState.getCurrentTime(), trip.getTripDuration());
  }

  @Test
  public void testPlanB() {

    TripPlanStatsMethods m = new TripPlanStatsMethods(_context);

    WalkPlansImpl walkPlans = new WalkPlansImpl();
    TripPlannerGraph graph = TripPlannerGraphsForTesting.createGraphA();

    long serviceDate = 24 * 60 * 60 * 1000;

    TripEntry tripEntryA = graph.getTripEntryByTripId("T00");
    List<StopTimeProxy> stopTimesA = tripEntryA.getStopTimes();
    StopTimeProxy stopTimeFromA = stopTimesA.get(0);
    StopTimeProxy stopTimeToA = stopTimesA.get(2);

    StopTimeInstanceProxy stiFromA = new StopTimeInstanceProxy(stopTimeFromA, serviceDate);
    StopTimeInstanceProxy stiToA = new StopTimeInstanceProxy(stopTimeToA, serviceDate);

    StopProxy fromStopA = stiFromA.getStop();
    StopProxy toStopA = stiToA.getStop();

    TripEntry tripEntryB = graph.getTripEntryByTripId("T01");
    List<StopTimeProxy> stopTimesB = tripEntryB.getStopTimes();
    StopTimeProxy stopTimeFromB = stopTimesB.get(0);
    StopTimeProxy stopTimeToB = stopTimesB.get(2);

    StopTimeInstanceProxy stiFromB = new StopTimeInstanceProxy(stopTimeFromB, serviceDate);
    StopTimeInstanceProxy stiToB = new StopTimeInstanceProxy(stopTimeToB, serviceDate);

    StopProxy fromStopB = stiFromB.getStop();
    StopProxy toStopB = stiToB.getStop();

    double walkDistanceA = 220;
    double walkDistanceAB = UtilityLibrary.distance(toStopA.getStopLocation(), fromStopB.getStopLocation());
    double walkDistanceB = 440;

    long walkTimeA = (long) (walkDistanceA / _constants.getWalkingVelocity());
    long walkTimeAB = (long) (walkDistanceAB / _constants.getWalkingVelocity());
    long walkTimeB = (long) (walkDistanceB / _constants.getWalkingVelocity());
    long extraInitialWait = 60 * 1000;

    Point fromLocation = fromStopA.getStopLocation();

    StartState startState = new StartState(stiFromA.getDepartureTime() - _constants.getMinTransferTime()
        - extraInitialWait - walkTimeA, point(fromLocation.getX(), fromLocation.getX() - walkDistanceA));
    WalkToStopState walkToStopState = new WalkToStopState(stiFromA.getDepartureTime() - _constants.getMinTransferTime()
        - extraInitialWait, fromStopA);
    WaitingAtStopState waitingAtStopStateA = new WaitingAtStopState(stiFromA.getDepartureTime() - extraInitialWait,
        fromStopA);
    VehicleDepartureState departureStateA = new VehicleDepartureState(stiFromA);
    VehicleArrivalState arrivalStateA = new VehicleArrivalState(stiToA);

    WalkFromStopState walkFromStopAState = new WalkFromStopState(stiToA.getArrivalTime(), stiToA.getStop());
    WalkToStopState walkToStopBState = new WalkToStopState(stiToA.getArrivalTime() + walkTimeAB, stiFromB.getStop());

    WaitingAtStopState waitingAtStopStateB = new WaitingAtStopState(stiToA.getArrivalTime() + walkTimeB
        + _constants.getMinTransferTime(), fromStopB);
    VehicleDepartureState departureStateB = new VehicleDepartureState(stiFromB);
    VehicleArrivalState arrivalStateB = new VehicleArrivalState(stiToB);

    WalkFromStopState walkFromStopBState = new WalkFromStopState(stiToB.getArrivalTime(), stiToB.getStop());
    Point toLocation = toStopB.getStopLocation();
    EndState endState = new EndState(stiToB.getArrivalTime() + walkTimeB, point(toLocation.getX(), toLocation.getX()
        + walkDistanceB));

    TripPlan trip = new TripPlan(endState.getCurrentTime(), walkPlans);

    trip.addLastState(startState);
    trip.addLastState(walkToStopState);
    trip.addLastState(waitingAtStopStateA);
    trip.addLastState(departureStateA);
    trip.addLastState(arrivalStateA);
    trip.addLastState(walkFromStopAState);
    trip.addLastState(walkToStopBState);
    trip.addLastState(waitingAtStopStateB);
    trip.addLastState(departureStateB);
    trip.addLastState(arrivalStateB);
    trip.addLastState(walkFromStopBState);
    trip.addLastState(walkFromStopAState);
    trip.addLastState(endState);

    walkPlans.putWalkDistance(startState, walkToStopState, walkDistanceA);
    walkPlans.putWalkDistance(walkFromStopAState, walkToStopBState, walkDistanceAB);
    walkPlans.putWalkDistance(walkFromStopAState, endState, walkDistanceB);

    m.updateTripPlanStatistics(trip);

    assertEquals(walkDistanceA + walkDistanceB + walkDistanceAB, trip.getTotalWalkingDistance(), 0);
    assertEquals(walkDistanceB, trip.getMaxSingleWalkDistance(), 0);

    assertEquals(departureStateB.getCurrentTime() - walkToStopBState.getCurrentTime(), trip.getTransferWaitingTime());
    assertEquals(_constants.getMinTransferTime() + extraInitialWait, trip.getInitialWaitingTime());
    assertEquals(stiToA.getArrivalTime() - stiFromA.getDepartureTime() + stiToB.getArrivalTime()
        - stiFromB.getDepartureTime(), trip.getVehicleTime());
    assertEquals(2, trip.getVehicleCount());

    assertEquals(startState.getCurrentTime(), trip.getTripStartTime());
    assertEquals(endState.getCurrentTime(), trip.getTripEndTime());
    assertEquals(endState.getCurrentTime() - startState.getCurrentTime(), trip.getTripDuration());

  }
}
