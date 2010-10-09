package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.tripplanner.ArrivalSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.BlockTransferSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.DepartureSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.EndSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.RideSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.StartSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.FirstShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.LastShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.LocationShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.tripplanner.BlockTransferState;
import org.onebusaway.transit_data_federation.model.tripplanner.EndState;
import org.onebusaway.transit_data_federation.model.tripplanner.StartState;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleArrivalState;
import org.onebusaway.transit_data_federation.model.tripplanner.VehicleDepartureState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkFromStopState;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPlanBeanService;
import org.onebusaway.transit_data_federation.services.beans.WalkPlanBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlanSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class TripPlanBeanServiceImpl implements TripPlanBeanService {

  private WalkPlanBeanService _walkPlanBeanService;

  private ShapePointService _shapePointService;

  private StopBeanService _stopBeanService;

  private TripBeanService _tripBeanService;

  @Autowired
  public void setWalkPlanBeanService(WalkPlanBeanService walkPlanBeanService) {
    _walkPlanBeanService = walkPlanBeanService;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Transactional
  public List<TripPlanBean> getTripsAsBeans(Collection<TripPlan> trips) {
    List<TripPlanBean> beans = new ArrayList<TripPlanBean>(trips.size());
    for (TripPlan plan : trips)
      beans.add(getTripAsBean(plan));
    return beans;
  }

  @Transactional
  public TripPlanBean getTripAsBean(TripPlan trip) {
    TripPlanBean bean = new TripPlanBean();

    List<TripSegmentBean> segments = bean.getSegments();
    WalkPlanSource walkPlans = trip.getWalkPlans();

    TripState from = null;

    for (TripState to : trip.getStates()) {

      if (from != null) {

        if (from instanceof StartState) {
          handleStartState(segments, (StartState) from, to, walkPlans);
        } else if (from instanceof VehicleDepartureState) {
          handleVehicleDeparture(segments, (VehicleDepartureState) from, to);
        } else if (from instanceof BlockTransferState) {
          handleBlockTransfer(segments, (BlockTransferState) from, to);
        } else if (from instanceof VehicleArrivalState) {
          handleVehicleArrival(segments, (VehicleArrivalState) from, to);
        } else if (from instanceof WalkFromStopState) {
          handleWalkFromStop(segments, (WalkFromStopState) from, to, walkPlans);
        }

        if (to instanceof EndState) {
          EndState endState = (EndState) to;
          CoordinatePoint point = endState.getLocation();
          segments.add(new EndSegmentBean(to.getCurrentTime(), point.getLat(),
              point.getLon()));
        }
      }
      from = to;
    }
    return bean;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void handleStartState(List<TripSegmentBean> segments,
      StartState from, TripState to, WalkPlanSource walkPlans) {

    CoordinatePoint point = from.getLocation();
    segments.add(new StartSegmentBean(from.getCurrentTime(), point.getLat(),
        point.getLon()));

    if (!(to instanceof WalkToStopState || to instanceof EndState))
      throw new IllegalStateException();

    segments.add(getHasWalksAsWalkSegment(from, to, walkPlans));
  }

  private void handleVehicleDeparture(List<TripSegmentBean> segments,
      VehicleDepartureState from, TripState to) {

    StopEntry stopFromProxy = from.getStop();
    StopTimeInstance stiFrom = from.getStopTimeInstance();
    BlockTripEntry blockTripEntry = stiFrom.getTrip();
    TripEntry tripEntry = blockTripEntry.getTrip();

    StopBean stopBean = _stopBeanService.getStopForId(stopFromProxy.getId());
    TripBean tripBean = _tripBeanService.getTripForId(tripEntry.getId());

    String routeShortName = tripBean.getRoute().getShortName();
    String routeLongName = tripBean.getRoute().getLongName();
    String tripHeadsign = ApplicationBeanLibrary.getBestName(tripBean.getTripHeadsign());

    DepartureSegmentBean departure = new DepartureSegmentBean();
    departure.setTime(from.getCurrentTime());
    departure.setStop(stopBean);
    departure.setRouteShortName(routeShortName);
    departure.setRouteLongName(routeLongName);
    departure.setTripHeadsign(tripHeadsign);
    departure.setTripId(AgencyAndIdLibrary.convertToString(tripEntry.getId()));

    segments.add(departure);

    if (to instanceof VehicleArrivalState) {

      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstance stiTo = vas.getStopTimeInstance();

      ShapePointIndex fromIndex = getStopTimeAsShapePointIndex(stiFrom.getStopTime().getStopTime());
      ShapePointIndex toIndex = getStopTimeAsShapePointIndex(stiTo.getStopTime().getStopTime());

      EncodedPolylineBean path = getShapePointsAsEncodedPolyline(
          tripEntry.getShapeId(), fromIndex, toIndex);
      if (path != null)
        segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else if (to instanceof BlockTransferState) {

      ShapePointIndex fromIndex = getStopTimeAsShapePointIndex(stiFrom.getStopTime().getStopTime());
      ShapePointIndex toIndex = new LastShapePointIndex();
      EncodedPolylineBean path = getShapePointsAsEncodedPolyline(
          tripEntry.getShapeId(), fromIndex, toIndex);
      if (path != null)
        segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else {
      throw new IllegalStateException("unexpected follow state: " + to);
    }
  }

  private void handleBlockTransfer(List<TripSegmentBean> segments,
      BlockTransferState from, TripState to) {

    BlockTransferSegmentBean bean = new BlockTransferSegmentBean();
    segments.add(bean);

    BlockTripEntry nextBlockTrip = from.getNextTrip();
    TripEntry nextTrip = nextBlockTrip.getTrip();

    if (to instanceof VehicleArrivalState) {

      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstance stiTo = vas.getStopTimeInstance();

      ShapePointIndex indexFrom = new FirstShapePointIndex();
      ShapePointIndex indexTo = getStopTimeAsShapePointIndex(stiTo.getStopTime().getStopTime());

      EncodedPolylineBean path = getShapePointsAsEncodedPolyline(
          nextTrip.getShapeId(), indexFrom, indexTo);
      if (path != null)
        segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else if (to instanceof BlockTransferState) {

      ShapePointIndex indexFrom = new FirstShapePointIndex();
      ShapePointIndex indexTo = new LastShapePointIndex();

      EncodedPolylineBean path = getShapePointsAsEncodedPolyline(
          nextTrip.getShapeId(), indexFrom, indexTo);
      if (path != null)
        segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else {
      throw new IllegalStateException("unexpected follow state: " + to);
    }
  }

  private void handleVehicleArrival(List<TripSegmentBean> segments,
      VehicleArrivalState from, TripState to) {

    StopTimeInstance sti = from.getStopTimeInstance();
    StopBean stopBean = _stopBeanService.getStopForId(sti.getStop().getId());
    ArrivalSegmentBean arrival = new ArrivalSegmentBean(from.getCurrentTime(),
        stopBean);
    segments.add(arrival);
  }

  private void handleWalkFromStop(List<TripSegmentBean> segments,
      WalkFromStopState from, TripState to, WalkPlanSource walkPlans) {

    if (!(to instanceof WalkToStopState || to instanceof EndState))
      throw new IllegalStateException();

    WalkPlan walkPlan = walkPlans.getWalkPlan(from, to);

    if (walkPlan != null) {
      WalkSegmentBean segmentBean = _walkPlanBeanService.getWalkPlanAsBean(
          from.getCurrentTime(), to.getCurrentTime() - from.getCurrentTime(),
          walkPlan);
      segments.add(segmentBean);
      return;
    }

    WalkToStopState walkToStop = (WalkToStopState) to;
    StopEntry fromStop = from.getStop();
    StopEntry toStop = walkToStop.getStop();
    WalkSegmentBean segmentBean = _walkPlanBeanService.getStopsAsBean(
        from.getCurrentTime(), to.getCurrentTime() - from.getCurrentTime(),
        fromStop, toStop);
    segments.add(segmentBean);
  }

  /*****************************************************************************
   * {@link WalkSegmentBean} Methods
   ****************************************************************************/

  private WalkSegmentBean getHasWalksAsWalkSegment(TripState from,
      TripState to, WalkPlanSource walkPlans) {
    WalkPlan walkPlan = walkPlans.getWalkPlan(from, to);
    return _walkPlanBeanService.getWalkPlanAsBean(from.getCurrentTime(),
        to.getCurrentTime() - from.getCurrentTime(), walkPlan);
  }

  private EncodedPolylineBean getShapePointsAsEncodedPolyline(
      AgencyAndId shapeId, ShapePointIndex fromIndex, ShapePointIndex toIndex) {
    ShapePoints points = _shapePointService.getShapePointsForShapeId(shapeId);
    if (points == null || points.getSize() == 0)
      return null;
    return getPolyline(points, fromIndex, toIndex);
  }

  private ShapePointIndex getStopTimeAsShapePointIndex(StopTimeEntry stopTime) {
    if (stopTime.getShapeDistTraveled() >= 0)
      return new DistanceTraveledShapePointIndex(
          stopTime.getShapeDistTraveled());
    StopEntry stop = stopTime.getStop();
    return new LocationShapePointIndex(stop.getStopLat(), stop.getStopLon());
  }

  private EncodedPolylineBean getPolyline(ShapePoints points,
      ShapePointIndex from, ShapePointIndex to) {
    int fromIndex = from.getIndex(points);
    int toIndex = to.getIndex(points);
    if (fromIndex >= toIndex)
      return null;
    return PolylineEncoder.createEncodings(points.getLats(), points.getLons(),
        fromIndex, toIndex - fromIndex, -1);
  }
}
