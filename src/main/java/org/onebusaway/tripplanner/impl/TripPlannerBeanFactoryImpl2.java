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
import org.onebusaway.tripplanner.model.AtStopState;
import org.onebusaway.tripplanner.model.BlockTransferState;
import org.onebusaway.tripplanner.model.EndState;
import org.onebusaway.tripplanner.model.StartState;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.model.VehicleArrivalState;
import org.onebusaway.tripplanner.model.VehicleDepartureState;
import org.onebusaway.tripplanner.model.VehicleState;
import org.onebusaway.tripplanner.model.WalkFromStopState;
import org.onebusaway.tripplanner.model.WalkNode;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.model.WalkToStopState;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripPlannerBeanFactory;
import org.onebusaway.tripplanner.services.WalkPlanSource;
import org.onebusaway.tripplanner.web.common.client.model.ArrivalSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.BlockTransferSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.DepartureSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.EndSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.RideSegmentBean;
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

public class TripPlannerBeanFactoryImpl2 implements TripPlannerBeanFactory {

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private ProjectionService _projection;

  private static WalkNodePointAdapter _walkNodePointAdapter = new WalkNodePointAdapter();

  @Transactional
  public List<TripBean> getTripsAsBeans(Collection<TripPlan> trips) {

    TripPlannerBeanCache cache = new TripPlannerBeanCache();

    determineRequiredEntities(trips, cache);

    cache.go(_gtfsDao);

    return getTripsAsBeans(trips, cache);
  }

  @Transactional
  public TripBean getTripAsBean(TripPlan trip) {
    throw new UnsupportedOperationException();
  }

  private void determineRequiredEntities(Collection<TripPlan> trips, TripPlannerBeanCache cache) {

    for (TripPlan trip : trips) {
      for (TripState state : trip.getStates()) {
        if (state instanceof VehicleState) {
          VehicleState vds = (VehicleState) state;
          StopTimeInstanceProxy sti = vds.getStopTimeInstance();
          StopTimeProxy st = sti.getStopTime();
          cache.addStopTime(st.getId());
          cache.addTrip(sti.getTripId());
        }
        if (state instanceof AtStopState) {
          AtStopState atStop = (AtStopState) state;
          StopProxy stop = atStop.getStop();
          cache.addStop(stop.getStopId());
        }
      }
    }
  }

  private List<TripBean> getTripsAsBeans(Collection<TripPlan> trips, TripPlannerBeanCache cache) {

    List<TripBean> beans = new ArrayList<TripBean>(trips.size());

    for (TripPlan trip : trips) {

      TripBean bean = new TripBean();
      beans.add(bean);

      List<TripSegmentBean> segments = bean.getSegments();
      WalkPlanSource walkPlans = trip.getWalkPlans();

      for (Pair<TripState> pair : Combinations.getSequentialPairs(trip.getStates())) {

        TripState from = pair.getFirst();
        TripState to = pair.getSecond();

        if (from instanceof StartState) {
          handleStartState(segments, (StartState) from, to, walkPlans);
        } else if (from instanceof VehicleDepartureState) {
          handleVehicleDeparture(segments, (VehicleDepartureState) from, to, cache);
        } else if (from instanceof BlockTransferState) {
          handleBlockTransfer(segments, (BlockTransferState) from, to, cache);
        } else if (from instanceof VehicleArrivalState) {
          handleVehicleArrival(segments, (VehicleArrivalState) from, to, cache);
        } else if (from instanceof WalkFromStopState) {
          handleWalkFromStop(segments, (WalkFromStopState) from, to, walkPlans);
        }

        if (to instanceof EndState) {
          CoordinatePoint point = _projection.getPointAsLatLong(to.getLocation());
          segments.add(new EndSegmentBean(to.getCurrentTime(), point.getLat(), point.getLon()));
        }
      }
    }

    return beans;
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
    if (!(to instanceof WalkToStopState || to instanceof EndState))
      throw new IllegalStateException();
    segments.add(getHasWalksAsWalkSegment(from, to, walkPlans));
    
    CoordinatePoint point = _projection.getPointAsLatLong(from.getLocation());
    segments.add(new EndSegmentBean(from.getCurrentTime(), point.getLat(), point.getLon()));
  }

  private void handleVehicleDeparture(List<TripSegmentBean> segments, VehicleDepartureState from, TripState to,
      TripPlannerBeanCache cache) {

    StopProxy stopFromProxy = from.getStop();
    Stop stopFrom = cache.getStopForId(stopFromProxy.getStopId());

    StopTimeInstanceProxy stiFrom = from.getStopTimeInstance();
    StopTime stFrom = cache.getStopTimeForId(stiFrom.getStopTime().getId());
    Trip trip = cache.getTripForId(stiFrom.getTripId());
    Route route = trip.getRoute();
    String routeName = ApplicationBeanLibrary.getBestName(stFrom.getRouteShortName(), trip.getRouteShortName(),
        route.getShortName());

    DepartureSegmentBean departure = new DepartureSegmentBean();
    departure.setTime(new Date(from.getCurrentTime()));

    StopBean stop = ApplicationBeanLibrary.getStopAsBean(stopFrom);
    departure.setStop(stop);
    departure.setRouteName(routeName);
    segments.add(departure);

    if (to instanceof VehicleArrivalState) {

      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstanceProxy stiTo = vas.getStopTimeInstance();
      StopTime stTo = cache.getStopTimeForId(stiTo.getStopTime().getId());

      PathBean path = getStopTimesAsPath(trip, stFrom, stTo, cache);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else if (to instanceof BlockTransferState) {
      CoordinatePoint p = _projection.getPointAsLatLong(to.getLocation());
      PathBean path = getPathFromStopTimeAndLocation(trip, stFrom, p, cache);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));
    } else {
      throw new IllegalStateException("unexpected follow state: " + to);
    }

    // System.out.println("handleVehicleDeparture - out=" +
    // (System.currentTimeMillis() - ts));
  }

  private void handleBlockTransfer(List<TripSegmentBean> segments, BlockTransferState from, TripState to,
      TripPlannerBeanCache cache) {

    BlockTransferSegmentBean bean = new BlockTransferSegmentBean();
    segments.add(bean);

    Trip trip = cache.getTripForId(from.getNextTripId());

    if (to instanceof VehicleArrivalState) {

      VehicleArrivalState vas = (VehicleArrivalState) to;
      StopTimeInstanceProxy stiTo = vas.getStopTimeInstance();
      StopTime stTo = cache.getStopTimeForId(stiTo.getStopTime().getId());
      CoordinatePoint p = _projection.getPointAsLatLong(from.getLocation());
      PathBean path = getPathFromLocationAndStopTimeAndLocation(trip, p, stTo, cache);
      segments.add(new RideSegmentBean(from.getCurrentTime(), path));

    } else if (to instanceof BlockTransferState) {

      if (trip.getShapeId() != null) {
        List<ShapePoint> points = cache.getShapePointsForShapeId(trip.getShapeId());
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

  private void handleVehicleArrival(List<TripSegmentBean> segments, VehicleArrivalState from, TripState to,
      TripPlannerBeanCache cache) {

    StopTimeInstanceProxy sti = from.getStopTimeInstance();
    StopTime st = cache.getStopTimeForId(sti.getStopTime().getId());
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
    StopProxy fromStop = from.getStop();
    StopProxy toStop = walkToStop.getStop();
    segments.add(getStopsAsWalkSegment(from.getCurrentTime(), fromStop, toStop));
  }

  /*****************************************************************************
   * {@link WalkSegmentBean} Methods
   ****************************************************************************/

  private WalkSegmentBean getHasWalksAsWalkSegment(TripState from, TripState to, WalkPlanSource walkPlans) {
    WalkPlan walkPlan = walkPlans.getWalkPlan(from, to);
    return getWalkPlanAsBean(from.getCurrentTime(), walkPlan);
  }

  private WalkSegmentBean getStopsAsWalkSegment(long time, StopProxy from, StopProxy to) {
    double[] lat = {from.getStopLat(), to.getStopLat()};
    double[] lon = {from.getStopLon(), to.getStopLon()};
    double d = UtilityLibrary.distance(from.getStopLocation(), to.getStopLocation());
    return new WalkSegmentBean(time, new PathBean(lat, lon), d);
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

  public PathBean getPathFromStopTimeAndLocation(Trip trip, StopTime stFrom, CoordinatePoint location,
      TripPlannerBeanCache cache) {
    return getPath(trip, stFrom, getStopTimeAsPoint(stFrom), null, location, cache);
  }

  public PathBean getPathFromLocationAndStopTimeAndLocation(Trip trip, CoordinatePoint location, StopTime stTo,
      TripPlannerBeanCache cache) {
    return getPath(trip, null, location, stTo, getStopTimeAsPoint(stTo), cache);
  }

  public PathBean getStopTimesAsPath(Trip trip, StopTime stFrom, StopTime stTo, TripPlannerBeanCache cache) {
    CoordinatePoint pointFrom = getStopTimeAsPoint(stFrom);
    CoordinatePoint pointTo = getStopTimeAsPoint(stTo);
    return getPath(trip, stFrom, pointFrom, stTo, pointTo, cache);

  }

  private CoordinatePoint getStopTimeAsPoint(StopTime st) {
    Stop stop = st.getStop();
    return new CoordinatePoint(stop.getLat(), stop.getLon());
  }

  private PathBean getPath(Trip trip, StopTime stFrom, CoordinatePoint pointFrom, StopTime stTo,
      CoordinatePoint pointTo, TripPlannerBeanCache cache) {

    if (stFrom == null && stTo == null) {
      double[] lat = {pointFrom.getLat(), pointTo.getLat()};
      double[] lon = {pointFrom.getLon(), pointTo.getLon()};
      return new PathBean(lat, lon);
    }

    List<ShapePoint> points = getShapePoints(trip, cache);

    int preIndex = 0;
    int postIndex = points.size();

    if (stFrom != null) {
      double distanceFrom = stFrom.getShapeDistanceTraveled();
      for (preIndex = 0; preIndex < points.size(); preIndex++) {
        ShapePoint point = points.get(preIndex);
        if (distanceFrom <= point.getDistTraveled())
          break;
      }
    }

    if (stTo != null) {
      double distanceTo = stTo.getShapeDistanceTraveled();
      for (postIndex = points.size(); postIndex > 0; postIndex--) {
        ShapePoint point = points.get(postIndex - 1);
        if (point.getDistTraveled() <= distanceTo)
          break;
      }
    }

    int count = postIndex - preIndex;

    double[] lat = new double[count + 2];
    double[] lon = new double[count + 2];

    for (int i = 0; i < count; i++) {
      ShapePoint point = points.get(i + preIndex);
      lat[i + 1] = point.getLat();
      lon[i + 1] = point.getLon();
    }

    CoordinatePoint p1 = getInterpolatedEndpoint(stFrom, pointFrom, points, preIndex - 1);
    lat[0] = p1.getLat();
    lon[0] = p1.getLon();

    CoordinatePoint p2 = getInterpolatedEndpoint(stTo, pointTo, points, postIndex - 1);
    lat[count + 1] = p2.getLat();
    lon[count + 1] = p2.getLon();

    // System.out.println("path2=" + (System.currentTimeMillis() - t));

    return new PathBean(lat, lon);
  }

  private List<ShapePoint> getShapePoints(Trip trip, TripPlannerBeanCache cache) {

    if (trip.getShapeId() == null)
      return new ArrayList<ShapePoint>();

    List<ShapePoint> shapePoints = cache.getShapePointsForShapeId(trip.getShapeId());

    if (shapePoints == null)
      shapePoints = new ArrayList<ShapePoint>();

    return shapePoints;
  }

  private CoordinatePoint getInterpolatedEndpoint(StopTime stopTime, CoordinatePoint point, List<ShapePoint> points,
      int index) {

    if (stopTime == null || index < 0 || index + 1 >= points.size())
      return point;

    double distanceTraveled = stopTime.getShapeDistanceTraveled();
    ShapePoint a = points.get(index);
    ShapePoint b = points.get(index + 1);
    double ratio = (distanceTraveled - a.getDistTraveled()) / (b.getDistTraveled() - a.getDistTraveled());
    double lat = ratio * b.getLat() + (1 - ratio) * a.getLat();
    double lon = ratio * b.getLon() + (1 - ratio) * a.getLon();
    return new CoordinatePoint(lat, lon);
  }
}
