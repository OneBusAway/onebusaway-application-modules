package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.TPRemainingWeightHeuristicImpl;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkToStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPQueryData;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopSequenceIndex;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;

@Component
class ItinerariesServiceImpl implements ItinerariesService {

  private TransitGraphDao _transitGraphDao;

  private double _walkSearchInitialRadius = 800;

  private double _walkSearchStep = 500;

  private double _walkSearchMaxRadius = 2500;

  private PathService _pathService;

  private GraphService _graphService;

  private StreetVertexIndexService _streetVertexIndexService;

  private BlockIndexService _blockIndexService;

  private TransferPatternService _transferPathService;

  private OTPConfigurationService _configService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
  }

  @Autowired
  public void setGraphService(GraphService graphService) {
    _graphService = graphService;
  }

  @Autowired
  public void setStreetVertexIndexService(
      StreetVertexIndexService streetVertexIndexService) {
    _streetVertexIndexService = streetVertexIndexService;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setTransf(TransferPatternService transferPathService) {
    _transferPathService = transferPathService;
  }

  @Autowired
  public void set(OTPConfigurationService configService) {
    _configService = configService;
  }

  /****
   * {@link ItinerariesService} Interface
   ****/

  public List<GraphPath> getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, TargetTime targetTime, OBATraverseOptions options)
      throws ServiceException {

    Vertex fromVertex = getTransitLocationAsVertex(from, options);
    Vertex toVertex = getTransitLocationAsVertex(to, options);

    Date t = new Date(targetTime.getTargetTime());

    if (_transferPathService.isEnabled()) {
      return getTransferPatternStops(fromVertex, toVertex, t, options);
    } else {
      return _pathService.plan(fromVertex, toVertex, t, options, 1);
    }
  }

  @Override
  public GraphPath getWalkingItineraryBetweenStops(StopEntry fromStop,
      StopEntry toStop, Date time, TraverseOptions options) {

    WalkFromStopVertex fromVertex = getWalkFromStopVertexForStop(fromStop);
    WalkToStopVertex toVertex = getWalkToStopVertexForStop(toStop);

    return getWalkingItineraryBetweenVertices(fromVertex, toVertex, time,
        options);
  }

  /****
   * Private Methods
   ****/

  private Vertex getTransitLocationAsVertex(TransitLocationBean from,
      OBATraverseOptions options) {
    Coordinate c = new Coordinate(from.getLon(), from.getLat());
    return _streetVertexIndexService.getClosestVertex(c, options);
  }

  private List<GraphPath> getTransferPatternStops(Vertex fromVertex,
      Vertex toVertex, Date time, OBATraverseOptions options) {

    /*
     * Map<StopEntry, GraphPath> stopsNearbyFromVertex = getNearbyStops(
     * fromVertex, options, time, true);
     */

    List<StopEntry> stops = getNearbyStops(toVertex, options, time, false);

    if (stops.isEmpty())
      return Collections.emptyList();

    TPQueryData queryData = new TPQueryData(new HashSet<StopEntry>(stops));
    options.putExtension(TPQueryData.class, queryData);

    Graph graph = _graphService.getGraph();
    State init = new State(time.getTime(), new OBAStateData());
    options.remainingWeightHeuristic = new TPRemainingWeightHeuristicImpl();
    ShortestPathTree spt = AStar.getShortestPathTree(graph, fromVertex,
        toVertex, init, options);

    return spt.getPaths(toVertex, false);
  }

  public List<StopEntry> getNearbyStops(Vertex v, TraverseOptions options,
      Date time, boolean isOrigin) {

    double initialRadius = Math.min(_walkSearchInitialRadius,
        options.maxWalkDistance);

    CoordinatePoint location = new CoordinatePoint(v.getY(), v.getX());

    /**
     * These are stops which don't have walking paths
     */
    Set<StopEntry> stopsWithoutPath = new HashSet<StopEntry>();

    for (double radius = initialRadius; radius < _walkSearchMaxRadius; radius += _walkSearchStep) {

      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(location,
          radius);

      List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);

      if (stops.isEmpty())
        continue;

      return stops;

    }

    return Collections.emptyList();
  }

  private Map<StopEntry, GraphPath> computeWalkPathsToStops(Vertex v,
      TraverseOptions options, Date time, boolean isOrigin,
      List<StopEntry> stops, Set<StopEntry> stopsWithoutPath) {

    Map<StopEntry, GraphPath> results = new HashMap<StopEntry, GraphPath>();

    for (StopEntry stop : stops) {

      Vertex stopVertex = isOrigin ? getWalkToStopVertexForStop(stop)
          : getWalkFromStopVertexForStop(stop);

      Vertex fromVertex = isOrigin ? v : stopVertex;
      Vertex toVertex = isOrigin ? stopVertex : v;

      if (stopsWithoutPath.contains(stop))
        continue;

      GraphPath path = getWalkingItineraryBetweenVertices(fromVertex, toVertex,
          time, options);

      if (path == null) {
        stopsWithoutPath.add(stop);
        continue;
      }

      results.put(stop, path);
    }
    return results;
  }

  /**
   * We want to reduce the set of stops we have to try planning itineraries
   * from. The idea here is that we only care about a stop if it gives us access
   * to a {@link BlockSequenceIndex} that we haven't already seen at a closer
   * stop. We order the stops by their path length, and then iterate over the
   * stops in increasing path length, ignoring a stop if it doesn't have any
   * {@link BlockSequenceIndex} indices that we haven't seen before.
   * 
   */
  private Map<StopEntry, GraphPath> pruneResults(
      Map<StopEntry, GraphPath> results) {

    List<StopEntry> stops = new ArrayList<StopEntry>(results.keySet());
    Collections.sort(stops, new GraphPathComparator<StopEntry>(results));

    Map<StopEntry, GraphPath> toKeep = new HashMap<StopEntry, GraphPath>();
    Set<BlockSequenceIndex> indicesWeHaveSeen = new HashSet<BlockSequenceIndex>();

    for (Map.Entry<StopEntry, GraphPath> entry : results.entrySet()) {
      StopEntry stop = entry.getKey();
      GraphPath path = entry.getValue();
      List<BlockStopSequenceIndex> indices = _blockIndexService.getStopSequenceIndicesForStop(stop);
      boolean keep = false;
      for (BlockStopSequenceIndex index : indices) {
        if (indicesWeHaveSeen.add(index.getIndex()))
          keep = true;
      }
      if (keep)
        toKeep.put(stop, path);
    }

    return toKeep;
  }

  private WalkFromStopVertex getWalkFromStopVertexForStop(StopEntry stop) {
    Graph graph = _graphService.getGraph();
    String label = WalkFromStopVertex.getVertexLabelForStop(stop);
    return (WalkFromStopVertex) graph.getVertex(label);
  }

  private WalkToStopVertex getWalkToStopVertexForStop(StopEntry stop) {
    Graph graph = _graphService.getGraph();
    String label = WalkToStopVertex.getVertexLabelForStop(stop);
    return (WalkToStopVertex) graph.getVertex(label);
  }

  private GraphPath getWalkingItineraryBetweenVertices(Vertex fromVertex,
      Vertex toVertex, Date time, TraverseOptions options) {

    options = options.clone();

    /**
     * Set walk only
     */
    TraverseModeSet modes = new TraverseModeSet(TraverseMode.WALK);
    options.setModes(modes);

    List<GraphPath> paths = _pathService.plan(fromVertex, toVertex, time,
        options, 1);

    if (CollectionsLibrary.isEmpty(paths))
      return null;

    return paths.get(0);
  }

  /****
   * 
   ****/

  private static class GraphPathComparator<T> implements Comparator<T> {

    private final Map<T, GraphPath> _paths;

    public GraphPathComparator(Map<T, GraphPath> paths) {
      _paths = paths;
    }

    @Override
    public int compare(T o1, T o2) {
      GraphPath p1 = _paths.get(o1);
      GraphPath p2 = _paths.get(o2);

      double d1 = getDurationOfPath(p1);
      double d2 = getDurationOfPath(p2);

      return Double.compare(d1, d2);
    }

    private double getDurationOfPath(GraphPath path) {
      Vector<SPTVertex> vertices = path.vertices;
      if (vertices.isEmpty())
        return 0;
      long t1 = vertices.get(0).state.getTime();
      long t2 = vertices.get(vertices.size() - 1).state.getTime();
      return Math.abs(t2 - t1);
    }
  }
}
