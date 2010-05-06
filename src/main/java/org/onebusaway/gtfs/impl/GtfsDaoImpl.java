package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.model.CalendarDate;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.stats.IntegerInterval;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.collections.tuple.T3;

import com.vividsolutions.jts.geom.Geometry;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;

@Component
@NamedQuery(name = "allStops", query = "SELECT stop FROM Stop stop", hints = {@QueryHint(name = "org.hibernate.readOnly", value = "true")})
class GtfsDaoImpl implements GtfsDao {

  private HibernateTemplate _dao;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _dao = new HibernateTemplate(sessionFactory);
  }

  public IntegerInterval getArrivalTimeIntervalForServiceId(String serviceId) {

    StringBuilder b = new StringBuilder();
    b.append("SELECT min(st.arrivalTime), max(st.arrivalTime)");
    b.append(" FROM Trip trip, StopTime st");
    b.append(" WHERE trip.serviceId = :serviceId");
    b.append(" AND trip = st.id.trip");

    List<?> result = _dao.findByNamedParam(b.toString(), "serviceId", serviceId);

    if (result.size() != 1)
      throw new IllegalStateException();

    Object[] row = (Object[]) result.get(0);
    Integer min = (Integer) row[0];
    Integer max = (Integer) row[1];
    return new IntegerInterval(min, max);
  }

  public IntegerInterval getDepartureTimeIntervalForServiceId(String serviceId) {

    StringBuilder b = new StringBuilder();
    b.append("SELECT min(st.departureTime), max(st.departureTime)");
    b.append(" FROM Trip trip, StopTime st");
    b.append(" WHERE trip.serviceId = :serviceId");
    b.append(" AND trip = st.id.trip");

    List<?> result = _dao.findByNamedParam(b.toString(), "serviceId", serviceId);

    if (result.size() != 1)
      throw new IllegalStateException();

    Object[] row = (Object[]) result.get(0);
    Integer min = (Integer) row[0];
    Integer max = (Integer) row[1];
    return new IntegerInterval(min, max);
  }

  @SuppressWarnings("unchecked")
  public List<CalendarDate> getAllCalendarDates() {
    return _dao.find("from CalendarDate");
  }

  @SuppressWarnings("unchecked")
  public List<ServiceCalendar> getAllCalendars() {
    return _dao.findByNamedQuery("allCalendars");
  }

  public StopTime getStopTimeById(Integer id) {
    return (StopTime) _dao.get(StopTime.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByIds(Collection<Integer> ids) {
    return _dao.findByNamedParam("FROM StopTime stopTime WHERE stopTime.id IN (:ids)", "ids", ids);
  }

  public List<StopTime> getStopTimesByStopAndServiceIds(Stop stop, Set<String> serviceIds) {
    return getStopTimesByStopAndServiceIdsAndTimeRange(stop, serviceIds, 0, 0);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByStopAndServiceIdsAndTimeRange(final Stop stop, final Set<String> serviceIds,
      final int timeFrom, final int timeTo) {

    if (serviceIds.isEmpty())
      return new ArrayList<StopTime>();

    return (List<StopTime>) _dao.execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        StringBuilder q = new StringBuilder();
        q.append("SELECT st");
        q.append(" FROM StopTime st");
        q.append("   LEFT JOIN FETCH st.trip");
        q.append(" WHERE st.stop = :stop");
        q.append("   AND st.trip.serviceId IN (:serviceIds)");

        boolean withTime = timeFrom != 0 || timeTo != 0;
        int tFrom = timeFrom % (24 * 60 * 60);
        int tTo = timeTo % (24 * 60 * 60);

        if (withTime) {

          if (tFrom < tTo) {
            q.append("  AND ((:timeFrom <= mod(st.arrivalTime,86400) AND mod(st.arrivalTime,86400) <= :timeTo)");
            q.append("  OR (:timeFrom <= mod(st.departureTime,86400) AND mod(st.departureTime,86400) <= :timeTo))");
          } else {
            q.append("  AND ((:timeFrom <= mod(st.arrivalTime,86400) OR mod(st.arrivalTime,86400) <= :timeTo)");
            q.append("  OR (:timeFrom <= mod(st.departureTime,86400) OR mod(st.departureTime,86400) <= :timeTo))");
          }
        }

        Query query = session.createQuery(q.toString());

        query.setParameter("stop", stop);
        query.setParameterList("serviceIds", serviceIds);

        if (withTime) {
          query.setInteger("timeFrom", tFrom);
          query.setInteger("timeTo", tTo);
        }

        return query.list();
      }
    });
  }

  public Stop getStopById(String id) {
    return (Stop) _dao.get(Stop.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getStopsByIds(Collection<String> ids) {
    return _dao.findByNamedParam("FROM Stop stop WHERE stop.id IN (:ids)", "ids", ids);
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByStopId(String id) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT route FROM");
    q.append("  Route route,");
    q.append("  Trip trip,");
    q.append("  StopTime stopTime");
    q.append(" WHERE");
    q.append("  stopTime.stop.id = :id");
    q.append("  AND stopTime.trip = trip");
    q.append("  AND trip.route = route");
    q.append(" GROUP BY route");
    return _dao.findByNamedParam(q.toString(), "id", id);
  }

  @SuppressWarnings("unchecked")
  public List<Route> getAllRoutes() {
    return _dao.find("FROM Route route");
  }

  @SuppressWarnings("unchecked")
  public Route getRouteByShortName(String route) {
    List<Route> routes = _dao.findByNamedParam("FROM Route route WHERE route.shortName = :shortName", "shortName",
        route);
    if (routes.isEmpty())
      return null;
    return routes.get(0);
  }

  public Route getRouteById(String id) {
    return (Route) _dao.get(Route.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByShortNames(Set<String> routeShortNames) {
    return _dao.findByNamedParam("FROM Route route WHERE route.shortName IN (:shortNames)", "shortNames",
        routeShortNames);
  }

  public Map<Stop, List<T2<Route, String>>> getRoutesAndDirectionIdsForStops(Collection<Stop> stops) {

    Map<String, Stop> stopsById = CollectionsLibrary.mapToValue(stops, "id", String.class);

    StringBuilder q = new StringBuilder();
    q.append("SELECT stopTime.stop.id, trip.directionId, route FROM");
    q.append("  Route route,");
    q.append("  Trip trip,");
    q.append("  StopTime stopTime");
    q.append(" WHERE");
    q.append("  stopTime.stop IN (:stops)");
    q.append("  AND stopTime.trip = trip");
    q.append("  AND trip.route = route");
    q.append(" GROUP BY stopTime.stop.id, trip.directionId, route");
    List<?> rows = _dao.findByNamedParam(q.toString(), "stops", stops);

    Map<Stop, List<T2<Route, String>>> result = new HashMap<Stop, List<T2<Route, String>>>();

    for (Object row : rows) {
      Object[] items = (Object[]) row;
      String stopId = (String) items[0];
      Stop stop = stopsById.get(stopId);
      List<T2<Route, String>> routesAndDirectionIds = result.get(stop);
      if (routesAndDirectionIds == null) {
        routesAndDirectionIds = new ArrayList<T2<Route, String>>();
        result.put(stop, routesAndDirectionIds);
      }
      String directionId = (String) items[1];
      Route route = (Route) items[2];
      routesAndDirectionIds.add(T2.create(route, directionId));
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public List<T3<Route, String, Stop>> getRoutesDirectionIdsAndStopsByLocation(final Geometry envelope) {
    final StringBuilder q = new StringBuilder();

    q.append("SELECT new edu.washington.cs.rse.collections.tuple.T3(route,trip.directionId,stop) FROM");
    q.append("  Route route,");
    q.append("  Trip trip,");
    q.append("  StopTime stopTime,");
    q.append("  Stop stop");
    q.append(" WHERE");
    q.append("  within(stop.location,:envelope) = true");
    q.append("  AND stopTime.stop = stop");
    q.append("  AND stopTime.trip = trip");
    q.append("  AND trip.route = route");
    q.append(" GROUP BY route, trip.directionId, stop");

    return (List<T3<Route, String, Stop>>) _dao.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createQuery(q.toString());
        Type geometryType = new CustomType(GeometryUserType.class, null);
        query.setParameter("envelope", envelope, geometryType);
        return query.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByLocation(final Geometry envelope) {

    final StringBuilder q = new StringBuilder();

    q.append("SELECT route FROM");
    q.append("  Route route,");
    q.append("  Trip trip,");
    q.append("  StopTime stopTime,");
    q.append("  Stop stop");
    q.append(" WHERE");
    q.append("  within(stop.location,:envelope) = true");
    q.append("  AND stopTime.stop = stop");
    q.append("  AND stopTime.trip = trip");
    q.append("  AND trip.route = route");
    q.append(" GROUP BY route");

    // return _dao.findByNamedParam(q.toString(), "envelope", envelope);

    return (List<Route>) _dao.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createQuery(q.toString());
        Type geometryType = new CustomType(GeometryUserType.class, null);
        query.setParameter("envelope", envelope, geometryType);
        return query.list();
      }
    });
  }

  public Map<Integer, String[]> getRouteShortNamesByStopTimeIds(Set<Integer> ids) {

    StringBuilder q = new StringBuilder();
    q.append("SELECT stopTime.id, stopTime.routeShortName, trip.routeShortName, route.shortName FROM");
    q.append("   StopTime stopTime,");
    q.append("   Trip trip,");
    q.append("   Route route");
    q.append(" WHERE");
    q.append("   stopTime.id IN (:ids)");
    q.append("   AND stopTime.trip = trip");
    q.append("   AND trip.route = route");

    List<?> rows = _dao.findByNamedParam(q.toString(), "ids", ids);

    Map<Integer, String[]> results = new HashMap<Integer, String[]>();

    for (Object row : rows) {
      Object[] data = (Object[]) row;

      Integer id = (Integer) data[0];
      String[] names = new String[3];
      names[0] = (String) data[1];
      names[1] = (String) data[2];
      names[2] = (String) data[3];

      results.put(id, names);
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getAllStopTimes() {
    return _dao.find("SELECT st from StopTime st");
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getAllStops() {
    return _dao.findByNamedQuery("allStops");
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getAllTrips() {
    return _dao.find("SELECT trip FROM Trip trip");
  }

  public Trip getTripById(String id) {
    return (Trip) _dao.get(Trip.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByIds(Collection<String> ids) {
    return _dao.findByNamedParam("FROM Trip trip WHERE trip.id IN (:ids)", "ids", ids);
  }

  @SuppressWarnings("unchecked")
  public List<String> getAllBlockIds() {
    return _dao.find("SELECT DISTINCT trip.blockId FROM Trip trip");
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByBlockId(final String blockId) {
    return _dao.findByNamedQueryAndNamedParam("tripsByBlockId", "blockId", blockId);
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByBlockId(Set<String> blockIds) {
    return _dao.findByNamedParam("SELECT trip FROM Trip trip WHERE trip.blockId IN (:blockIds)", "blockIds", blockIds);
  }

  @SuppressWarnings("unchecked")
  public Trip getNextTripInBlock(Trip trip) {
    if (trip.getBlockId() == null)
      throw new IllegalArgumentException("Trip has no block id");
    String[] names = {"blockId", "sequenceId"};
    Object[] values = {trip.getBlockId(), trip.getBlockSequenceId() + 1};
    List<Trip> trips = _dao.findByNamedParam(
        "SELECT trip FROM Trip trip WHERE trip.blockId = :blockId and trip.blockSequenceId = :sequenceId", names,
        values);
    if (trips.size() == 0)
      return null;
    if (trips.size() == 1)
      return trips.get(0);
    throw new IllegalStateException("Multiple trips with the same blockId and blockSequenceId");
  }

  @SuppressWarnings("unchecked")
  public Trip getPreviousTripInBlock(Trip trip) {
    if (trip.getBlockId() == null)
      throw new IllegalArgumentException("Trip has no block id");
    String[] names = {"blockId", "sequenceId"};
    Object[] values = {trip.getBlockId(), trip.getBlockSequenceId() - 1};
    List<Trip> trips = _dao.findByNamedParam(
        "SELECT trip FROM Trip trip WHERE trip.blockId = :blockId and trip.blockSequenceId = :sequenceId", names,
        values);
    if (trips.size() == 0)
      return null;
    if (trips.size() == 1)
      return trips.get(0);
    throw new IllegalStateException("Multiple trips with the same blockId and blockSequenceId");
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByRoute(Route route) {
    return _dao.findByNamedParam("SELECT trip FROM Trip trip WHERE trip.route = :route", "route", route);
  }

  @SuppressWarnings("unchecked")
  public List<String> getServiceIdsByStop(Stop stop) {
    return _dao.findByNamedParam(
        "SELECT st.trip.serviceId FROM StopTime st WHERE st.stop = :stop GROUP BY st.trip.serviceId", "stop", stop);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByStop(Stop stop) {
    return _dao.findByNamedParam("SELECT st FROM StopTime st WHERE st.stop = :stop", "stop", stop);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByTrip(Trip trip) {
    return _dao.findByNamedParam("SELECT st FROM StopTime st WHERE st.trip = :trip", "trip", trip);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByBlockId(final String blockId) {
    return _dao.findByNamedQueryAndNamedParam("stopTimesByBlockId", "blockId", blockId);
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getStopsByLocation(final Geometry envelope) {
    return (List<Stop>) _dao.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria criteria = session.createCriteria(Stop.class).add(SpatialRestrictions.filter("location", envelope));
        return criteria.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getStopsByLocation(final Geometry envelope, final int resultLimit) {
    return (List<Stop>) _dao.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria criteria = session.createCriteria(Stop.class).add(SpatialRestrictions.filter("location", envelope));
        criteria.setMaxResults(resultLimit);
        return criteria.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public Set<String> getDirectionIdsByRoute(Route route) {
    List<String> ids = _dao.findByNamedParam(
        "SELECT trip.directionId FROM Trip trip WHERE trip.route = :route GROUP BY trip.directionId", "route", route);
    return new HashSet<String>(ids);
  }

  public Map<Trip, StopTime> getFirstStopTimesByTrips(Collection<Trip> trips) {
    List<?> rows = _dao.findByNamedParam(
        "SELECT st, min(st.arrivalTime) FROM StopTime st WHERE st.trip IN (:trips) GROUP BY st.trip", "trips", trips);
    Map<Trip, StopTime> results = new HashMap<Trip, StopTime>();
    for (Object row : rows) {
      Object[] values = (Object[]) row;
      StopTime st = (StopTime) values[0];
      results.put(st.getTrip(), st);
    }
    return results;
  }

  public Map<Trip, StopTime> getLastStopTimesByTrips(Collection<Trip> trips) {
    List<?> rows = _dao.findByNamedParam(
        "SELECT st, max(st.departureTime) FROM StopTime st WHERE st.trip IN (:trips) GROUP BY st.trip", "trips", trips);
    Map<Trip, StopTime> results = new HashMap<Trip, StopTime>();
    for (Object row : rows) {
      Object[] values = (Object[]) row;
      StopTime st = (StopTime) values[0];
      results.put(st.getTrip(), st);
    }
    return results;
  }

  public Map<Trip, StopTime> getFirstStopTimesByRoute(Route route) {
    List<?> rows = _dao.findByNamedParam(
        "SELECT st, min(st.arrivalTime) FROM StopTime st WHERE st.trip.route = :route GROUP BY st.trip", "route", route);
    Map<Trip, StopTime> results = new HashMap<Trip, StopTime>();
    for (Object row : rows) {
      Object[] values = (Object[]) row;
      StopTime st = (StopTime) values[0];
      results.put(st.getTrip(), st);
    }
    return results;
  }

  @SuppressWarnings("unchecked")
  public List<ShapePoint> getShapePointsByShapeId(String shapeId) {
    return _dao.findByNamedParam("from ShapePoint point WHERE point.id.shapeId = :shapeId", "shapeId", shapeId);
  }

  @SuppressWarnings("unchecked")
  public List<ShapePoint> getShapePointsByShapeIds(Set<String> shapeIds) {
    return _dao.findByNamedParam("from ShapePoint point WHERE point.id.shapeId IN (:shapeIds)", "shapeIds", shapeIds);
  }

  @SuppressWarnings("unchecked")
  public List<ShapePoint> getShapePointsByShapeIdAndDistanceRange(String shapeId, double dFrom, double dTo) {

    if (dTo <= dFrom)
      return new ArrayList<ShapePoint>();

    String[] names = {"id", "dFrom", "dTo"};
    Object[] values = {shapeId, dFrom, dTo};
    return _dao.findByNamedParam(
        "FROM ShapePoint point WHERE point.id.shapeId = :id AND :dFrom <= point.distTraveled AND point.distTraveled <= :dTo",
        names, values);
  }

  @SuppressWarnings("unchecked")
  public List<ShapePoint> getShapePointsByShapeIdAndDistanceFrom(String shapeId, double distanceFrom) {

    String[] names = {"id", "dFrom"};
    Object[] values = {shapeId, distanceFrom};
    return _dao.findByNamedParam(
        "FROM ShapePoint point WHERE point.id.shapeId = :id AND :dFrom <= point.distTraveled ", names, values);
  }

  @SuppressWarnings("unchecked")
  public List<ShapePoint> getShapePointsByShapeIdAndDistanceTo(String shapeId, double distanceTo) {

    String[] names = {"id", "dTo"};
    Object[] values = {shapeId, distanceTo};
    return _dao.findByNamedParam("FROM ShapePoint point WHERE point.id.shapeId = :id AND point.distTraveled <= :dTo",
        names, values);
  }
}
