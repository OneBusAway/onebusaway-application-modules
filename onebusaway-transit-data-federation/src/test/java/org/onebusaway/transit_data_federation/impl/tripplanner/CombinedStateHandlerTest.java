package org.onebusaway.transit_data_federation.impl.tripplanner;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlansImpl;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripContext;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkFromStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkNode;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;

public class CombinedStateHandlerTest {

  private TripContext _context;

  private TripPlannerConstants _constants;

  private TripPlannerConstraints _constraints;

  private TripPlannerGraph _graph;

  private WalkPlannerService _walkPlannerService;

  private CombinedStateHandler _handler;

  private HashSet<TripState> _results;

  @Before
  public void setup() {
    _context = new TripContext();
    _constants = new TripPlannerConstants();
    _context.setConstants(_constants);

    _constraints = new TripPlannerConstraints();
    _context.setConstraints(_constraints);

    _graph = Mockito.mock(TripPlannerGraph.class);
    _context.setGraph(_graph);

    _walkPlannerService = Mockito.mock(WalkPlannerService.class);
    _context.setWalkPlannerService(_walkPlannerService);

    _handler = new CombinedStateHandler(_context);
    _results = new HashSet<TripState>();
  }

  @Test
  public void testGetForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetReverseTransitionsTripStateSetOfTripState() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetStartForwardTransitions() throws NoPathException {

    // We're going to test a transition from the start state to a stop and to
    // and endpoint

    // Setup

    long startTime = System.currentTimeMillis();
    CoordinatePoint startPoint = new CoordinatePoint(47.5, -122.5);
    StartState state = new StartState(startTime, startPoint);

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(startPoint,
        _constants.getMaxTransferDistance());

    StopEntry stopEntry = mockStopEntry(new AgencyAndId("1", "stopIdA"),
        47.501, -122.501);
    CoordinatePoint stopLocation = stopEntry.getStopLocation();
    Mockito.when(_graph.getStopsByLocation(bounds)).thenReturn(
        Arrays.asList(stopEntry));

    WalkPlan walkToStopPlan = new WalkPlan(Arrays.asList(walkNode(startPoint),
        walkNode(stopLocation)));
    Mockito.when(_walkPlannerService.getWalkPlan(startPoint, stopLocation)).thenReturn(
        walkToStopPlan);

    CoordinatePoint endPoint = new CoordinatePoint(47.501, -122.501);

    WalkPlan walkToEndPlan = new WalkPlan(Arrays.asList(walkNode(startPoint),
        walkNode(endPoint)));
    _handler.setEndPointWalkPlans(endPoint, walkToEndPlan, null);

    // Compute Transitions

    _handler.getStartForwardTransitions(state, _results);

    // Evaluation

    WalkPlansImpl plans = _context.getWalkPlans();

    assertEquals(2, _results.size());

    WalkToStopState walkToStopState = getResultOfType(WalkToStopState.class);
    assertEquals(stopEntry, walkToStopState.getStop());
    long walkToStopTime = (long) (startTime + walkToStopPlan.getDistance()
        / _constants.getWalkingVelocity());
    assertEquals(walkToStopTime, walkToStopState.getCurrentTime());

    assertEquals(walkToStopPlan, plans.getWalkPlan(state, walkToStopState));

    EndState endState = getResultOfType(EndState.class);
    assertEquals(endPoint, endState.getLocation());
    long walkToEndTime = (long) (startTime + walkToEndPlan.getDistance()
        / _constants.getWalkingVelocity());
    assertEquals(walkToEndTime, endState.getCurrentTime());

    assertEquals(walkToEndPlan, plans.getWalkPlan(state, endState));
  }

  @Test
  public void testGetWalkFromStopForwardTransitions() {

    // Setup

    StopEntry stopEntryB = mockStopEntry(new AgencyAndId("1", "stopIdB"),
        47.501, -122.501);
    StopEntry stopEntryA = mockStopEntry(new AgencyAndId("1", "stopIdA"), 47.5,
        -122.5, stopEntryB);

    CoordinatePoint stopLocationA = stopEntryA.getStopLocation();
    CoordinatePoint stopLocationB = stopEntryB.getStopLocation();

    long startTime = System.currentTimeMillis();
    WalkFromStopState state = new WalkFromStopState(startTime, stopEntryA);

    WalkPlan walkToStopPlan = new WalkPlan(Arrays.asList(
        walkNode(stopLocationA), walkNode(stopLocationB)));

    CoordinatePoint endPoint = new CoordinatePoint(47.501, -122.501);

    WalkPlan walkToEndPlan = new WalkPlan(Arrays.asList(
        walkNode(stopLocationA), walkNode(endPoint)));
    Map<StopEntry, WalkPlan> walkFromStopToEndpointPlans = new HashMap<StopEntry, WalkPlan>();
    walkFromStopToEndpointPlans.put(stopEntryA, walkToEndPlan);
    _handler.setEndPointWalkPlans(endPoint, null, walkFromStopToEndpointPlans);

    // Compute Transitions

    _handler.getWalkFromStopForwardTransitions(state, _results);

    // Evaluation

    WalkPlansImpl plans = _context.getWalkPlans();

    assertEquals(2, _results.size());

    WalkToStopState walkToStopState = getResultOfType(WalkToStopState.class);
    assertEquals(stopEntryB, walkToStopState.getStop());
    int d = (int) walkToStopPlan.getDistance();
    double walkingTime = d / _constants.getWalkingVelocity();
    long walkToStopTime = (long) (startTime + walkingTime);
    assertEquals(walkToStopTime, walkToStopState.getCurrentTime(), 100);

    EndState endState = getResultOfType(EndState.class);
    assertEquals(endPoint, endState.getLocation());
    long walkToEndTime = (long) (startTime + walkToEndPlan.getDistance()
        / _constants.getWalkingVelocity());
    assertEquals(walkToEndTime, endState.getCurrentTime());

    assertEquals(walkToEndPlan, plans.getWalkPlan(state, endState));
  }

  @Test
  public void testGetWalkFromStopReverseTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetWalkToStopForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetWalkToStopReverseTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetWaitingAtStopForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetReverseTransitionsWaitingAtStopStateSetOfTripState() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetVehicleDepartureOrContinuationForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetVehicleDepartureReverseTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetVehicleContinuationAndArrivalReverseTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetVehicleArrivalForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetBlockTransferForwardTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetBlockTransferReverseTransitions() {
    // fail("Not yet implemented");
  }

  @Test
  public void testGetEndReverseTransitions() {
    // fail("Not yet implemented");
  }

  /****
   * Private Methods
   ****/

  private WalkNode walkNode(CoordinatePoint p) {
    return new WalkNode(ProjectedPointFactory.forward(p));
  }

  private StopEntry mockStopEntry(AgencyAndId stopId, double lat, double lon,
      StopEntry... transfers) {
    StopEntryImpl stop = new StopEntryImpl(stopId, lat, lon);
    for (StopEntry transfer : transfers) {
      CoordinatePoint transferStopLocation = transfer.getStopLocation();
      double distance = SphericalGeometryLibrary.distance(transferStopLocation,
          stop.getStopLocation());
      //stop.addTransfer(transfer, distance);
    }
    return stop;
  }

  @SuppressWarnings("unchecked")
  private <T extends TripState> T getResultOfType(Class<T> stateType) {
    for (TripState state : _results) {
      if (stateType.isAssignableFrom(state.getClass()))
        return (T) state;
    }
    return null;
  }
}
