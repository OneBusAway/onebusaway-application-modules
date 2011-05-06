package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.min.MinStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.min.MinTravelEdge;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.opentripplanner.routing.algorithm.EmptyExtraEdgesStrategy;
import org.opentripplanner.routing.algorithm.GenericAStar;
import org.opentripplanner.routing.algorithm.strategies.ExtraEdgesStrategy;
import org.opentripplanner.routing.algorithm.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class MinTravelTimeUsingTransitHeuristic {

  private final RemainingWeightHeuristic _remainingWeightHeuristic = new RemainingWeightHeuristicImpl();

  private final Map<StopEntry, Integer> _minTravelTimeToTargetFromStop = new HashMap<StopEntry, Integer>();

  private final ExtraEdgesStrategy _extraEdgesStrategy = new ExtraEdgesStrategyImpl();

  private final Vertex _target;
  
  private final TraverseOptions _parentOptions;

  private final CustomGraphContext _context;

  private final double _maxTransitSpeed;

  private Map<Vertex, List<Edge>> _extraEdgesToOrigin;

  public MinTravelTimeUsingTransitHeuristic(Vertex target,
      TraverseOptions parentOptions, GraphContext parentContext,
      double maxTransitSpeed) {

    _target = target;
    _parentOptions = parentOptions;
    _context = new CustomGraphContext(parentContext);
    _maxTransitSpeed = maxTransitSpeed;

    _extraEdgesToOrigin = computeExtraEdgesToOrigin(parentOptions,
        parentContext.getTransitGraphDao());
  }

  public int getMinTravelTimeFromStopToTarget(StopEntry stop) {

    /**
     * If we don't have any edges connecting stops to the target vertex, the min
     * travel time will never be found
     */
    if (_extraEdgesToOrigin.isEmpty())
      return -1;
    
    Integer cached = _minTravelTimeToTargetFromStop.get(stop);
    if( cached != null)
        return cached;

    Graph graph = new Graph();
    State init = new State();
    TraverseOptions options = new TraverseOptions();

    options.remainingWeightHeuristic = _remainingWeightHeuristic;
    options.extraEdgesStrategy = _extraEdgesStrategy;
    options.useServiceDays = false;

    MinStopVertex from = new MinStopVertex(_context, stop);

    GenericAStar search = new GenericAStar();
    ShortestPathTree sptTree = search.getShortestPathTree(graph, from, _target,
        init, options);

    GraphPath path = sptTree.getPath(_target);
    return computeAccumulatedTimeAndAddShortcuts(path);
  }

  private int computeAccumulatedTimeAndAddShortcuts(GraphPath path) {

    if (path == null)
      return -1;

    Vector<SPTVertex> vertices = path.vertices;

    long time = -1;
    int accumulatedTime = -1;

    for (int i = vertices.size() - 1; i >= 0; i--) {
      SPTVertex sptv = vertices.get(i);

      State state = sptv.state;
      if (time == -1)
        time = state.getTime();

      accumulatedTime = (int) (Math.abs(time - state.getTime()) / 1000);

      Vertex v = sptv.mirror;

      if (v instanceof MinStopVertex) {
        MinStopVertex msv = (MinStopVertex) v;
        StopEntry stop = msv.getStop();

        _minTravelTimeToTargetFromStop.put(stop, accumulatedTime);
      }
    }
    return accumulatedTime;
  }

  private Map<Vertex, List<Edge>> computeExtraEdgesToOrigin(
      TraverseOptions parentOptions, TransitGraphDao transitGraphDao) {

    CoordinatePoint p = new CoordinatePoint(_target.getY(), _target.getX());

    /**
     * Expand out to find some stops
     */
    for (double radius = 2000; radius < 5000; radius += 1000) {

      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(p, radius);
      List<StopEntry> stops = transitGraphDao.getStopsByLocation(bounds);

      if (!stops.isEmpty()) {

        Map<Vertex, List<Edge>> edges = new HashMap<Vertex, List<Edge>>();

        for (StopEntry stop : stops) {
          double d = SphericalGeometryLibrary.distance(stop.getStopLat(),
              stop.getStopLon(), p.getLat(), p.getLon());
          int travelTime = (int) (d / parentOptions.speed);
          MinStopVertex v = new MinStopVertex(_context, stop);
          Edge edge = new MinTravelEdge(_context, v, _target, travelTime);
          edges.put(v, Arrays.asList(edge));
        }

        return edges;
      }
    }

    return Collections.emptyMap();
  }

  public class CustomGraphContext extends GraphContext {

    public CustomGraphContext(GraphContext parentContext) {
      super(parentContext);
    }

    public Vertex getTarget() {
      return _target;
    }

    public Integer getMinTravelTimeToTargetForStop(StopEntry stop) {
      return _minTravelTimeToTargetFromStop.get(stop);
    }
    
    public TraverseOptions getOptions() {
      return _parentOptions;
    }
  }

  private class RemainingWeightHeuristicImpl implements
      RemainingWeightHeuristic {

    @Override
    public double computeInitialWeight(Vertex from, Vertex to,
        TraverseOptions traverseOptions) {
      return weight(from, to);
    }

    @Override
    public double computeForwardWeight(SPTVertex from, Edge edge,
        TraverseResult traverseResult, Vertex target) {
      return weight(from.mirror, target);
    }

    @Override
    public double computeReverseWeight(SPTVertex from, Edge edge,
        TraverseResult traverseResult, Vertex target) {
      return weight(from.mirror, target);
    }

    private double weight(Vertex from, Vertex to) {
      double d = SphericalGeometryLibrary.distance(from.getY(), from.getX(),
          to.getY(), to.getX());
      return d / _maxTransitSpeed;
    }
  }

  private class ExtraEdgesStrategyImpl extends EmptyExtraEdgesStrategy {

    @Override
    public void addOutgoingEdgesForTarget(Map<Vertex, List<Edge>> extraEdges,
        Vertex target) {
      extraEdges.putAll(_extraEdgesToOrigin);
    }
  }
}
