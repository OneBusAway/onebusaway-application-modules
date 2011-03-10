package org.onebusaway.transit_data_federation.impl.walkplanner;

import static org.junit.Assert.assertEquals;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.impl.walkplanner.WalkPlannerServiceImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.WalkNodeEntryImpl;
import org.onebusaway.transit_data_federation.impl.walkplanner.offline.WalkPlannerGraphImpl;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkNode;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkPlannerServiceImplTest {

  private static final double GRID_LENGTH = 100.0;

  private static final double ROOT_LAT = 47.675906527237984;

  private static final double ROOT_LON = -122.30049133300781;

  private WalkPlannerServiceImpl _service;

  private Map<String, WalkNodeEntry> _nodes = new HashMap<String, WalkNodeEntry>();

  @Before
  public void setup() {

    WalkPlannerGraphImpl graph = constructExampleGraph();

    _service = new WalkPlannerServiceImpl();
    _service.setWalkPlannerGraph(graph);

    TripPlannerConstants constants = new TripPlannerConstants();
    _service.setTripPlannerConstants(constants);
  }

  @Test
  public void testPathA() throws NoPathException {

    ProjectedPoint from = point(1, 0.5);
    ProjectedPoint to = point(4.5, 4);
    WalkPlan plan = _service.getWalkPlan(from.toCoordinatePoint(),
        to.toCoordinatePoint());

    List<WalkNode> path = plan.getPath();

    assertEquals(642.08, plan.getDistance(), 0.02);
    assertEquals(8, path.size());

    assertEquals(from, path.get(0).getLocation());
    assertEquals(node(1, 1).getLocation(), path.get(1).getLocation());
    assertEquals(node(1, 2).getLocation(), path.get(2).getLocation());
    assertEquals(node(2, 2).getLocation(), path.get(3).getLocation());
    assertEquals(node(3, 3).getLocation(), path.get(4).getLocation());
    assertEquals(node(3, 4).getLocation(), path.get(5).getLocation());
    assertEquals(node(4, 4).getLocation(), path.get(6).getLocation());
    assertEquals(to, path.get(7).getLocation());
  }

  @Test
  public void testPathB() throws NoPathException {

    ProjectedPoint from = point(0.5, 1.5);
    ProjectedPoint to = point(3.5, 4.5);
    WalkPlan plan = _service.getWalkPlan(from.toCoordinatePoint(),
        to.toCoordinatePoint());

    List<WalkNode> path = plan.getPath();

    assertEquals(541.98, plan.getDistance(), 0.02);
    assertEquals(8, path.size());

    assertEquals(from, path.get(0).getLocation());
    assertPointXYEquals(point(1, 1.5), path.get(1).getLocation(), 0.001);
    assertEquals(node(1, 2).getLocation(), path.get(2).getLocation());
    assertEquals(node(2, 2).getLocation(), path.get(3).getLocation());
    assertEquals(node(3, 3).getLocation(), path.get(4).getLocation());
    assertEquals(node(3, 4).getLocation(), path.get(5).getLocation());
    assertPointXYEquals(point(3.5, 4), path.get(6).getLocation(), 0.001);
    assertEquals(to, path.get(7).getLocation());
  }

  private void assertPointXYEquals(ProjectedPoint a, ProjectedPoint b,
      double delta) {
    assertEquals(a.getX(), b.getX(), delta);
    assertEquals(a.getY(), b.getY(), delta);
  }

  @Test
  public void testPathC() throws NoPathException {

    ProjectedPoint from = point(1, 0.5);
    ProjectedPoint to = point(0.5, 0.5);

    WalkPlan plan = _service.getWalkPlan(from.toCoordinatePoint(),
        to.toCoordinatePoint());
    List<WalkNode> path = plan.getPath();

    assertEquals(120.76, plan.getDistance(), 0.02);
    assertEquals(3, path.size());

    assertEquals(from, path.get(0).getLocation());
    assertEquals(node(1, 1).getLocation(), path.get(1).getLocation());
    assertEquals(to, path.get(2).getLocation());
  }

  /****
   * Private Methods
   ****/

  private WalkPlannerGraphImpl constructExampleGraph() {

    WalkPlannerGraphImpl graph = new WalkPlannerGraphImpl(0);

    WalkNodeEntryImpl n11 = node(graph, 1, 1);
    WalkNodeEntryImpl n12 = node(graph, 1, 2);
    WalkNodeEntryImpl n13 = node(graph, 1, 3);
    WalkNodeEntryImpl n21 = node(graph, 2, 1);
    WalkNodeEntryImpl n22 = node(graph, 2, 2);
    WalkNodeEntryImpl n23 = node(graph, 2, 3);
    WalkNodeEntryImpl n24 = node(graph, 2, 4);
    WalkNodeEntryImpl n31 = node(graph, 3, 1);
    WalkNodeEntryImpl n32 = node(graph, 3, 2);
    WalkNodeEntryImpl n33 = node(graph, 3, 3);
    WalkNodeEntryImpl n34 = node(graph, 3, 4);
    WalkNodeEntryImpl n42 = node(graph, 4, 2);
    WalkNodeEntryImpl n43 = node(graph, 4, 3);
    WalkNodeEntryImpl n44 = node(graph, 4, 4);

    join(n11, n12);
    join(n12, n13);
    join(n13, n23);
    join(n23, n24);
    join(n12, n22);
    join(n22, n21);
    join(n21, n31);
    join(n31, n32);
    join(n32, n42);
    join(n42, n43);
    join(n43, n33);
    join(n33, n34);
    join(n34, n44);
    join(n22, n33);

    graph.initialize();

    return graph;
  }

  private WalkNodeEntry node(double x, double y) {
    return _nodes.get(x + " " + y);
  }

  private WalkNodeEntryImpl node(WalkPlannerGraphImpl graph, double x, double y) {
    ProjectedPoint point = point(x, y);
    WalkNodeEntryImpl node = graph.addNode((int) (x * 100 + y), point);
    _nodes.put(x + " " + y, node);
    return node;
  }

  private ProjectedPoint point(double x, double y) {
    CoordinateBounds a = SphericalGeometryLibrary.bounds(ROOT_LAT, ROOT_LON, x
        * GRID_LENGTH);
    CoordinateBounds b = SphericalGeometryLibrary.bounds(ROOT_LAT, ROOT_LON, y
        * GRID_LENGTH);
    double lat = b.getMaxLat();
    double lon = a.getMaxLon();
    CoordinatePoint point = new CoordinatePoint(lat, lon);
    return ProjectedPointFactory.forward(point);
  }

  private void join(WalkNodeEntryImpl a, WalkNodeEntryImpl b) {
    double d = a.getLocation().distance(b.getLocation());
    a.addEdge(b, d);
    b.addEdge(a, d);
  }
}
