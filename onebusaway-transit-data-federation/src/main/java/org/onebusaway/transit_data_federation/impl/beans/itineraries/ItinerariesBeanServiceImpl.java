package org.onebusaway.transit_data_federation.impl.beans.itineraries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.LocationBean;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.beans.FrequencyBeanLibrary;
import org.onebusaway.transit_data_federation.impl.otp.BlockArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.BlockDepartureVertex;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.beans.ItinerariesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTEdge;
import org.opentripplanner.routing.spt.SPTVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

@Component
class ItinerariesBeanServiceImpl implements ItinerariesBeanService {

  private PathService _pathService;

  private TripBeanService _tripBeanService;

  private NarrativeService _narrativeService;

  private StopBeanService _stopBeanService;

  private ShapePointService _shapePointService;

  @Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
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
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException {

    String fromPlace = latFrom + "," + lonFrom;
    String toPlace = latTo + "," + lonTo;

    TraverseOptions options = new TraverseOptions();

    List<GraphPath> paths = _pathService.plan(fromPlace, toPlace, new Date(
        constraints.getMinDepartureTime()), options, 1);

    LocationBean from = getPointAsLocation(latFrom, lonFrom);
    LocationBean to = getPointAsLocation(latTo, lonTo);

    return getPathsAsItineraries(paths, from, to);
  }

  /****
   * Private Methods
   ****/

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

        BlockDepartureVertex bdv = (BlockDepartureVertex) vFrom;
        BlockArrivalVertex bav = (BlockArrivalVertex) vTo;

        StopTimeInstance from = bdv.getInstance();
        StopTimeInstance to = bav.getInstance();

        BlockTripEntry tripFrom = from.getTrip();
        BlockTripEntry tripTo = to.getTrip();

        if (builder.getBlockInstance() == null) {
          builder.setStartTime(from.getDepartureTime());
          builder.setBlockInstance(from.getBlockInstance());
          builder.setBlockTrip(from.getTrip());
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
          long transitionTime = (from.getDepartureTime() + to.getArrivalTime()) / 2;

          builder.setEndTime(transitionTime);
          builder.setToStop(null);

          getTransitLegBuilderAsLeg(builder, legs);

          builder = new TransitLegBuilder();
          builder.setStartTime(transitionTime);
          builder.setBlockInstance(to.getBlockInstance());
          builder.setBlockTrip(tripTo);

        }

        builder.setToStop(to);
        builder.setEndTime(to.getArrivalTime());

      } else {
        // Lots of cases we are still missing
      }

      currentIndex++;
    }

    getTransitLegBuilderAsLeg(builder, legs);

    return currentIndex;
  }

  private void getTransitLegBuilderAsLeg(TransitLegBuilder builder,
      List<LegBean> legs) {

    BlockInstance blockInstance = builder.getBlockInstance();
    BlockTripEntry blockTrip = builder.getBlockTrip();

    if (blockInstance == null || blockTrip == null)
      return;

    LegBean leg = new LegBean();
    legs.add(leg);

    leg.setStartTime(builder.getStartTime());
    leg.setEndTime(builder.getEndTime());

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

    transitLeg.setScheduledDepartureTime(builder.getStartTime());
    transitLeg.setScheduledArrivalTime(builder.getEndTime());

    String path = getTransitLegBuilderAsPath(builder);
    transitLeg.setPath(path);

    applyFromStopDetailsForTransitLeg(builder, transitLeg);
    applyToStopDetailsForTransitLeg(builder, transitLeg);
  }

  private double getTransitLegBuilderAsDistance(TransitLegBuilder builder) {

    BlockInstance blockInstance = builder.getBlockInstance();
    BlockConfigurationEntry blockConfig = blockInstance.getBlock();

    BlockStopTimeEntry fromStop = null;
    BlockStopTimeEntry toStop = null;

    if (builder.getFromStop() != null)
      fromStop = builder.getFromStop().getStopTime();

    if (builder.getToStop() != null)
      toStop = builder.getToStop().getStopTime();

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
      fromStop = builder.getFromStop().getStopTime();

    if (builder.getToStop() != null)
      toStop = builder.getToStop().getStopTime();

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

    StopTimeInstance fromStopTimeInstance = builder.getFromStop();

    if (fromStopTimeInstance == null)
      return;

    BlockStopTimeEntry bstFrom = fromStopTimeInstance.getStopTime();

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

    StopTimeInstance toStopTimeInstance = builder.getToStop();

    if (toStopTimeInstance == null)
      return;

    StopEntry toStop = toStopTimeInstance.getStop();
    StopBean toStopBean = _stopBeanService.getStopForId(toStop.getId());
    transitLeg.setToStop(toStopBean);

    BlockStopTimeEntry blockStopTime = toStopTimeInstance.getStopTime();
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
}
