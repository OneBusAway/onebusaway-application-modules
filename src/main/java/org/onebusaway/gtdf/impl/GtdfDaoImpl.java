package org.onebusaway.gtdf.impl;

import edu.washington.cs.rse.collections.stats.IntegerInterval;

import com.vividsolutions.jts.geom.Geometry;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.ServiceCalendar;
import org.onebusaway.gtdf.model.CalendarDate;
import org.onebusaway.gtdf.model.ShapePoint;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.GtdfDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class GtdfDaoImpl implements GtdfDao {

  private HibernateTemplate _dao;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _dao = new HibernateTemplate(sessionFactory);
  }

  public IntegerInterval getPassingTimeIntervalForServiceId(String serviceId) {

    StringBuilder b = new StringBuilder();
    b.append("SELECT min(st.arrivalTime), max(st.departureTime)");
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
    return _dao.find("from ServiceCalendar");
  }

  public List<StopTime> getStopTimesByStopAndServiceIds(Stop stop,
      Set<String> serviceIds) {
    return getStopTimesByStopAndServiceIdsAndTimeRange(stop, serviceIds, 0, 0);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByStopAndServiceIdsAndTimeRange(
      final Stop stop, final Set<String> serviceIds, final int timeFrom,
      final int timeTo) {

    if (serviceIds.isEmpty())
      return new ArrayList<StopTime>();

    return (List<StopTime>) _dao.execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {

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
    List<Route> routes = _dao.findByNamedParam(
        "FROM Route route WHERE route.shortName = :shortName", "shortName",
        route);
    if (routes.isEmpty())
      return null;
    return routes.get(0);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getAllStopTimes() {
    return _dao.find("SELECT st from StopTime st");
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getAllStops() {
    return _dao.find("SELECT stop FROM Stop stop");
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getAllTrips() {
    return _dao.find("SELECT trip FROM Trip trip");
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByBlockId(Set<String> blockIds) {
    return _dao.findByNamedParam(
        "SELECT trip FROM Trip trip WHERE trip.blockId IN (:blockIds)",
        "blockIds", blockIds);
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByRoute(Route route) {
    return _dao.findByNamedParam(
        "SELECT trip FROM Trip trip WHERE trip.route = :route", "route", route);
  }

  @SuppressWarnings("unchecked")
  public List<String> getServiceIdsByStop(Stop stop) {
    return _dao.findByNamedParam(
        "SELECT st.trip.serviceId FROM StopTime st WHERE st.stop = :stop GROUP BY st.trip.serviceId",
        "stop", stop);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByStop(Stop stop) {
    return _dao.findByNamedParam(
        "SELECT st FROM StopTime st WHERE st.stop = :stop", "stop", stop);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByTrip(Trip trip) {
    return _dao.findByNamedParam(
        "SELECT st FROM StopTime st WHERE st.trip = :trip", "trip", trip);
  }

  @SuppressWarnings("unchecked")
  public List<Stop> getStopsByLocation(final Geometry envelope) {
    return (List<Stop>) _dao.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Criteria criteria = session.createCriteria(Stop.class).add(
            SpatialRestrictions.filter("location", envelope));
        return criteria.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public Set<String> getDirectionIdsByRoute(Route route) {
    List<String> ids = _dao.findByNamedParam(
        "SELECT trip.directionId FROM Trip trip WHERE trip.route = :route GROUP BY trip.directionId",
        "route", route);
    return new HashSet<String>(ids);
  }

  public Map<Trip, StopTime> getFirstStopTimesByTrips(List<Trip> trips) {
    List<?> rows = _dao.findByNamedParam(
        "SELECT st, min(st.arrivalTime) FROM StopTime st WHERE st.trip IN (:trips) GROUP BY st.trip",
        "trips", trips);
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
        "SELECT st, min(st.arrivalTime) FROM StopTime st WHERE st.trip.route = :route GROUP BY st.trip",
        "route", route);
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
    return _dao.findByNamedParam(
        "from ShapePoint point WHERE point.id.shapeId = :shapeId", "shapeId",
        shapeId);
  }
}
