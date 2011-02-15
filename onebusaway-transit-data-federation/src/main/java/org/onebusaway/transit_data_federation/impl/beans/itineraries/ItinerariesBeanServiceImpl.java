package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.EdgeNarrativeBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.LocationBean;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.beans.FrequencyBeanLibrary;
import org.onebusaway.transit_data_federation.impl.otp.AbstractStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.ArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.BlockArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.BlockDepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.DepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.OTPConfiguration;
import org.onebusaway.transit_data_federation.impl.otp.RemainingWeightHeuristicImpl;
import org.onebusaway.transit_data_federation.impl.otp.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.WalkToStopVertex;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
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

  private PathService _pathService;

  private StreetVertexIndexService _streetVertexIndexService;

  private Graph _graph;

  private TripBeanService _tripBeanService;

  private NarrativeService _narrativeService;

  private StopBeanService _stopBeanService;

  private ShapePointService _shapePointService;

  @Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
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

  @Override
  public ItinerariesBean getItinerariesBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, ConstraintsBean constraints)
      throws ServiceException {

    String fromPlace = latFrom + "," + lonFrom;
    String toPlace = latTo + "," + lonTo;

    Date time = new Date(constraints.getTime());

    TraverseOptions options = createTraverseOptions();
    applyConstraintsToOptions(constraints, options);

    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace, time,
        options, 1);

    LocationBean from = getPointAsLocation(latFrom, lonFrom);
    LocationBean to = getPointAsLocation(latTo, lonTo);

    return getPathsAsItineraries(paths, from, to);
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

    options.remainingWeightHeuristic = new RemainingWeightHeuristicImpl();
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
      if (modes.contains("walk"))
        ms.setWalk(true);
      if (modes.contains("transit"))
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
      options.walkReluctance = constraints.getWaitReluctance();

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

    /**
     * Our custom traverse options extension
     */
    OTPConfiguration config = new OTPConfiguration();
    options.putExtension(OTPConfiguration.class, config);

    config.useRealtime = constraints.isUseRealTime();
  }

  private LocationBean getPointAsLocation(double lat, double lon) {
    LocationBean bean = new LocationBean();
    bean.setLocation(new CoordinatePoint(lat, lon));
    return bean;
  }

  private ItinerariesBean getPathsAsItineraries(List<GraphPath> paths,
      LocationBean from, LocationBean to) {

    ItinerariesBean bean = new ItinerariesBean();
    bean.setFrom(from);
    bean.setTo(to);

    List<ItineraryBean> beans = new ArrayList<ItineraryBean>();
    bean.setItineraries(beans);

    for (GraphPath path : paths) {
      ItineraryBean itinerary = getPathAsItinerary(path);
      beans.add(itinerary);
    }

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

    LegBean leg = new LegBean();
    legs.add(leg);

    leg.setStartTime(builder.getBestDepartureTime());
    leg.setEndTime(builder.getBestArrivalTime());

    double distance = getTransitLegBuilderAsDistance(builder);
    leg.setDistance(distance);

    leg.setMode("transit");

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

    applyFromStopDetailsForTransitLeg(builder, transitLeg);
    applyToStopDetailsForTransitLeg(builder, transitLeg);

    return new TransitLegBuilder();
  }

  private double getTransitLegBuilderAsDistance(TransitLegBuilder builder) {

    BlockInstance blockInstance = builder.getBlockInstance();
    BlockConfigurationEntry blockConfig = blockInstance.getBlock();

    BlockStopTimeEntry fromStop = null;
    BlockStopTimeEntry toStop = null;

    if (builder.getFromStop() != null)
      fromStop = builder.getFromStop().getBlockStopTime();

    if (builder.getToStop() != null)
      toStop = builder.getToStop().getBlockStopTime();

    if (fromStop == null && toStop == null)
      return blockConfig.getTotalBlockDistance();

    if (fromStop == null && toStop != null)
      return toStop.getDistanceAlongBlock();

    if (fromStop != null && toStop == null)
      return blockConfig.getTotalBlockDistance()
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
      TransitLegBean transitLeg) {

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
  }

  private void applyToStopDetailsForTransitLeg(TransitLegBuilder builder,
      TransitLegBean transitLeg) {

    ArrivalAndDepartureInstance toStopTimeInstance = builder.getToStop();

    if (toStopTimeInstance == null)
      return;

    StopEntry toStop = toStopTimeInstance.getStop();
    StopBean toStopBean = _stopBeanService.getStopForId(toStop.getId());
    transitLeg.setToStop(toStopBean);

    BlockStopTimeEntry blockStopTime = toStopTimeInstance.getBlockStopTime();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    transitLeg.setToStopSequence(stopTime.getSequence());
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

    }

    addPathToStreetLegIfApplicable(streetLeg, path, distance);

    LegBean leg = new LegBean();
    legs.add(leg);

    leg.setStartTime(startTime);
    leg.setEndTime(endTime);

    leg.setMode(getStreetModeAsString(mode));
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
        return "bicycle";
      case WALK:
        return "walk";
    }

    throw new IllegalStateException("unknown street mode: " + mode);
  }

  private ItineraryBean getWalkingItineraryBetweenStops(StopEntry from,
      StopEntry to, long time) {

    String fromPlace = WalkFromStopVertex.getVertexLabelForStop(from);
    String toPlace = WalkToStopVertex.getVertexLabelForStop(to);

    TraverseOptions options = createTraverseOptions();

    TraverseModeSet modes = new TraverseModeSet(TraverseMode.WALK);
    options.setModes(modes);

    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace,
        new Date(time), options, 1);

    if (paths.isEmpty())
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

}
