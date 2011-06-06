package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.RemainingWeightHeuristicImpl;
import org.onebusaway.transit_data_federation.impl.otp.SearchTerminationStrategyImpl;
import org.onebusaway.transit_data_federation.impl.otp.TPRemainingWeightHeuristicImpl;
import org.onebusaway.transit_data_federation.impl.otp.TripSequenceShortestPathTree;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkToStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPQueryData;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.algorithm.GenericAStar;
import org.opentripplanner.routing.algorithm.strategies.GenericAStarFactory;
import org.opentripplanner.routing.algorithm.strategies.SkipTraverseResultStrategy;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
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

  private TransferPatternService _transferPathService;

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
  public void setTransf(TransferPatternService transferPathService) {
    _transferPathService = transferPathService;
  }

  /****
   * {@link ItinerariesService} Interface
   ****/

  public List<GraphPath> getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, long targetTime, OBATraverseOptions options)
      throws ServiceException {

    Vertex fromVertex = getTransitLocationAsVertex(from, options);
    Vertex toVertex = getTransitLocationAsVertex(to, options);

    if (fromVertex == null || toVertex == null)
      throw new OutOfServiceAreaServiceException();

    State state = new State(targetTime, new OBAStateData());

    if (_transferPathService.isEnabled()) {
      return getTransferPatternStops(fromVertex, toVertex,
          new Date(targetTime), options);
    } else {

      options.remainingWeightHeuristic = new RemainingWeightHeuristicImpl();
      options.aStarSearchFactory = new GenericAStarFactoryImpl();

      return _pathService.plan(fromVertex, toVertex, state, options, 1);
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

  @Override
  public GraphPath getWalkingItineraryBetweenPoints(CoordinatePoint from,
      CoordinatePoint to, Date time, TraverseOptions options) {

    Coordinate a = new Coordinate(from.getLon(), from.getLat());
    Coordinate b = new Coordinate(to.getLon(), to.getLat());

    Vertex v1 = _streetVertexIndexService.getClosestVertex(a, options);
    Vertex v2 = _streetVertexIndexService.getClosestVertex(b, options);

    if (v1 == null || v2 == null)
      return null;

    return getWalkingItineraryBetweenVertices(v1, v2, time, options);
  }

  @Override
  public GraphPath getWalkingItineraryBetweenVertices(Vertex from, Vertex to,
      Date time, TraverseOptions options) {

    options = options.clone();
    
    /**
     * Set walk only
     */
    TraverseModeSet modes = new TraverseModeSet(TraverseMode.WALK);
    options.setModes(modes);

    State state = new State(time.getTime(), new OBAStateData());

    List<GraphPath> paths = _pathService.plan(from, to, state, options, 1);

    if (CollectionsLibrary.isEmpty(paths))
      return null;

    return paths.get(0);
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

    Set<StopEntry> sourceStops = Collections.emptySet();
    Set<StopEntry> destStops = Collections.emptySet();

    if (options.isArriveBy()) {
      List<StopEntry> stops = getNearbyStops(fromVertex, options, time, true);
      sourceStops = new HashSet<StopEntry>(stops);
      if (sourceStops.isEmpty())
        return Collections.emptyList();
    } else {
      List<StopEntry> stops = getNearbyStops(toVertex, options, time, false);
      destStops = new HashSet<StopEntry>(stops);
      if (destStops.isEmpty())
        return Collections.emptyList();
    }

    TPQueryData queryData = new TPQueryData(sourceStops, destStops);
    options.putExtension(TPQueryData.class, queryData);

    Graph graph = _graphService.getGraph();
    State init = new State(time.getTime(), new OBAStateData());
    options.remainingWeightHeuristic = new TPRemainingWeightHeuristicImpl();
    GenericAStar search = new GenericAStar();
    search.setSkipTraverseResultStrategy(new SkipVertexImpl());
    search.setSearchTerminationStrategy(new SearchTerminationStrategyImpl());
    search.setShortestPathTreeFactory(TripSequenceShortestPathTree.FACTORY);

    if (options.isArriveBy()) {
      ShortestPathTree spt = search.getShortestPathTree(graph, toVertex,
          fromVertex, init, options);
      List<GraphPath> paths = spt.getPaths(fromVertex, true);
      for (GraphPath path : paths)
        path.reverse();
      return paths;
    } else {
      ShortestPathTree spt = search.getShortestPathTree(graph, fromVertex,
          toVertex, init, options);
      return spt.getPaths(toVertex, true);
    }
  }

  public List<StopEntry> getNearbyStops(Vertex v, TraverseOptions options,
      Date time, boolean isOrigin) {

    double initialRadius = Math.min(_walkSearchInitialRadius,
        options.maxWalkDistance);

    CoordinatePoint location = new CoordinatePoint(v.getY(), v.getX());

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

  private static class SkipVertexImpl implements SkipTraverseResultStrategy {

    @Override
    public boolean shouldSkipTraversalResult(Vertex origin, Vertex target,
        SPTVertex parent, TraverseResult traverseResult, ShortestPathTree spt,
        TraverseOptions traverseOptions) {

      State state = traverseResult.state;
      StateData data = state.getData();
      if (traverseOptions.maxWalkDistance > 0
          && data.getWalkDistance() > traverseOptions.maxWalkDistance)
        return true;

      return false;
    }
  }
  
  
  private static class GenericAStarFactoryImpl implements GenericAStarFactory {

    @Override
    public GenericAStar createAStarInstance() {
      GenericAStar instance = new GenericAStar();
      instance.setSearchTerminationStrategy(new SearchTerminationStrategyImpl());
      instance.setShortestPathTreeFactory(TripSequenceShortestPathTree.FACTORY);
      return instance;
    }    
  }
}
