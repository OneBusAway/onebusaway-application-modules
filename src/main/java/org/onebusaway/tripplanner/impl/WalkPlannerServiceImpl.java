package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.offline.graph.AStarProblem;
import edu.washington.cs.rse.transit.common.offline.graph.AStarResults;
import edu.washington.cs.rse.transit.common.offline.graph.AStarSearch;
import edu.washington.cs.rse.transit.common.offline.graph.AStarSearch.NoPathToGoalException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.tripplanner.NoPathException;
import org.onebusaway.tripplanner.WalkPlannerService;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.Walk;
import org.onebusaway.tripplanner.model.WalkNode;
import org.onebusaway.tripplanner.model.WalkPlannerGraph;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WalkPlannerServiceImpl implements WalkPlannerService {

  private static GeometryFactory _geometryFactory = new GeometryFactory();

  private static WalkProblem _walkProblem = new WalkProblem();

  private ProjectionService _projection;

  private TripPlannerConstants _constants;

  private WalkPlannerGraph _graph;

  @Autowired
  public void setProjectionService(ProjectionService projection) {
    _projection = projection;
  }

  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public void setWalkPlannerGraph(WalkPlannerGraph graph) {
    _graph = graph;
    _graph.initialize();
  }

  public Walk getWalkPlan(Point from, Point to) throws NoPathException {
    CoordinatePoint latLonFrom = _projection.getPointAsLatLong(from);
    CoordinatePoint latLonTo = _projection.getPointAsLatLong(to);
    return getWalkPlan(latLonFrom, from, latLonTo, to);
  }

  public Walk getWalkPlan(CoordinatePoint latLonFrom, Point from,
      CoordinatePoint latLonTo, Point to) throws NoPathException {

    OnSegment fromSegment = getClosestWalkSegment(latLonFrom, from);
    OnSegment toSegment = getClosestWalkSegment(latLonTo, to);

    if (fromSegment.getNodes().equals(toSegment.getNodes()))
      return new Walk(exportNode(fromSegment), exportNode(toSegment));

    fromSegment.setTarget(toSegment);
    toSegment.setTarget(toSegment);

    try {
      AStarResults<InternalWalkNode> results = AStarSearch.searchWithResults(
          _walkProblem, fromSegment, toSegment);

      LinkedList<WalkNode> path = new LinkedList<WalkNode>();
      InternalWalkNode node = toSegment;
      while (node != null) {
        path.addFirst(exportNode(node));
        node = results.getCameFrom().get(node);
      }

      double distance = results.getGScore().get(toSegment);
      return new Walk(path, distance);

    } catch (NoPathToGoalException e) {
      throw new NoPathException();
    }

  }

  private WalkNode exportNode(InternalWalkNode node) {
    return new WalkNode(node.getLatLon(), node.getLocation());
  }

  private OnSegment getClosestWalkSegment(CoordinatePoint latLon, Point point)
      throws NoPathException {

    double distance = _constants.getInitialMaxDistanceToWalkNode();
    Set<Integer> nodes = getNodesByLocationAndRadius(point, distance);

    while (nodes.isEmpty()) {
      distance *= 2;
      if (distance > _constants.getMaxDistanceToWalkNode())
        throw new NoPathException();
      nodes = getNodesByLocationAndRadius(point, distance);
    }

    Map<Integer, Point> locations = new HashMap<Integer, Point>();

    Set<Pair<Integer>> pairs = new HashSet<Pair<Integer>>();
    Set<Integer> allIds = new HashSet<Integer>();

    for (Integer id : nodes) {
      allIds.add(id);
      for (Integer next : _graph.getNeighbors(id)) {
        Pair<Integer> pair = Pair.createPair(id, next);
        if (id.compareTo(next) > 0)
          pair = pair.swap();
        pairs.add(pair);
        allIds.add(next);
      }
    }

    if (pairs.isEmpty())
      throw new NoPathException();

    for (Integer id : allIds)
      locations.put(id, _graph.getLocationById(id));

    Min<Pair<Integer>> m = new Min<Pair<Integer>>();

    for (Pair<Integer> pair : pairs) {
      Point segmentStart = locations.get(pair.getFirst());
      Point segmentEnd = locations.get(pair.getSecond());
      Coordinate[] cs = {
          segmentStart.getCoordinate(), segmentEnd.getCoordinate()};
      LineString line = _geometryFactory.createLineString(cs);
      double d = point.distance(line);
      m.add(d, pair);
    }

    return new OnSegment(m.getMinElement(), point, latLon);
  }

  private Set<Integer> getNodesByLocationAndRadius(Point point, double distance) {
    Geometry boundary = point.buffer(distance).getBoundary();
    return _graph.getNodesByLocation(boundary);
  }

  private static class WalkProblem implements AStarProblem<InternalWalkNode> {

    public double getDistance(InternalWalkNode from, InternalWalkNode to) {
      return UtilityLibrary.distance(from.getLocation(), to.getLocation());
    }

    public double getEstimatedDistance(InternalWalkNode from,
        InternalWalkNode to) {
      return getDistance(from, to);
    }

    public Collection<InternalWalkNode> getNeighbors(InternalWalkNode node) {
      return node.getNeighbors();
    }
  }

  private interface InternalWalkNode {

    public Point getLocation();

    public CoordinatePoint getLatLon();

    public Collection<InternalWalkNode> getNeighbors();
  }

  private class OnSegment implements InternalWalkNode {

    private Pair<Integer> _nodes;

    private Point _location;

    private OnSegment _target;

    private CoordinatePoint _latLon;

    public OnSegment(Pair<Integer> nodes, Point location, CoordinatePoint latLon) {
      _nodes = nodes;
      _location = location;
      _latLon = latLon;
    }

    public void setTarget(OnSegment target) {
      _target = target;
    }

    public Pair<Integer> getNodes() {
      return _nodes;
    }

    public Point getLocation() {
      return _location;
    }

    public CoordinatePoint getLatLon() {
      return _latLon;
    }

    public List<InternalWalkNode> getNeighbors() {
      List<InternalWalkNode> nodes = new ArrayList<InternalWalkNode>(2);
      nodes.add(new OnNode(_nodes.getFirst(), _target));
      nodes.add(new OnNode(_nodes.getSecond(), _target));
      return nodes;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof OnSegment))
        return false;
      OnSegment seg = (OnSegment) obj;
      return _nodes.equals(seg._nodes) && _location.equals(seg._location);
    }

    @Override
    public int hashCode() {
      return _nodes.hashCode() + _location.hashCode();
    }

  }

  private class OnNode implements InternalWalkNode {

    private Integer _id;
    private OnSegment _target;

    public OnNode(Integer id, OnSegment target) {
      _id = id;
      _target = target;
    }

    public Point getLocation() {
      return _graph.getLocationById(_id);
    }

    public CoordinatePoint getLatLon() {
      return _graph.getLatLonById(_id);
    }

    public Collection<InternalWalkNode> getNeighbors() {

      Set<Integer> neighbors = _graph.getNeighbors(_id);
      List<InternalWalkNode> nodes = new ArrayList<InternalWalkNode>(
          neighbors.size() + 1);

      for (Integer id : neighbors)
        nodes.add(new OnNode(id, _target));
      Pair<Integer> n = _target.getNodes();
      if (_id.equals(n.getFirst()) || _id.equals(n.getSecond()))
        nodes.add(_target);
      return nodes;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof OnNode))
        return false;
      OnNode on = (OnNode) obj;
      return _id.equals(on._id);
    }

    @Override
    public int hashCode() {
      return _id.hashCode();
    }

  }
}
