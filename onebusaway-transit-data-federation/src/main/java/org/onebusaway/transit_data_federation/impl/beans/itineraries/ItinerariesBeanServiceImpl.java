package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.EdgeNarrativeBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.LocationBean;
import org.onebusaway.transit_data.model.tripplanning.Modes;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.beans.ApplicationBeanLibrary;
import org.onebusaway.transit_data_federation.impl.beans.FrequencyBeanLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OTPConfiguration;
import org.onebusaway.transit_data_federation.impl.otp.RemainingWeightHeuristicImpl;
import org.onebusaway.transit_data_federation.impl.otp.SearchTerminationStrategyImpl;
import org.onebusaway.transit_data_federation.impl.otp.TripSequenceShortestPathTree;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractBlockVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.ArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.BlockArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.BlockDepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.DepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.WalkToStopVertex;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.otp.TransitShedPathService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.StreetVertex;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTEdge;
import org.opentripplanner.routing.spt.SPTVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

@Component
public class ItinerariesBeanServiceImpl implements ItinerariesBeanService {

  private static final String MODE_WALK = "walk";

  private static final String MODE_BICYCLE = "bicycle";

  private static final String MODE_TRANSIT = "transit";

  private PathService _pathService;

  private TransitShedPathService _transitShedPathService;

  private StreetVertexIndexService _streetVertexIndexService;

  private Graph _graph;

  private TripBeanService _tripBeanService;

  private NarrativeService _narrativeService;

  private StopBeanService _stopBeanService;

  private ShapePointService _shapePointService;

  private TransitGraphDao _transitGraphDao;

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  private BlockCalendarService _blockCalendarService;

  @Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
  }

  @Autowired
  public void setTransitShedPathService(
      TransitShedPathService transitShedPathService) {
    _transitShedPathService = transitShedPathService;
  }

  @Autowired
  public void setStreetVertexIndexService(
      StreetVertexIndexService streetVertexIndexService) {
    _streetVertexIndexService = streetVertexIndexService;
  }

  @Autowired
  public void setGraph(Graph graph) {
    _graph = graph;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    _arrivalAndDepartureService = arrivalAndDepartureService;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  /****
   * {@link ItinerariesBeanService} Interface
   ****/

  @Override
  public ItinerariesBean getItinerariesBetween(CoordinatePoint from,
      CoordinatePoint to, long targetTime, long currentTime,
      ConstraintsBean constraints) throws ServiceException {

    String fromPlace = getVertexLabelForPoint(from);
    String toPlace = getVertexLabelForPoint(to);

    Date t = new Date(targetTime);

    TraverseOptions options = createTraverseOptions();
    applyConstraintsToOptions(constraints, options);

    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace, t, options, 1);

    LocationBean fromBean = getPointAsLocation(from);
    LocationBean toBean = getPointAsLocation(to);

    ItinerariesBean itineraries = getPathsAsItineraries(paths, fromBean, toBean);

    ensureAdditionalItineraryIsIncluded(from, to, targetTime, currentTime,
        constraints, itineraries);

    return itineraries;
  }

  private String getVertexLabelForPoint(CoordinatePoint point) {
    return point.getLat() + "," + point.getLon();
  }

  @Override
  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo) {

    double x1 = Math.min(lonFrom, lonTo);
    double x2 = Math.max(lonFrom, lonTo);
    double y1 = Math.min(latFrom, latTo);
    double y2 = Math.max(latFrom, latTo);

    Envelope env = new Envelope(x1, x2, y1, y2);

    Collection<Vertex> vertices = _streetVertexIndexService.getVerticesForEnvelope(env);

    Map<Vertex, VertexBean> beansByVertex = new HashMap<Vertex, VertexBean>();

    for (Vertex vertex : vertices)
      getVertexAsBean(beansByVertex, vertex);

    for (Vertex vertex : vertices) {

      Collection<Edge> edges = null;

      if (vertex instanceof HasEdges) {
        HasEdges hasEdges = (HasEdges) vertex;
        edges = hasEdges.getOutgoing();
      } else {
        GraphVertex gv = _graph.getGraphVertex(vertex.getLabel());
        if (gv != null)
          edges = gv.getOutgoing();
      }

      if (edges != null) {

        VertexBean from = getVertexAsBean(beansByVertex, vertex);
        List<EdgeNarrativeBean> edgeNarratives = new ArrayList<EdgeNarrativeBean>();

        for (Edge edge : edges) {
          if (edge instanceof EdgeNarrative) {
            EdgeNarrative narrative = (EdgeNarrative) edge;
            EdgeNarrativeBean narrativeBean = new EdgeNarrativeBean();
            narrativeBean.setName(narrative.getName());

            Geometry geom = narrative.getGeometry();
            if (geom != null) {
              List<CoordinatePoint> path = new ArrayList<CoordinatePoint>();
              appendGeometryToPath(geom, path, true);
              EncodedPolylineBean polyline = PolylineEncoder.createEncodings(path);
              narrativeBean.setPath(polyline.getPoints());
            }

            narrativeBean.setFrom(from);
            narrativeBean.setTo(getVertexAsBean(beansByVertex,
                narrative.getToVertex()));

            Map<String, Object> tags = new HashMap<String, Object>();
            if (edge instanceof StreetEdge) {
              StreetEdge streetEdge = (StreetEdge) edge;
              StreetTraversalPermission permission = streetEdge.getPermission();
              if (permission != null)
                tags.put("access", permission.toString().toLowerCase());
            }

            if (!tags.isEmpty())
              narrativeBean.setTags(tags);

            edgeNarratives.add(narrativeBean);
          }
        }

        if (!edgeNarratives.isEmpty())
          from.setOutgoing(edgeNarratives);
      }
    }

    List<VertexBean> beans = new ArrayList<VertexBean>(beansByVertex.values());
    return new ListBean<VertexBean>(beans, false);
  }

  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints) {

    TraverseOptions options = createTraverseOptions();
    applyConstraintsToOptions(constraints.getConstraints(), options);

    Coordinate c = new Coordinate(location.getLon(), location.getLat());
    Vertex origin = _streetVertexIndexService.getClosestVertex(c, options);

    State originState = new State(time);
    BasicShortestPathTree tree = _transitShedPathService.getTransitShed(origin,
        originState, options);

    Map<StopEntry, Long> results = new HashMap<StopEntry, Long>();

    for (SPTVertex vertex : tree.getVertices()) {

      State state = vertex.state;
      OBAStateData data = (OBAStateData) state.getData();
      Vertex v = vertex.mirror;

      if (v instanceof AbstractStopVertex) {
        AbstractStopVertex stopVertex = (AbstractStopVertex) v;
        StopEntry stop = stopVertex.getStop();
        long initialWaitTime = data.getInitialWaitTime();
        long duration = Math.abs(state.getTime() - time) - initialWaitTime;
        if (!results.containsKey(stop) || results.get(stop) > duration)
          results.put(stop, duration);
      } else if (v instanceof AbstractBlockVertex) {
        AbstractBlockVertex blockVertex = (AbstractBlockVertex) v;
        ArrivalAndDepartureInstance instance = blockVertex.getInstance();
        StopEntry stop = instance.getStop();
        long initialWaitTime = data.getInitialWaitTime();
        long duration = Math.abs(state.getTime() - time) - initialWaitTime;
        if (!results.containsKey(stop) || results.get(stop) > duration)
          results.put(stop, duration);
      }
    }

    return getStopTravelTimesAsResultsBean(results, options.speed);
  }

  /****
   * Private Methods
   ****/

  /**
   * From 'Transit Capacity and Quality of Service Manual' - Part 3 - Exhibit
   * 3.9
   * 
   * http://onlinepubs.trb.org/Onlinepubs/tcrp/tcrp100/part%203.pdf
   * 
   * Table of passenger perceptions of time. Given that actual in-vehicle time
   * seems to occur in real-time (penalty ratio of 1.0), how do passengers
   * perceived walking, waiting for the first vehicle, and waiting for a
   * transfer. In addition, is there an additive penalty for making a transfer
   * of any kind.
   */
  private TraverseOptions createTraverseOptions() {

    TraverseOptions options = new TraverseOptions();

    options.walkReluctance = 2.2;
    options.waitAtBeginningFactor = 0.1;
    options.waitReluctance = 2.5;

    options.boardCost = 14 * 60;
    options.maxTransfers = 2;
    options.minTransferTime = 60;

    options.maxWalkDistance = 1000;
    options.maxTransfers = 2;

    /**
     * Ten seconds max
     */
    options.maxComputationTime = 10000;

    options.useServiceDays = false;

    options.stateFactory = OBAStateData.STATE_FACTORY;
    options.remainingWeightHeuristic = new RemainingWeightHeuristicImpl();
    options.searchTerminationStrategy = new SearchTerminationStrategyImpl();
    options.shortestPathTreeFactory = TripSequenceShortestPathTree.FACTORY;

    return options;
  }

  private void applyConstraintsToOptions(ConstraintsBean constraints,
      TraverseOptions options) {

    options.setArriveBy(constraints.isArriveBy());

    /**
     * Modes
     */
    Set<String> modes = constraints.getModes();
    if (modes != null) {
      TraverseModeSet ms = new TraverseModeSet();
      if (modes.contains(Modes.WALK))
        ms.setWalk(true);
      if (modes.contains(Modes.TRANSIT))
        ms.setTransit(true);
      options.setModes(ms);
    }

    /**
     * Walking
     */
    if (constraints.getWalkSpeed() != -1)
      options.speed = constraints.getWalkSpeed();
    if (constraints.getMaxWalkingDistance() != -1)
      options.maxWalkDistance = constraints.getMaxWalkingDistance();
    if (constraints.getWalkReluctance() != -1)
      options.walkReluctance = constraints.getWalkReluctance();

    /**
     * Waiting
     */
    if (constraints.getInitialWaitReluctance() != -1)
      options.waitAtBeginningFactor = constraints.getInitialWaitReluctance();
    if (constraints.getInitialWaitReluctance() != -1)
      options.waitReluctance = constraints.getWaitReluctance();

    /**
     * Transferring
     */
    if (constraints.getTransferCost() != -1)
      options.boardCost = constraints.getTransferCost();
    if (constraints.getMinTransferTime() != -1)
      options.minTransferTime = constraints.getMinTransferTime();
    if (constraints.getMaxTransfers() != -1)
      options.maxTransfers = constraints.getMaxTransfers();
    if (constraints.getMaxComputationTime() > 0
        && constraints.getMaxComputationTime() < 15000)
      options.maxComputationTime = constraints.getMaxComputationTime();

    options.numItineraries = constraints.getResultCount();

    /**
     * Our custom traverse options extension
     */
    OTPConfiguration config = new OTPConfiguration();
    options.putExtension(OTPConfiguration.class, config);

    config.useRealtime = constraints.isUseRealTime();

    if (constraints.getMaxTripDuration() != -1)
      config.maxTripDuration = constraints.getMaxTripDuration() * 1000;
  }

  private LocationBean getPointAsLocation(CoordinatePoint p) {
    LocationBean bean = new LocationBean();
    bean.setLocation(p);
    return bean;
  }

  private ItinerariesBean getPathsAsItineraries(List<GraphPath> paths,
      LocationBean from, LocationBean to) {

    ItinerariesBean bean = new ItinerariesBean();
    bean.setFrom(from);
    bean.setTo(to);

    List<ItineraryBean> beans = new ArrayList<ItineraryBean>();
    bean.setItineraries(beans);

    boolean computationTimeLimitReached = false;

    if (!CollectionsLibrary.isEmpty(paths)) {
      for (GraphPath path : paths) {
        computationTimeLimitReached |= path.isComputationTimeLimitReached();
        ItineraryBean itinerary = getPathAsItinerary(path);
        beans.add(itinerary);
      }
    }

    bean.setComputationTimeLimitReached(computationTimeLimitReached);

    return bean;
  }

  private ItineraryBean getPathAsItinerary(GraphPath path) {

    ItineraryBean itinerary = new ItineraryBean();

    SPTVertex startVertex = path.vertices.firstElement();
    State startState = startVertex.state;
    SPTVertex endVertex = path.vertices.lastElement();
    State endState = endVertex.state;

    itinerary.setStartTime(startState.getTime());
    itinerary.setEndTime(endState.getTime());

    List<LegBean> legs = new ArrayList<LegBean>();
    itinerary.setLegs(legs);

    List<SPTEdge> edges = path.edges;
    int currentIndex = 0;

    while (currentIndex < edges.size()) {

      SPTEdge sptEdge = edges.get(currentIndex);
      EdgeNarrative edgeNarrative = sptEdge.narrative;

      TraverseMode mode = edgeNarrative.getMode();

      if (mode.isTransit()) {
        currentIndex = extendTransitLeg(edges, currentIndex, legs);
      } else {
        currentIndex = extendStreetLeg(edges, currentIndex, mode, legs);
      }
    }

    return itinerary;
  }

  private int extendTransitLeg(List<SPTEdge> edges, int currentIndex,
      List<LegBean> legs) {

    TransitLegBuilder builder = new TransitLegBuilder();

    while (currentIndex < edges.size()) {

      SPTEdge sptEdge = edges.get(currentIndex);
      EdgeNarrative narrative = sptEdge.narrative;
      TraverseMode mode = narrative.getMode();

      if (!mode.isTransit())
        break;

      Vertex vFrom = narrative.getFromVertex();
      Vertex vTo = narrative.getToVertex();

      if (vFrom instanceof BlockDepartureVertex) {

        builder = extendTransitLegWithDepartureAndArrival(legs, builder,
            (BlockDepartureVertex) vFrom, (BlockArrivalVertex) vTo);

      } else if (vFrom instanceof BlockArrivalVertex) {

        BlockArrivalVertex arrival = (BlockArrivalVertex) vFrom;

        /**
         * Did we finish up a transit leg?
         */
        if (vTo instanceof ArrivalVertex) {

          /**
           * We've finished up our transit leg, so publish the leg
           */
          builder = getTransitLegBuilderAsLeg(builder, legs);

        }
        /**
         * Did we have a transfer to another stop?
         */
        else if (vTo instanceof DepartureVertex) {

          /**
           * We've finished up our transit leg either way, so publish the leg
           */
          builder = getTransitLegBuilderAsLeg(builder, legs);

          /**
           * We've possibly transfered to another stop, so we need to insert the
           * walk leg
           */
          ArrivalAndDepartureInstance fromStopTimeInstance = arrival.getInstance();
          StopEntry fromStop = fromStopTimeInstance.getStop();

          DepartureVertex toStopVertex = (DepartureVertex) vTo;
          StopEntry toStop = toStopVertex.getStop();

          addTransferLegIfNeeded(sptEdge, fromStop, toStop, legs);
        }
      } else if (vFrom instanceof ArrivalVertex) {
        if (vTo instanceof BlockDepartureVertex) {

          /**
           * This vertex combination occurs when we are doing an "arrive by"
           * trip and we need to do a transfer between two stops.
           */

          ArrivalVertex fromStopVertex = (ArrivalVertex) vFrom;
          StopEntry fromStop = fromStopVertex.getStop();

          BlockDepartureVertex toStopVertex = (BlockDepartureVertex) vTo;
          ArrivalAndDepartureInstance departureInstance = toStopVertex.getInstance();
          StopEntry toStop = departureInstance.getStop();

          addTransferLegIfNeeded(sptEdge, fromStop, toStop, legs);
        }
      }

      currentIndex++;
    }

    return currentIndex;
  }

  private void addTransferLegIfNeeded(SPTEdge sptEdge, StopEntry fromStop,
      StopEntry toStop, List<LegBean> legs) {
    if (!fromStop.equals(toStop)) {

      long timeFrom = sptEdge.fromv.state.getTime();
      long timeTo = sptEdge.tov.state.getTime();

      ItineraryBean walk = getWalkingItineraryBetweenStops(fromStop, toStop,
          timeFrom);

      if (walk != null) {
        scaleItinerary(walk, timeFrom, timeTo);
        legs.addAll(walk.getLegs());
      }
    }
  }

  private TransitLegBuilder extendTransitLegWithDepartureAndArrival(
      List<LegBean> legs, TransitLegBuilder builder,
      BlockDepartureVertex vFrom, BlockArrivalVertex vTo) {

    ArrivalAndDepartureInstance from = vFrom.getInstance();
    ArrivalAndDepartureInstance to = vTo.getInstance();

    BlockTripEntry tripFrom = from.getBlockTrip();
    BlockTripEntry tripTo = to.getBlockTrip();

    if (builder.getBlockInstance() == null) {
      builder.setScheduledDepartureTime(from.getScheduledDepartureTime());
      builder.setPredictedDepartureTime(from.getPredictedDepartureTime());
      builder.setBlockInstance(from.getBlockInstance());
      builder.setBlockTrip(tripFrom);
      builder.setFromStop(from);
    }

    if (!tripFrom.equals(tripTo)) {

      /**
       * We switch trips during the course of the block, so we clean up the
       * current leg and introduce a new one
       */

      /**
       * We just split the difference for now
       */
      long scheduledTransitionTime = (from.getScheduledDepartureTime() + to.getScheduledArrivalTime()) / 2;
      long predictedTransitionTime = 0;

      if (from.isPredictedDepartureTimeSet() && to.isPredictedArrivalTimeSet())
        predictedTransitionTime = (from.getPredictedDepartureTime() + to.getPredictedArrivalTime()) / 2;

      builder.setScheduledArrivalTime(scheduledTransitionTime);
      builder.setPredictedArrivalTime(predictedTransitionTime);
      builder.setToStop(null);

      getTransitLegBuilderAsLeg(builder, legs);

      builder = new TransitLegBuilder();
      builder.setScheduledDepartureTime(scheduledTransitionTime);
      builder.setPredictedDepartureTime(predictedTransitionTime);
      builder.setBlockInstance(to.getBlockInstance());
      builder.setBlockTrip(tripTo);

    }

    builder.setToStop(to);
    builder.setScheduledArrivalTime(to.getScheduledArrivalTime());
    builder.setPredictedArrivalTime(to.getPredictedArrivalTime());
    return builder;
  }

  private TransitLegBuilder getTransitLegBuilderAsLeg(
      TransitLegBuilder builder, List<LegBean> legs) {

    BlockInstance blockInstance = builder.getBlockInstance();
    BlockTripEntry blockTrip = builder.getBlockTrip();

    if (blockInstance == null || blockTrip == null)
      return new TransitLegBuilder();

    LegBean leg = createTransitLegFromBuilder(builder);
    legs.add(leg);

    return new TransitLegBuilder();
  }

  private LegBean createTransitLegFromBuilder(TransitLegBuilder builder) {

    BlockInstance blockInstance = builder.getBlockInstance();
    BlockTripEntry blockTrip = builder.getBlockTrip();

    LegBean leg = new LegBean();

    leg.setStartTime(builder.getBestDepartureTime());
    leg.setEndTime(builder.getBestArrivalTime());

    double distance = getTransitLegBuilderAsDistance(builder);
    leg.setDistance(distance);

    leg.setMode(MODE_TRANSIT);

    TripEntry trip = blockTrip.getTrip();

    TransitLegBean transitLeg = new TransitLegBean();
    leg.setTransitLeg(transitLeg);

    transitLeg.setServiceDate(blockInstance.getServiceDate());

    if (blockInstance.getFrequency() != null) {
      FrequencyBean frequency = FrequencyBeanLibrary.getBeanForFrequency(
          blockInstance.getServiceDate(), blockInstance.getFrequency());
      transitLeg.setFrequency(frequency);
    }

    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    transitLeg.setTrip(tripBean);

    transitLeg.setScheduledDepartureTime(builder.getScheduledDepartureTime());
    transitLeg.setScheduledArrivalTime(builder.getScheduledArrivalTime());

    transitLeg.setPredictedDepartureTime(builder.getPredictedDepartureTime());
    transitLeg.setPredictedArrivalTime(builder.getPredictedArrivalTime());

    String path = getTransitLegBuilderAsPath(builder);
    transitLeg.setPath(path);

    applyFromStopDetailsForTransitLeg(builder, transitLeg, leg);
    applyToStopDetailsForTransitLeg(builder, transitLeg, leg);

    if (leg.getFrom() == null || leg.getTo() == null && path != null) {
      List<CoordinatePoint> points = PolylineEncoder.decode(path);
      if (leg.getFrom() == null)
        leg.setFrom(points.get(0));
      if (leg.getTo() == null)
        leg.setTo(points.get(points.size() - 1));
    }

    return leg;
  }

  private double getTransitLegBuilderAsDistance(TransitLegBuilder builder) {

    BlockTripEntry trip = builder.getBlockTrip();

    BlockStopTimeEntry fromStop = null;
    BlockStopTimeEntry toStop = null;

    if (builder.getFromStop() != null)
      fromStop = builder.getFromStop().getBlockStopTime();

    if (builder.getToStop() != null)
      toStop = builder.getToStop().getBlockStopTime();

    if (fromStop == null && toStop == null)
      return trip.getTrip().getTotalTripDistance();

    if (fromStop == null && toStop != null)
      return toStop.getDistanceAlongBlock() - trip.getDistanceAlongBlock();

    if (fromStop != null && toStop == null)
      return trip.getDistanceAlongBlock()
          + trip.getTrip().getTotalTripDistance()
          - fromStop.getDistanceAlongBlock();

    return toStop.getDistanceAlongBlock() - fromStop.getDistanceAlongBlock();
  }

  private String getTransitLegBuilderAsPath(TransitLegBuilder builder) {

    BlockTripEntry blockTrip = builder.getBlockTrip();
    TripEntry trip = blockTrip.getTrip();

    AgencyAndId shapeId = trip.getShapeId();

    if (shapeId == null)
      return null;

    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

    BlockStopTimeEntry fromStop = null;
    BlockStopTimeEntry toStop = null;

    if (builder.getFromStop() != null)
      fromStop = builder.getFromStop().getBlockStopTime();

    if (builder.getToStop() != null)
      toStop = builder.getToStop().getBlockStopTime();

    if (fromStop == null && toStop == null) {
      return ShapeSupport.getFullPath(shapePoints);
    }

    if (fromStop == null && toStop != null) {
      return ShapeSupport.getPartialPathToStop(shapePoints,
          toStop.getStopTime());
    }

    if (fromStop != null && toStop == null) {
      return ShapeSupport.getPartialPathFromStop(shapePoints,
          fromStop.getStopTime());
    }

    return ShapeSupport.getPartialPathBetweenStops(shapePoints,
        fromStop.getStopTime(), toStop.getStopTime());
  }

  private void applyFromStopDetailsForTransitLeg(TransitLegBuilder builder,
      TransitLegBean transitLeg, LegBean leg) {

    ArrivalAndDepartureInstance fromStopTimeInstance = builder.getFromStop();

    if (fromStopTimeInstance == null)
      return;

    BlockStopTimeEntry bstFrom = fromStopTimeInstance.getBlockStopTime();

    StopTimeEntry fromStopTime = bstFrom.getStopTime();
    StopTimeNarrative stopTimeNarrative = _narrativeService.getStopTimeForEntry(fromStopTime);
    transitLeg.setRouteShortName(stopTimeNarrative.getRouteShortName());
    transitLeg.setTripHeadsign(stopTimeNarrative.getStopHeadsign());

    StopEntry fromStop = fromStopTimeInstance.getStop();
    StopBean fromStopBean = _stopBeanService.getStopForId(fromStop.getId());
    transitLeg.setFromStop(fromStopBean);

    transitLeg.setFromStopSequence(fromStopTime.getSequence());

    leg.setFrom(fromStop.getStopLocation());
  }

  private void applyToStopDetailsForTransitLeg(TransitLegBuilder builder,
      TransitLegBean transitLeg, LegBean leg) {

    ArrivalAndDepartureInstance toStopTimeInstance = builder.getToStop();

    if (toStopTimeInstance == null)
      return;

    StopEntry toStop = toStopTimeInstance.getStop();
    StopBean toStopBean = _stopBeanService.getStopForId(toStop.getId());
    transitLeg.setToStop(toStopBean);

    BlockStopTimeEntry blockStopTime = toStopTimeInstance.getBlockStopTime();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    transitLeg.setToStopSequence(stopTime.getSequence());

    leg.setTo(toStop.getStopLocation());
  }

  private int extendStreetLeg(List<SPTEdge> edges, int currentIndex,
      TraverseMode mode, List<LegBean> legs) {

    List<SPTEdge> streetEdges = new ArrayList<SPTEdge>();

    while (currentIndex < edges.size()) {

      SPTEdge sptEdge = edges.get(currentIndex);
      EdgeNarrative narrative = sptEdge.narrative;
      TraverseMode edgeMode = narrative.getMode();

      if (mode != edgeMode)
        break;

      streetEdges.add(sptEdge);

      currentIndex++;
    }

    if (!streetEdges.isEmpty()) {
      getStreetLegBuilderAsLeg(streetEdges, mode, legs);
    }

    return currentIndex;
  }

  private void getStreetLegBuilderAsLeg(List<SPTEdge> streetEdges,
      TraverseMode mode, List<LegBean> legs) {

    List<StreetLegBean> streetLegs = new ArrayList<StreetLegBean>();

    StreetLegBean streetLeg = null;
    List<CoordinatePoint> path = new ArrayList<CoordinatePoint>();

    double distance = 0.0;
    double totalDistance = 0.0;

    long startTime = 0;
    long endTime = 0;

    CoordinatePoint from = null;
    CoordinatePoint to = null;

    for (SPTEdge sptEdge : streetEdges) {

      EdgeNarrative edgeResult = sptEdge.narrative;

      Geometry geom = edgeResult.getGeometry();
      if (geom == null) {
        continue;
      }

      String streetName = edgeResult.getName();

      if (streetLeg == null
          || !ObjectUtils.equals(streetLeg.getStreetName(), streetName)) {

        addPathToStreetLegIfApplicable(streetLeg, path, distance);

        streetLeg = createStreetLeg(sptEdge);
        streetLegs.add(streetLeg);
        path = new ArrayList<CoordinatePoint>();

        appendGeometryToPath(geom, path, true);
        distance = edgeResult.getDistance();

      } else {

        appendGeometryToPath(geom, path, false);
        distance += edgeResult.getDistance();
      }

      totalDistance += edgeResult.getDistance();

      if (startTime == 0)
        startTime = sptEdge.fromv.state.getTime();
      endTime = sptEdge.tov.state.getTime();

      if (!path.isEmpty()) {
        if (from == null)
          from = path.get(0);
        to = path.get(path.size() - 1);
      }

    }

    addPathToStreetLegIfApplicable(streetLeg, path, distance);

    LegBean leg = new LegBean();
    legs.add(leg);

    leg.setStartTime(startTime);
    leg.setEndTime(endTime);

    leg.setMode(getStreetModeAsString(mode));

    leg.setFrom(from);
    leg.setTo(to);
    leg.setDistance(totalDistance);

    leg.setStreetLegs(streetLegs);
  }

  private void addPathToStreetLegIfApplicable(StreetLegBean streetLeg,
      List<CoordinatePoint> path, double distance) {
    if (streetLeg != null) {
      EncodedPolylineBean polyline = PolylineEncoder.createEncodings(path);
      streetLeg.setPath(polyline.getPoints());
      streetLeg.setDistance(distance);
    }
  }

  private void appendGeometryToPath(Geometry geom, List<CoordinatePoint> path,
      boolean includeFirstPoint) {

    if (geom instanceof LineString) {
      LineString ls = (LineString) geom;

      for (int i = 0; i < ls.getNumPoints(); i++) {
        if (i == 0 && !includeFirstPoint)
          continue;

        Coordinate c = ls.getCoordinateN(i);
        CoordinatePoint p = new CoordinatePoint(c.y, c.x);
        path.add(p);
      }
    } else {
      throw new IllegalStateException("unknown geometry: " + geom);
    }
  }

  private StreetLegBean createStreetLeg(SPTEdge edge) {
    StreetLegBean bean = new StreetLegBean();
    bean.setStreetName(edge.getName());
    return bean;
  }

  private String getStreetModeAsString(TraverseMode mode) {

    switch (mode) {
      case BICYCLE:
        return MODE_BICYCLE;
      case WALK:
        return MODE_WALK;
    }

    throw new IllegalStateException("unknown street mode: " + mode);
  }

  private ItineraryBean getWalkingItineraryBetweenStops(StopEntry from,
      StopEntry to, long time) {

    String fromPlace = WalkFromStopVertex.getVertexLabelForStop(from);
    String toPlace = WalkToStopVertex.getVertexLabelForStop(to);

    return getWalkingItineraryBetweenVertexLabels(fromPlace, toPlace, time);
  }

  private ItineraryBean getWalkingItineraryBetweenVertexLabels(
      String fromPlace, String toPlace, long time) {
    TraverseOptions options = createTraverseOptions();

    TraverseModeSet modes = new TraverseModeSet(TraverseMode.WALK);
    options.setModes(modes);

    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace,
        new Date(time), options, 1);

    if (CollectionsLibrary.isEmpty(paths))
      return null;

    return getPathAsItinerary(paths.get(0));
  }

  private void scaleItinerary(ItineraryBean bean, long timeFrom, long timeTo) {

    long tStart = bean.getStartTime();
    long tEnd = bean.getEndTime();

    double ratio = (timeTo - timeFrom) / (tEnd - tStart);

    bean.setStartTime(scaleTime(tStart, timeFrom, ratio, tStart));
    bean.setEndTime(scaleTime(tStart, timeFrom, ratio, tEnd));

    for (LegBean leg : bean.getLegs()) {
      leg.setStartTime(scaleTime(tStart, timeFrom, ratio, leg.getStartTime()));
      leg.setEndTime(scaleTime(tStart, timeFrom, ratio, leg.getEndTime()));
    }
  }

  private long scaleTime(long tStartOrig, long tStartNew, double ratio, long t) {
    return (long) ((t - tStartOrig) * ratio + tStartNew);
  }

  private void ensureAdditionalItineraryIsIncluded(CoordinatePoint from,
      CoordinatePoint to, long targetTime, long currentTime,
      ConstraintsBean constraints, ItinerariesBean itineraries) {

    ItineraryBean toInclude = constraints.getIncludeItinerary();

    if (toInclude == null)
      return;

    if (!isItinerarySufficientlySpecified(toInclude))
      return;

    for (ItineraryBean itinerary : itineraries.getItineraries()) {
      if (isItineraryMatch(itinerary, toInclude))
        return;
    }

    updateItinerary(toInclude, from, to, targetTime, currentTime, constraints);

    itineraries.getItineraries().add(toInclude);
  }

  private boolean isItinerarySufficientlySpecified(ItineraryBean itinerary) {
    for (LegBean leg : itinerary.getLegs()) {
      if (!isLegSufficientlySpecified(leg))
        return false;
    }
    return true;
  }

  private boolean isLegSufficientlySpecified(LegBean leg) {

    if (leg == null)
      return false;

    if (leg.getFrom() == null || leg.getTo() == null)
      return false;

    String mode = leg.getMode();
    if (MODE_TRANSIT.equals(mode)) {
      if (!isTransitLegSufficientlySpecified(leg.getTransitLeg()))
        return false;
    } else if (MODE_WALK.equals(mode) || MODE_BICYCLE.equals(mode)) {

    } else {
      return false;
    }

    return true;
  }

  private boolean isTransitLegSufficientlySpecified(TransitLegBean leg) {
    if (leg == null)
      return false;
    if (leg.getTrip() == null || leg.getTrip().getId() == null)
      return false;
    if (leg.getServiceDate() <= 0)
      return false;
    return true;
  }

  private boolean isItineraryMatch(ItineraryBean a, ItineraryBean b) {
    List<String> instancesA = getTransitInstancesForItinerary(a);
    List<String> instancesB = getTransitInstancesForItinerary(b);
    return instancesA.equals(instancesB);
  }

  private List<String> getTransitInstancesForItinerary(ItineraryBean itinerary) {
    List<String> instances = new ArrayList<String>();
    for (LegBean leg : itinerary.getLegs()) {
      TransitLegBean transitLeg = leg.getTransitLeg();
      if (transitLeg != null) {
        String instance = transitLeg.getTrip().getId() + " "
            + transitLeg.getServiceDate();
        instances.add(instance);
      }
    }
    return instances;
  }

  private void updateItinerary(ItineraryBean itinerary, CoordinatePoint from,
      CoordinatePoint to, long time, long currentTime,
      ConstraintsBean constraints) {

    List<LegBean> legs = itinerary.getLegs();

    int firstTransitLegIndex = -1;

    /**
     * Update the legs
     */
    for (int i = 0; i < legs.size(); i++) {

      LegBean leg = legs.get(i);
      TransitLegBean transitLeg = leg.getTransitLeg();

      if (transitLeg != null) {
        LegBean updatedLeg = updateTransitLeg(transitLeg, time);
        legs.set(i, updatedLeg);

        if (firstTransitLegIndex == -1)
          firstTransitLegIndex = i;

      } else if (isStreetLeg(leg)) {

        String fromPlace = getVertexLabelForPoint(leg.getFrom());
        String toPlace = getVertexLabelForPoint(leg.getTo());
        ItineraryBean walkItinerary = getWalkingItineraryBetweenVertexLabels(
            fromPlace, toPlace, currentTime);
        legs.set(i, walkItinerary.getLegs().get(0));
      }
    }

    /**
     * Update the times
     */
    if (firstTransitLegIndex == -1) {

    } else {

      long nextTime = legs.get(firstTransitLegIndex).getStartTime();
      for (int i = firstTransitLegIndex - 1; i >= 0; i--) {

        LegBean leg = legs.get(i);
        if (isStreetLeg(leg)) {
          long duration = leg.getEndTime() - leg.getStartTime();
          leg.setEndTime(nextTime);
          leg.setStartTime(nextTime - duration);
        }

        nextTime = leg.getStartTime();
      }

      long prevTime = legs.get(firstTransitLegIndex).getEndTime();
      for (int i = firstTransitLegIndex + 1; i < legs.size(); i++) {

        LegBean leg = legs.get(i);
        if (isStreetLeg(leg)) {
          long duration = leg.getEndTime() - leg.getStartTime();
          leg.setStartTime(prevTime);
          leg.setEndTime(prevTime + duration);
        }

        prevTime = leg.getEndTime();
      }

      itinerary.setStartTime(nextTime);
      itinerary.setEndTime(prevTime);
    }
  }

  private boolean isStreetLeg(LegBean leg) {
    return MODE_WALK.equals(leg.getMode())
        || MODE_BICYCLE.equals(leg.getMode());
  }

  private LegBean updateTransitLeg(TransitLegBean transitLeg, long time) {

    TransitLegBuilder b = new TransitLegBuilder();

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(transitLeg.getTrip().getId());
    TripEntry trip = _transitGraphDao.getTripEntryForId(tripId);

    if (trip == null)
      throw new NoSuchTripServiceException(transitLeg.getTrip().getId());

    long serviceDate = transitLeg.getServiceDate();

    AgencyAndId vehicleId = null;
    if (transitLeg.getVehicleId() != null)
      vehicleId = AgencyAndIdLibrary.convertFromString(transitLeg.getVehicleId());

    if (transitLeg.getFromStop() != null
        && transitLeg.getFromStop().getId() != null) {

      AgencyAndId fromStopId = AgencyAndIdLibrary.convertFromString(transitLeg.getFromStop().getId());
      StopEntry fromStop = _transitGraphDao.getStopEntryForId(fromStopId);

      if (fromStop == null)
        throw new NoSuchStopServiceException(transitLeg.getFromStop().getId());
      int fromStopSequence = transitLeg.getFromStopSequence();

      ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(
          fromStop, fromStopSequence, trip, serviceDate, vehicleId, time);

      b.setFromStop(instance);
      b.setBlockInstance(instance.getBlockInstance());
      b.setBlockTrip(instance.getBlockTrip());
      b.setScheduledDepartureTime(instance.getScheduledDepartureTime());
      b.setPredictedDepartureTime(instance.getPredictedDepartureTime());
    }

    if (transitLeg.getToStop() != null
        && transitLeg.getToStop().getId() != null) {

      AgencyAndId toStopId = AgencyAndIdLibrary.convertFromString(transitLeg.getToStop().getId());
      StopEntry toStop = _transitGraphDao.getStopEntryForId(toStopId);

      if (toStop == null)
        throw new NoSuchStopServiceException(transitLeg.getToStop().getId());
      int toStopSequence = transitLeg.getToStopSequence();

      ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(
          toStop, toStopSequence, trip, serviceDate, vehicleId, time);

      b.setToStop(instance);
      b.setBlockInstance(instance.getBlockInstance());
      b.setBlockTrip(instance.getBlockTrip());
      b.setScheduledArrivalTime(instance.getScheduledArrivalTime());
      b.setPredictedArrivalTime(instance.getPredictedArrivalTime());
    }

    if (b.getBlockInstance() == null) {

      BlockEntry block = trip.getBlock();

      BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
          block.getId(), serviceDate);
      b.setBlockInstance(blockInstance);

      BlockTripEntry blockTrip = _blockCalendarService.getTargetBlockTrip(
          blockInstance, trip);
      b.setBlockTrip(blockTrip);
    }

    return createTransitLegFromBuilder(b);
  }

  private VertexBean getVertexAsBean(Map<Vertex, VertexBean> beansByVertex,
      Vertex vertex) {

    VertexBean bean = beansByVertex.get(vertex);

    if (bean == null) {

      bean = new VertexBean();
      bean.setId(vertex.getLabel());
      bean.setLocation(new CoordinatePoint(vertex.getY(), vertex.getX()));

      Map<String, Object> tags = new HashMap<String, Object>();

      tags.put("class", vertex.getClass().getName());

      if (vertex instanceof StreetVertex) {
        StreetVertex sv = (StreetVertex) vertex;
        StreetTraversalPermission perms = sv.getPermission();
        if (perms != null)
          tags.put("access", perms.toString().toLowerCase());
      } else if (vertex instanceof AbstractStopVertex) {
        AbstractStopVertex stopVertex = (AbstractStopVertex) vertex;
        StopEntry stop = stopVertex.getStop();
        StopBean stopBean = _stopBeanService.getStopForId(stop.getId());
        tags.put("stop", stopBean);
      }

      bean.setTags(tags);

      beansByVertex.put(vertex, bean);

    }

    return bean;
  }

  private MinTravelTimeToStopsBean getStopTravelTimesAsResultsBean(
      Map<StopEntry, Long> results, double walkingVelocity) {

    int n = results.size();

    String[] stopIds = new String[n];
    double[] lats = new double[n];
    double[] lons = new double[n];
    long[] times = new long[n];

    int index = 0;
    String agencyId = null;

    for (Map.Entry<StopEntry, Long> entry : results.entrySet()) {
      StopEntry stop = entry.getKey();
      agencyId = stop.getId().getAgencyId();
      Long time = entry.getValue();
      stopIds[index] = ApplicationBeanLibrary.getId(stop.getId());
      lats[index] = stop.getStopLat();
      lons[index] = stop.getStopLon();
      times[index] = time;
      index++;
    }
    return new MinTravelTimeToStopsBean(agencyId, stopIds, lats, lons, times,
        walkingVelocity);
  }
}
