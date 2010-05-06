package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkNode;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopWalkPlannerService;
import org.onebusaway.tripplanner.services.TripPlannerBeanFactory;
import org.onebusaway.tripplanner.services.WalkPlanSource;
import org.onebusaway.tripplanner.web.common.client.model.ArrivalSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.BlockTransferSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.DepartureSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.EndSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.RideSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.StartSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.WalkSegmentBean;
import org.onebusaway.where.impl.ApplicationBeanLibrary;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.collections.combinations.Combinations;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TripPlannerBeanFactoryImpl implements TripPlannerBeanFactory {

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private TripPathFactoryImpl _tripPathFactory;

  @Autowired
  private StopWalkPlannerService _stopWalkPlanner;

  private static WalkNodePointAdapter _walkNodePointAdapter = new WalkNodePointAdapter();

  @Transactional
  public List<TripBean> getTripsAsBeans(Collection<TripPlan> trips) {
    List<TripBean> beans = new ArrayList<TripBean>(trips.size());
    for (TripPlan trip : trips)
      beans.add(getTripAsBean(trip));
    return beans;
  }

  @Transactional
  public TripBean getTripAsBean(TripPlan trip) {

    TripBean bean = new TripBean();
    List<TripSegmentBean> segments = bean.getSegments();

    WalkPlanSource walkPlans = trip.getWalkPlans();

    for (Pair<TripState> pair : Combinations.getSequentialPairs(trip.getStates())) {

      TripState from = pair.getFirst();
      TripState to = pair.getSecond();

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
      } else if (from instanceof EndState) {
        CoordinatePoint point = _projection.getPointAsLatLong(from.getLocation());
        segments.add(new EndSegmentBean(from.getCurrentTime(), point.getLat(), point.getLon()));
      }
    }

    return bean;
  }

  public WalkSegmentBean getWalkPlanAsBean(long startTime, WalkPlan walkPlan) {

    List<WalkNode> path = walkPlan.getPath();
    Iterable<Point> points = AdapterLibrary.adapt(path, _walkNodePointAdapter);
    List<CoordinatePoint> coordinates = _projection.getPointsAsLatLongs(points, path.size());
    double[] lat = new double[coordinates.size()];
    double[] lon = new double[coordinates.size()];
    int index = 0;
    for (CoordinatePoint point : coordinates) {
      lat[index] = point.getLat();
      lon[index] = point.getLon();
      index++;
    }
    PathBean pathBean = new PathBean(lat, lon);
    return new WalkSegmentBean(startTime, pathBean, walkPlan.getDistance());
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void handleStartState(List<TripSegmentBean> segments, StartState from, TripState to, WalkPlanSource walkPlans) {

    CoordinatePoint point = _projection.getPointAsLatLong(from.getLocation());
    segments.add(new StartSegmentBean(from.getCurrentTime(), point.getLat(), point.getLon()));

    if (!(to instanceof WalkToStopState || to instanceof EndState))
      throw new IllegalStateException();
    segments.add(getHasWalksAsWalkSegment(from, to, walkPlans));
  }

  private void handleVehicleDeparture(List<TripSegmentBean> segments, VehicleDepartureState from, TripState to) {

    StopTimeInstanceProxy stiFrom = from.getStopTimeInstance();
    StopTime stFrom = _gtfsDao.getStopTimeById(stiFrom.getStopTime().getId());
    Trip trip = stFrom.getTrip();
    Route route = trip.getRoute();

    StopProxy stopFromProxy = stiFrom.getStop();
    Stop stopFrom = _gtfsDao.getStopById(stopFromProxy.getStopId());

    DepartureSegmentBean departure = new DepartureSegmentBean();
    departure.setTime(new Date(from.getCurrentTime()));

    StopBean stop = ApplicationBeanLibrary.getStopAsBean(stopFrom);
    departure.setStop(stop);
    departure.setRouteName(ApplicationBeanLibrary.getBestName(stFrom.getRouteShortName(), trip.getRouteShortName(),
        route.getShortName()));
    segments.add(departure);

    if (to instanceof VehicleArrivalState) {

      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstanceProxy stiTo = vas.getStopTimeInstance();
      StopTime stTo = _gtfsDao.getStopTimeById(stiTo.getStopTime().getId());

      PathBean path = _tripPathFactory.getStopTimesAsPath(stFrom, stTo);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else if (to instanceof BlockTransferState) {
      CoordinatePoint p = _projection.getPointAsLatLong(to.getLocation());
      PathBean path = _tripPathFactory.getPathFromStopTimeAndLocation(stFrom, p);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));
    } else {
      throw new IllegalStateException("unexpected follow state: " + to);
    }

    // System.out.println("handleVehicleDeparture - out=" +
    // (System.currentTimeMillis() - ts));
  }

  private void handleBlockTransfer(List<TripSegmentBean> segments, BlockTransferState from, TripState to) {

    BlockTransferSegmentBean bean = new BlockTransferSegmentBean();
    segments.add(bean);

    if (to instanceof VehicleArrivalState) {
      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstanceProxy stiTo = vas.getStopTimeInstance();
      StopTime stTo = _gtfsDao.getStopTimeById(stiTo.getStopTime().getId());
      CoordinatePoint p = _projection.getPointAsLatLong(from.getLocation());
      PathBean path = _tripPathFactory.getPathFromLocationAndStopTimeAndLocation(p, stTo);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));
    } else if (to instanceof BlockTransferState) {
      Trip trip = _gtfsDao.getTripById(from.getNextTripId());
      if (trip.getShapeId() != null) {
        List<ShapePoint> points = _gtfsDao.getShapePointsByShapeId(trip.getShapeId());
        if (points.size() > 0)
          segments.add(getShapePointsAsRideSegment(from.getCurrentTime(), points));
        else
          segments.add(getPointsAsRideSegment(from.getCurrentTime(), from.getLocation(), to.getLocation()));
      } else {
        segments.add(getPointsAsRideSegment(from.getCurrentTime(), from.getLocation(), to.getLocation()));
      }
    } else {
      throw new IllegalStateException("unexpected follow state: " + to);
    }
  }

  private void handleVehicleArrival(List<TripSegmentBean> segments, VehicleArrivalState from, TripState to) {

    StopTimeInstanceProxy sti = from.getStopTimeInstance();
    StopTime st = _gtfsDao.getStopTimeById(sti.getStopTime().getId());
    StopBean stopBean = ApplicationBeanLibrary.getStopAsBean(st.getStop());
    ArrivalSegmentBean arrival = new ArrivalSegmentBean(from.getCurrentTime(), stopBean);
    segments.add(arrival);
  }

  private void handleWalkFromStop(List<TripSegmentBean> segments, WalkFromStopState from, TripState to,
      WalkPlanSource walkPlans) {

    if (!(to instanceof WalkToStopState || to instanceof EndState))
      throw new IllegalStateException();

    WalkPlan walkPlan = walkPlans.getWalkPlan(from, to);

    if (walkPlan != null) {
      segments.add(getWalkPlanAsBean(from.getCurrentTime(), walkPlan));
      return;
    }

    WalkToStopState walkToStop = (WalkToStopState) to;
    Stop fromStop = getStop(from.getStop());
    Stop toStop = getStop(walkToStop.getStop());
    segments.add(getStopsAsWalkSegment(from.getCurrentTime(), fromStop, toStop));
  }

  /*****************************************************************************
   * {@link WalkSegmentBean} Methods
   ****************************************************************************/

  private WalkSegmentBean getHasWalksAsWalkSegment(TripState from, TripState to, WalkPlanSource walkPlans) {
    WalkPlan walkPlan = walkPlans.getWalkPlan(from, to);
    return getWalkPlanAsBean(from.getCurrentTime(), walkPlan);
  }

  private WalkSegmentBean getStopsAsWalkSegment(long time, Stop from, Stop to) {

    try {
      WalkPlan walkPlan = _stopWalkPlanner.getWalkPlanForStopToStop(from, to);
      return getWalkPlanAsBean(time, walkPlan);
    } catch (NoPathException e) {
      double[] lat = {from.getLat(), to.getLat()};
      double[] lon = {from.getLon(), to.getLon()};
      double d = UtilityLibrary.distance(from.getLocation(), to.getLocation());
      return new WalkSegmentBean(time, new PathBean(lat, lon), d);
    }
  }

  private RideSegmentBean getShapePointsAsRideSegment(long time, List<ShapePoint> points) {
    double[] lat = new double[points.size()];
    double[] lon = new double[points.size()];
    int index = 0;
    for (ShapePoint point : points) {
      lat[index] = point.getLat();
      lon[index] = point.getLon();
      index++;
    }
    return new RideSegmentBean(time, new PathBean(lat, lon));
  }

  private TripSegmentBean getPointsAsRideSegment(long time, Point from, Point to) {
    CoordinatePoint pFrom = _projection.getPointAsLatLong(from);
    CoordinatePoint pTo = _projection.getPointAsLatLong(to);
    double[] lat = {pFrom.getLat(), pTo.getLat()};
    double[] lon = {pFrom.getLon(), pTo.getLon()};
    return new RideSegmentBean(time, new PathBean(lat, lon));
  }

  private static class WalkNodePointAdapter implements IAdapter<WalkNode, Point> {
    public Point adapt(WalkNode source) {
      return source.getLocation();
    }
  }

  private Stop getStop(StopProxy proxy) {
    return _gtfsDao.getStopById(proxy.getStopId());
  }

}
