package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
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
import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.VertexBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.beans.ApplicationBeanLibrary;
import org.onebusaway.transit_data_federation.impl.beans.FrequencyBeanLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAState;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractBlockVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.ArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.BlockArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.BlockDepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.DepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPBlockArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPBlockDepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPTransferEdge;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureQuery;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.otp.TransitShedPathService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.StreetVertex;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.opentripplanner.routing.spt.GraphPath;
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
  
  private ItinerariesService _itinerariesService;

  private TransitShedPathService _transitShedPathService;

  private StreetVertexIndexService _streetVertexIndexService;

  private GraphService _graphService;

  private OTPConfigurationService _otpConfigurationService;

  private TripBeanService _tripBeanService;

  private NarrativeService _narrativeService;

  private StopBeanService _stopBeanService;

  private ShapePointService _shapePointService;

  private TransitGraphDao _transitGraphDao;

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  private BlockCalendarService _blockCalendarService;
  
  private boolean _enabled = true;

  @Autowired
  public void setItinerariesService(ItinerariesService itinerariesService) {
    _itinerariesService = itinerariesService;
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
  public void setGraphService(GraphService graphService) {
    _graphService = graphService;
  }

  @Autowired
  public void setOtpConfigurationService(
      OTPConfigurationService otpConfigurationService) {
    _otpConfigurationService = otpConfigurationService;
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
  
  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  /****
   * {@link ItinerariesBeanService} Interface
   ****/

  @Override
  public ItinerariesBean getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ConstraintsBean constraints)
      throws ServiceException {
    
    if( ! _enabled )
      throw new ServiceException("service disabled");

    OBATraverseOptions options = createTraverseOptions();
    applyConstraintsToOptions(constraints, options);

    List<GraphPath> paths = _itinerariesService.getItinerariesBetween(from, to,
        targetTime, options);

    LocationBean fromBean = getPointAsLocation(from);
    LocationBean toBean = getPointAsLocation(to);

    ItinerariesBean itineraries = getPathsAsItineraries(paths, fromBean,
        toBean, options);

    ensureSelectedItineraryIsIncluded(from, to, targetTime, itineraries,
        constraints.getSelectedItinerary(), options);

    if (options.isArriveBy())
      Collections.sort(itineraries.getItineraries(), new SortByArrival());
    else
      Collections.sort(itineraries.getItineraries(), new SortByDeparture());

    return itineraries;
  }

  @Override
  public ListBean<VertexBean> getStreetGraphForRegion(double latFrom,
      double lonFrom, double latTo, double lonTo) {
    
    if( ! _enabled)
      throw new ServiceException("service disabled");

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
        Graph graph = _graphService.getGraph();
        GraphVertex gv = graph.getGraphVertex(vertex.getLabel());
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

  @Override
  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints) {
    
    if( ! _enabled)
      throw new ServiceException("service disabled");

    OBATraverseOptions options = createTraverseOptions();
    applyConstraintsToOptions(constraints.getConstraints(), options);

    Coordinate c = new Coordinate(location.getLon(), location.getLat());
    Vertex origin = _streetVertexIndexService.getClosestVertex(c, options);

    State originState = new OBAState(time, origin, options);
    BasicShortestPathTree tree = _transitShedPathService.getTransitShed(origin,
        originState, options);

    Map<StopEntry, Long> results = new HashMap<StopEntry, Long>();

    for (State state : tree.getAllStates()) {

      OBAState obaState = (OBAState) state;
      Vertex v = state.getVertex();

      if (v instanceof AbstractStopVertex) {
        AbstractStopVertex stopVertex = (AbstractStopVertex) v;
        StopEntry stop = stopVertex.getStop();
        long initialWaitTime = obaState.getInitialWaitTime();
        long duration = Math.abs(state.getTime() - time) - initialWaitTime;
        if (!results.containsKey(stop) || results.get(stop) > duration)
          results.put(stop, duration);
      } else if (v instanceof AbstractBlockVertex) {
        AbstractBlockVertex blockVertex = (AbstractBlockVertex) v;
        ArrivalAndDepartureInstance instance = blockVertex.getInstance();
        StopEntry stop = instance.getStop();
        long initialWaitTime = obaState.getInitialWaitTime();
        long duration = Math.abs(state.getTime() - time) - initialWaitTime;
        if (!results.containsKey(stop) || results.get(stop) > duration)
          results.put(stop, duration);
      }
    }

    return getStopTravelTimesAsResultsBean(results, options.speed);
  }

  @Override
  public List<TimedPlaceBean> getLocalPaths(ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {
    
    if( ! _enabled)
      throw new ServiceException("service disabled");

    long maxTripLength = constraints.getMaxTripDuration() * 1000;

    List<TimedPlaceBean> beans = new ArrayList<TimedPlaceBean>();

    double walkingVelocity = travelTimes.getWalkingVelocity() / 1000;

    ConstraintsBean walkConstraints = new ConstraintsBean(constraints);
    walkConstraints.setModes(CollectionsLibrary.set(Modes.WALK));

    for (LocalSearchResult result : localResults) {

      double placeLat = result.getLat();
      double placeLon = result.getLon();

      List<TripToStop> closestStops = new ArrayList<TripToStop>();

      for (int index = 0; index < travelTimes.getSize(); index++) {

        String stopIdAsString = travelTimes.getStopId(index);
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopIdAsString);

        long currentTripDuration = travelTimes.getTravelTime(index);
        double stopLat = travelTimes.getStopLat(index);
        double stopLon = travelTimes.getStopLon(index);
        double d = SphericalGeometryLibrary.distance(stopLat, stopLon,
            placeLat, placeLon);
        double t = currentTripDuration + d / walkingVelocity;
        if (d <= constraints.getMaxWalkingDistance() && t < maxTripLength) {
          closestStops.add(new TripToStop(stopId, currentTripDuration, t, index));
        }
      }

      if (closestStops.isEmpty())
        continue;

      Collections.sort(closestStops);

      double minTime = 0;
      TripToStop minStop = null;

      TransitLocationBean place = new TransitLocationBean();
      place.setLat(result.getLat());
      place.setLon(result.getLon());

      for (TripToStop o : closestStops) {

        long currentTripDuration = o.getTransitTimeToStop();
        double minTimeToPlace = o.getMinTansitTimeToPlace();

        // Short circuit if there is no way any of the remaining trips is going
        // to be better than our current winner
        if (minStop != null && minTimeToPlace > minTime)
          break;

        int remainingTime = (int) ((maxTripLength - currentTripDuration) / 1000);
        walkConstraints.setMaxTripDuration(remainingTime);

        int index = o.getIndex();
        TransitLocationBean stopLocation = new TransitLocationBean();
        stopLocation.setLat(travelTimes.getStopLat(index));
        stopLocation.setLon(travelTimes.getStopLon(index));

        ItinerariesBean itineraries = getItinerariesBetween(stopLocation,
            place, System.currentTimeMillis(), walkConstraints);

        for (ItineraryBean plan : itineraries.getItineraries()) {
          double t = currentTripDuration
              + (plan.getEndTime() - plan.getStartTime());
          if (minStop == null || t < minTime) {
            minTime = t;
            minStop = o;
          }
        }
      }

      if (minStop != null && minTime <= maxTripLength) {
        TimedPlaceBean bean = new TimedPlaceBean();
        bean.setPlaceId(result.getId());
        bean.setStopId(ApplicationBeanLibrary.getId(minStop.getStopId()));
        bean.setTime((int) (minTime / 1000));
        beans.add(bean);
      }
    }

    return beans;
  }

  /****
   * Private Methods
   ****/

  private OBATraverseOptions createTraverseOptions() {
    OBATraverseOptions options = _otpConfigurationService.createTraverseOptions();
    return options;
  }

  private void applyConstraintsToOptions(ConstraintsBean constraints,
      OBATraverseOptions options) {

    _otpConfigurationService.applyConstraintsToTraverseOptions(constraints,
        options);
  }

  private LocationBean getPointAsLocation(TransitLocationBean p) {
    LocationBean bean = new LocationBean();
    bean.setLocation(new CoordinatePoint(p.getLat(), p.getLon()));
    return bean;
  }

  private ItinerariesBean getPathsAsItineraries(List<GraphPath> paths,
      LocationBean from, LocationBean to, OBATraverseOptions options) {

    ItinerariesBean bean = new ItinerariesBean();
    bean.setFrom(from);
    bean.setTo(to);

    List<ItineraryBean> beans = new ArrayList<ItineraryBean>();
    bean.setItineraries(beans);

    boolean computationTimeLimitReached = false;

    if (!CollectionsLibrary.isEmpty(paths)) {
      for (GraphPath path : paths) {

        ItineraryBean itinerary = getPathAsItinerary(path, options);
        beans.add(itinerary);
      }
    }

    bean.setComputationTimeLimitReached(computationTimeLimitReached);

    return bean;
  }

  private ItineraryBean getPathAsItinerary(GraphPath path,
      OBATraverseOptions options) {

    ItineraryBean itinerary = new ItineraryBean();

    State startState = path.states.getFirst();
    State endState = path.states.getLast();

    itinerary.setStartTime(startState.getTime());
    itinerary.setEndTime(endState.getTime());

    List<LegBean> legs = new ArrayList<LegBean>();
    itinerary.setLegs(legs);

    /**
     * We set the current state index to 1, skipping the first state, since it
     * has no back edge
     */
    List<State> states = new ArrayList<State>(path.states);
    int currentIndex = 1;

    while (currentIndex < states.size()) {

      State state = states.get(currentIndex);
      EdgeNarrative edgeNarrative = state.getBackEdgeNarrative();

      TraverseMode mode = edgeNarrative.getMode();

      if (mode.isTransit()) {
        currentIndex = extendTransitLeg(states, currentIndex, options, legs);
      } else {
        currentIndex = extendStreetLeg(states, currentIndex, mode, legs);
      }
    }

    return itinerary;
  }

  private int extendTransitLeg(List<State> states, int currentIndex,
      OBATraverseOptions options, List<LegBean> legs) {

    TransitLegBuilder builder = new TransitLegBuilder();

    while (currentIndex < states.size()) {

      State state = states.get(currentIndex);
      Edge edge = state.getBackEdge();
      EdgeNarrative narrative = state.getBackEdgeNarrative();
      TraverseMode mode = narrative.getMode();

      if (!mode.isTransit())
        break;

      Vertex vFrom = narrative.getFromVertex();
      Vertex vTo = narrative.getToVertex();

      if (vFrom instanceof BlockDepartureVertex) {

        builder = extendTransitLegWithDepartureAndArrival(legs, builder,
            (BlockDepartureVertex) vFrom, (BlockArrivalVertex) vTo);

      } else if (vFrom instanceof TPBlockDepartureVertex) {

        builder = extendTransitLegWithTPDepartureAndArrival(legs, builder,
            (TPBlockDepartureVertex) vFrom, (TPBlockArrivalVertex) vTo);

      } else if (vFrom instanceof BlockArrivalVertex) {

        builder = extendTransitLegWithArrival(legs, builder,
            (BlockArrivalVertex) vFrom, vTo, state, options);

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

          addTransferLegIfNeeded(state, fromStop, toStop, options, legs);
        }
      } else if (edge instanceof TPTransferEdge) {

        TPTransferEdge transferEdge = (TPTransferEdge) edge;
        StopEntry fromStop = transferEdge.getFromStop();
        StopEntry toStop = transferEdge.getToStop();

        addTransferLegIfNeeded(state, fromStop, toStop, options, legs);
      }

      currentIndex++;
    }

    return currentIndex;
  }

  private void addTransferLegIfNeeded(State state, StopEntry fromStop,
      StopEntry toStop, OBATraverseOptions options, List<LegBean> legs) {

    if (!fromStop.equals(toStop)) {

      long timeFrom = state.getBackState().getTime();
      long timeTo = state.getTime();

      GraphPath path = _itinerariesService.getWalkingItineraryBetweenStops(
          fromStop, toStop, new Date(timeFrom), options);

      if (path != null) {
        ItineraryBean walk = getPathAsItinerary(path, options);
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

    return extendTransitLegWithDepartureAndArrival(legs, builder, from, to);
  }

  private TransitLegBuilder extendTransitLegWithTPDepartureAndArrival(
      List<LegBean> legs, TransitLegBuilder builder,
      TPBlockDepartureVertex vFrom, TPBlockArrivalVertex vTo) {

    ArrivalAndDepartureInstance from = vFrom.getDeparture();
    ArrivalAndDepartureInstance to = vFrom.getArrival();

    builder = extendTransitLegWithDepartureAndArrival(legs, builder, from, to);

    return getTransitLegBuilderAsLeg(builder, legs);
  }

  private TransitLegBuilder extendTransitLegWithDepartureAndArrival(
      List<LegBean> legs, TransitLegBuilder builder,
      ArrivalAndDepartureInstance from, ArrivalAndDepartureInstance to) {

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
      builder.setNextTrip(tripTo);

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

  private TransitLegBuilder extendTransitLegWithArrival(List<LegBean> legs,
      TransitLegBuilder builder, BlockArrivalVertex arrival, Vertex vTo,
      State state, OBATraverseOptions options) {

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

      addTransferLegIfNeeded(state, fromStop, toStop, options, legs);
    }

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

    CoordinatePoint nextPoint = null;

    BlockTripEntry nextBlockTrip = builder.getNextTrip();
    if (nextBlockTrip != null) {
      TripEntry nextTrip = nextBlockTrip.getTrip();
      AgencyAndId nextShapeId = nextTrip.getShapeId();
      if (nextShapeId != null) {
        ShapePoints nextShapePoints = _shapePointService.getShapePointsForShapeId(nextShapeId);
        nextPoint = nextShapePoints.getPointForIndex(0);
      }
    }

    if (fromStop == null && toStop == null) {
      return ShapeSupport.getFullPath(shapePoints, nextPoint);
    }

    if (fromStop == null && toStop != null) {
      return ShapeSupport.getPartialPathToStop(shapePoints,
          toStop.getStopTime());
    }

    if (fromStop != null && toStop == null) {
      return ShapeSupport.getPartialPathFromStop(shapePoints,
          fromStop.getStopTime(), nextPoint);
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

    BlockLocation blockLocation = fromStopTimeInstance.getBlockLocation();
    if (blockLocation != null) {
      AgencyAndId vehicleId = blockLocation.getVehicleId();
      transitLeg.setVehicleId(AgencyAndIdLibrary.convertToString(vehicleId));
    }
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

    BlockLocation blockLocation = toStopTimeInstance.getBlockLocation();
    if (blockLocation != null) {
      AgencyAndId vehicleId = blockLocation.getVehicleId();
      transitLeg.setVehicleId(AgencyAndIdLibrary.convertToString(vehicleId));
    }
  }

  private int extendStreetLeg(List<State> states, int currentIndex,
      TraverseMode mode, List<LegBean> legs) {

    List<State> streetStates = new ArrayList<State>();

    while (currentIndex < states.size()) {

      State state = states.get(currentIndex);
      EdgeNarrative narrative = state.getBackEdgeNarrative();
      TraverseMode edgeMode = narrative.getMode();

      if (mode != edgeMode)
        break;

      streetStates.add(state);

      currentIndex++;
    }

    if (!streetStates.isEmpty()) {
      getStreetLegBuilderAsLeg(streetStates, mode, legs);
    }

    return currentIndex;
  }

  private void getStreetLegBuilderAsLeg(List<State> streetStates,
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

    for (State state : streetStates) {

      EdgeNarrative edgeResult = state.getBackEdgeNarrative();

      Geometry geom = edgeResult.getGeometry();
      if (geom == null) {
        continue;
      }

      String streetName = edgeResult.getName();

      if (streetLeg == null
          || !ObjectUtils.equals(streetLeg.getStreetName(), streetName)) {

        addPathToStreetLegIfApplicable(streetLeg, path, distance);

        streetLeg = createStreetLeg(state);
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
        startTime = state.getBackState().getTime();
      endTime = state.getTime();

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

  private StreetLegBean createStreetLeg(State state) {

    StreetLegBean bean = new StreetLegBean();
    bean.setStreetName(state.getBackEdgeNarrative().getName());
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

  private void ensureSelectedItineraryIsIncluded(TransitLocationBean from,
      TransitLocationBean to, long targetTime, ItinerariesBean itineraries,
      ItineraryBean selected, OBATraverseOptions options) {

    if (selected == null)
      return;

    if (!isItinerarySufficientlySpecified(selected))
      return;

    for (ItineraryBean itinerary : itineraries.getItineraries()) {
      if (isItineraryMatch(itinerary, selected)) {
        itinerary.setSelected(true);
        return;
      }
    }

    updateItinerary(selected, from, to, targetTime, options);
    selected.setSelected(true);

    itineraries.getItineraries().add(selected);
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

  private void updateItinerary(ItineraryBean itinerary,
      TransitLocationBean from, TransitLocationBean to, long targetTime,
      OBATraverseOptions options) {

    List<LegBean> legs = itinerary.getLegs();

    int firstTransitLegIndex = -1;

    /**
     * Update the legs
     */
    for (int i = 0; i < legs.size(); i++) {

      LegBean leg = legs.get(i);
      TransitLegBean transitLeg = leg.getTransitLeg();

      if (transitLeg != null) {
        LegBean updatedLeg = updateTransitLeg(transitLeg, options);
        legs.set(i, updatedLeg);

        if (firstTransitLegIndex == -1)
          firstTransitLegIndex = i;

      } else if (isStreetLeg(leg)) {

        Date time = new Date(targetTime);

        CoordinatePoint walkFrom = leg.getFrom();
        CoordinatePoint walkTo = leg.getTo();

        /**
         * Adjust the start and end locations for walk-legs to match the query
         * points. This is a hack, since it allows the client to pass in the
         * original unmodified itinerary from a previous to call, but also
         * slightly change their start or end point (aka user walks some
         * distance) without having to understand how to update the itinerary
         * object itself.
         */
        if (i == 0)
          walkFrom = from.getLocation();
        if (i + 1 == legs.size())
          walkTo = to.getLocation();

        GraphPath path = _itinerariesService.getWalkingItineraryBetweenPoints(
            walkFrom, walkTo, time, options);

        if (path == null) {
          throw new IllegalStateException("expected walking path to exist");
        }

        ItineraryBean walkItinerary = getPathAsItinerary(path, options);
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

  private LegBean updateTransitLeg(TransitLegBean transitLeg,
      OBATraverseOptions options) {

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
      StopEntry fromStop = _transitGraphDao.getStopEntryForId(fromStopId, true);
      int fromStopSequence = transitLeg.getFromStopSequence();

      ArrivalAndDepartureQuery query = new ArrivalAndDepartureQuery();
      query.setStop(fromStop);
      query.setStopSequence(fromStopSequence);
      query.setTrip(trip);
      query.setServiceDate(serviceDate);
      query.setVehicleId(vehicleId);
      query.setTime(options.currentTime);

      ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(query);

      b.setFromStop(instance);
      b.setBlockInstance(instance.getBlockInstance());
      b.setBlockTrip(instance.getBlockTrip());
      b.setScheduledDepartureTime(instance.getScheduledDepartureTime());
      b.setPredictedDepartureTime(instance.getPredictedDepartureTime());
    }

    if (transitLeg.getToStop() != null
        && transitLeg.getToStop().getId() != null) {

      AgencyAndId toStopId = AgencyAndIdLibrary.convertFromString(transitLeg.getToStop().getId());
      StopEntry toStop = _transitGraphDao.getStopEntryForId(toStopId, true);
      int toStopSequence = transitLeg.getToStopSequence();

      ArrivalAndDepartureQuery query = new ArrivalAndDepartureQuery();
      query.setStop(toStop);
      query.setStopSequence(toStopSequence);
      query.setTrip(trip);
      query.setServiceDate(serviceDate);
      query.setVehicleId(vehicleId);
      query.setTime(options.currentTime);

      ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(query);

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

  private static class SortByDeparture implements Comparator<ItineraryBean> {

    @Override
    public int compare(ItineraryBean o1, ItineraryBean o2) {
      long t1 = o1.getStartTime();
      long t2 = o2.getStartTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }

  private static class SortByArrival implements Comparator<ItineraryBean> {

    @Override
    public int compare(ItineraryBean o1, ItineraryBean o2) {
      long t1 = o1.getEndTime();
      long t2 = o2.getEndTime();
      return t1 == t2 ? 0 : (t1 > t2 ? -1 : 1);
    }
  }

  private static class TripToStop implements Comparable<TripToStop> {

    private AgencyAndId _stopId;
    private long _transitTimeToStop;
    private double _minTransitTimeToPlace;
    private int _index;

    public TripToStop(AgencyAndId stopId, long transitTimeToStop,
        double minTransitTimeToPlace, int index) {
      _stopId = stopId;
      _transitTimeToStop = transitTimeToStop;
      _minTransitTimeToPlace = minTransitTimeToPlace;
      _index = index;
    }

    public AgencyAndId getStopId() {
      return _stopId;
    }

    public long getTransitTimeToStop() {
      return _transitTimeToStop;
    }

    public double getMinTansitTimeToPlace() {
      return _minTransitTimeToPlace;
    }

    public int getIndex() {
      return _index;
    }

    public int compareTo(TripToStop o) {
      return _minTransitTimeToPlace == o._minTransitTimeToPlace ? 0
          : (_minTransitTimeToPlace < o._minTransitTimeToPlace ? -1 : 1);
    }
  }
}
