package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.PointVector;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.tripplanner.impl.AStarSearch.NoPathToGoalException;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.WalkNode;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.WalkPlannerGraph;
import org.onebusaway.tripplanner.services.WalkPlannerService;
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

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private TripPlannerConstants _constants;

  private WalkPlannerGraph _graph;

  public void setWalkPlannerGraph(WalkPlannerGraph graph) {
    _graph = graph;
  }

  public WalkPlan getWalkPlan(Point from, Point to) throws NoPathException {
    CoordinatePoint latLonFrom = _projection.getPointAsLatLong(from);
    CoordinatePoint latLonTo = _projection.getPointAsLatLong(to);
    return getWalkPlan(latLonFrom, from, latLonTo, to);
  }

  public WalkPlan getWalkPlan(CoordinatePoint latLonFrom, Point from,
      CoordinatePoint latLonTo, Point to) throws NoPathException {

    OnSegment fromSegment = getClosestWalkSegment(latLonFrom, from, true);
    OnSegment toSegment = getClosestWalkSegment(latLonTo, to, false);

    if (fromSegment.getNodes().equals(toSegment.getNodes())) {
      LinkedList<WalkNode> nodes = new LinkedList<WalkNode>();
      toSegment.exportNodes(nodes);
      fromSegment.exportNodes(nodes);
      return new WalkPlan(nodes);
    }

    fromSegment.setTarget(toSegment);
    toSegment.setTarget(toSegment);

    try {
      AStarResults<InternalWalkNode> results = AStarSearch.searchWithResults(
          _walkProblem, fromSegment, toSegment);

      LinkedList<WalkNode> path = new LinkedList<WalkNode>();
      InternalWalkNode node = toSegment;
      while (node != null) {
        node.exportNodes(path);
        node = results.getCameFrom().get(node);
      }

      return new WalkPlan(path);

    } catch (NoPathToGoalException e) {
      throw new NoPathException();
    }

  }

  private OnSegment getClosestWalkSegment(CoordinatePoint latLon, Point point,
      boolean forward) throws NoPathException {

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

    Pair<Integer> segment = m.getMinElement();
    Point segmentStart = locations.get(segment.getFirst());
    Point segmentEnd = locations.get(segment.getSecond());
    Point onSegment = projectPointOntoSegment(point, segmentStart, segmentEnd);

    double best = UtilityLibrary.distance(point, onSegment);
    double b = UtilityLibrary.distance(point, segmentStart);
    double c = UtilityLibrary.distance(point, segmentEnd);
    if (b < best) {
      best = b;
      onSegment = segmentStart;
    }
    if (c < best) {
      best = c;
      onSegment = segmentEnd;
    }

    CoordinatePoint latLonOnSegment = _projection.getPointAsLatLong(onSegment);
    return new OnSegment(m.getMinElement(), onSegment, latLonOnSegment, point,
        latLon, forward);
  }

  private Set<Integer> getNodesByLocationAndRadius(Point point, double distance) {
    Geometry boundary = point.buffer(distance).getBoundary();
    return _graph.getNodesByLocation(boundary);
  }

  private Point projectPointOntoSegment(Point point, Point segmentStart,
      Point segmentEnd) {
    PointVector v = new PointVector(point.getX() - segmentStart.getX(),
        point.getY() - segmentStart.getY());
    PointVector line = new PointVector(segmentEnd.getX() - segmentStart.getX(),
        segmentEnd.getY() - segmentStart.getY());
    PointVector proj = line.getProjection(v);
    double x = segmentStart.getX() + proj.getX();
    double y = segmentStart.getY() + proj.getY();
    return _geometryFactory.createPoint(new Coordinate(x, y));
  }

  private static class WalkProblem implements AStarProblem<InternalWalkNode> {

    public double getDistance(InternalWalkNode from, InternalWalkNode to) {
      return UtilityLibrary.distance(from.getLocation(), to.getLocation());
    }

    public double getEstimatedDistance(InternalWalkNode from,
        InternalWalkNode to) {
      return getDistance(from, to);
    }

    public Collection<InternalWalkNode> getNeighbors(InternalWalkNode node, Collection<InternalWalkNode> results) {
      return node.getNeighbors();
    }
  }

  private interface InternalWalkNode {

    public Point getLocation();

    public CoordinatePoint getLatLon();

    public double getOffsetDistance();

    public Collection<InternalWalkNode> getNeighbors();

    public void exportNodes(LinkedList<WalkNode> nodes);
  }

  private class OnSegment implements InternalWalkNode {

    private Pair<Integer> _nodes;

    private Point _point;

    private CoordinatePoint _latLon;

    private double _offsetDistance;

    private Point _pointOffSegment;

    private CoordinatePoint _latLonOffSegment;

    private OnSegment _target;

    private boolean _forward;

    public OnSegment(Pair<Integer> nodes, Point location,
        CoordinatePoint latLon, Point pointOffSegment,
        CoordinatePoint latLonOffSegment, boolean forward) {
      _nodes = nodes;
      _point = location;
      _latLon = latLon;
      _pointOffSegment = pointOffSegment;
      _latLonOffSegment = latLonOffSegment;
      _offsetDistance = UtilityLibrary.distance(_point, _pointOffSegment);
      _forward = forward;
    }

    public void setTarget(OnSegment target) {
      _target = target;
    }

    public Pair<Integer> getNodes() {
      return _nodes;
    }

    public Point getLocation() {
      return _point;
    }

    public CoordinatePoint getLatLon() {
      return _latLon;
    }

    public double getOffsetDistance() {
      return _offsetDistance;
    }

    public Point getLocationOffSegment() {
      return _pointOffSegment;
    }

    public CoordinatePoint getLatLonOffSegment() {
      return _latLonOffSegment;
    }

    public List<InternalWalkNode> getNeighbors() {
      List<InternalWalkNode> nodes = new ArrayList<InternalWalkNode>(2);
      nodes.add(new OnNode(_nodes.getFirst(), _target));
      nodes.add(new OnNode(_nodes.getSecond(), _target));
      return nodes;
    }

    public void exportNodes(LinkedList<WalkNode> nodes) {
      if (_forward) {
        nodes.addFirst(new WalkNode(_latLon, _point));
        nodes.addFirst(new WalkNode(_latLonOffSegment, _pointOffSegment));
      } else {
        nodes.addFirst(new WalkNode(_latLonOffSegment, _pointOffSegment));
        nodes.addFirst(new WalkNode(_latLon, _point));

      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof OnSegment))
        return false;
      OnSegment seg = (OnSegment) obj;
      return _nodes.equals(seg._nodes) && _point.equals(seg._point);
    }

    @Override
    public int hashCode() {
      return _nodes.hashCode() + _point.hashCode();
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

    public double getOffsetDistance() {
      return 0;
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

    public void exportNodes(LinkedList<WalkNode> nodes) {
      nodes.addFirst(new WalkNode(getLatLon(), getLocation()));
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
