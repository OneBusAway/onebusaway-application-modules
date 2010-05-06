/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.collections.tuple.Tuple;
import edu.washington.cs.rse.geospatial.CoordinateProjections;
import edu.washington.cs.rse.geospatial.GeoPoint;
import edu.washington.cs.rse.geospatial.ICoordinateProjection;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.model.ChangeDate;
import edu.washington.cs.rse.transit.common.model.IHasId;
import edu.washington.cs.rse.transit.common.model.LocationBookmarks;
import edu.washington.cs.rse.transit.common.model.PatternTimepoints;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.StreetName;
import edu.washington.cs.rse.transit.common.model.TPI;
import edu.washington.cs.rse.transit.common.model.TPIPath;
import edu.washington.cs.rse.transit.common.model.TPIPathKey;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.TransLink;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePoint;
import edu.washington.cs.rse.transit.common.model.TransNode;
import edu.washington.cs.rse.transit.common.model.Trip;
import edu.washington.cs.rse.transit.common.model.aggregate.InterpolatedStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternTimeBlock;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.mysql.MySQLGeometryUserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class MetroKCDAO {

  private static final int STOP_BY_LOCATION_MAX_RESULT_SIZE = 75;

  public static ICoordinateProjection PROJECTION = CoordinateProjections.WA_NORTH_NAD83_4061_FEET;

  private static GeometryFactory _factory = new GeometryFactory(
      new PrecisionModel(PrecisionModel.FLOATING), 2285);

  private PointToGeopointAdapter _pointToGeoPointAdapter = new PointToGeopointAdapter();

  private HibernateTemplate _t2;

  private ChangeDate _currentServiceRevision = null;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  @Autowired
  public void setSessionFactory(SessionFactory factory) {
    _t2 = new HibernateTemplate(factory);
  }

  /*****************************************************************************
   * ID Methods
   ****************************************************************************/

  public static <T extends IHasId> Map<Integer, T> getElementsById(
      Iterable<T> elements) {
    Map<Integer, T> byId = new HashMap<Integer, T>();
    for (T element : elements)
      byId.put(element.getId(), element);
    return byId;
  }

  /*****************************************************************************
   * Location Projection
   ****************************************************************************/

  public Point getLatLonAsPoint(double lat, double lon) {
    CoordinatePoint fcp = new CoordinatePoint(lat, lon, 0);
    IGeoPoint p = PROJECTION.forward(fcp);
    return _factory.createPoint(new Coordinate(p.getX(), p.getY(), p.getZ()));
  }

  public List<IGeoPoint> getLatLonsAsPoints(List<CoordinatePoint> points) {
    return PROJECTION.forward(points, new ArrayList<IGeoPoint>(points.size()),
        points.size());
  }

  public IGeoPoint getPointAsGeoPoint(Point point) {
    return _pointToGeoPointAdapter.adapt(point);
  }

  public IGeoPoint getLocationAsGeoPoint(double x, double y) {
    return new GeoPoint(PROJECTION, x, y, 0.0);
  }

  public CoordinatePoint getPointAsLatLong(Point p) {
    IGeoPoint gp = getLocationAsGeoPoint(p.getX(), p.getY());
    return gp.getCoordinates();
  }

  public List<CoordinatePoint> getPointsAsLatLongs(Iterable<Point> points,
      int size) {
    Iterable<IGeoPoint> geoPoints = AdapterLibrary.adapt(points,
        _pointToGeoPointAdapter);
    List<CoordinatePoint> cPoints = new ArrayList<CoordinatePoint>(size);
    PROJECTION.reverse(geoPoints, cPoints, size);
    return cPoints;
  }

  /*****************************************************************************
   * Generic
   ****************************************************************************/

  public void save(Object entity) {
    _t2.save(entity);
  }

  public <T> void saveAllEntities(final Collection<T> entities) {
    _t2.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        for (T obj : entities)
          session.save(obj);
        session.flush();
        session.clear();
        return null;
      }
    });
  }

  public void update(Object entity) {
    _t2.update(entity);
  }

  public void saveOrUpdate(Object entity) {
    _t2.saveOrUpdate(entity);
  }

  public <T> void saveOrUpdateAllEntities(Collection<T> entities) {
    _t2.saveOrUpdateAll(entities);
  }

  @SuppressWarnings("unchecked")
  public <T> T getEntity(Class<T> entityClass, Serializable id) {
    return (T) _t2.get(entityClass, id);
  }

  @SuppressWarnings("unchecked")
  public <T> T loadEntity(Class<?> entityClass, Serializable id) {
    return (T) _t2.load(entityClass, id);
  }

  /*****************************************************************************
   * Change Date
   ****************************************************************************/

  public ChangeDate getChangeDateById(int id) {
    return (ChangeDate) _t2.get(ChangeDate.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<ChangeDate> getChangeDates() {
    return _t2.find("from ChangeDate cd");
  }

  @SuppressWarnings("unchecked")
  public synchronized ChangeDate getCurrentServiceRevision() {

    if (_currentServiceRevision == null) {

      List<ChangeDate> cds = _t2.find("from ChangeDate cd WHERE currentNextCode = 'CURRENT'");
      for (ChangeDate cd : cds)
        _currentServiceRevision = cd;
    }

    return _currentServiceRevision;
  }

  /*****************************************************************************
   * Stop Locations
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public List<StopLocation> getAllStopLocations() {
    return _t2.find("from StopLocation stop");
  }

  public StopLocation getStopLocationById(int id) {
    return (StopLocation) _t2.get(StopLocation.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Integer> getAllStopLocationIds() {
    return _t2.find("select stop.id from StopLocation stop");
  }

  @SuppressWarnings("unchecked")
  public List<StopLocation> getStopLocationsByLocation(final Geometry bounds) {
    return (List<StopLocation>) _t2.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Criteria criteria = session.createCriteria(StopLocation.class).add(
            SpatialRestrictions.filter("location", bounds)).setMaxResults(
            STOP_BY_LOCATION_MAX_RESULT_SIZE);
        return criteria.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<StopLocation> getStopLocationsByLocationNoLimit(
      final Geometry bounds) {
    return (List<StopLocation>) _t2.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Criteria criteria = session.createCriteria(StopLocation.class).add(
            SpatialRestrictions.filter("location", bounds));
        return criteria.list();
      }
    });
  }

  /**
   * Return the ordered set of StopLocations that a bus will stop at following a
   * specified service pattern. The {@link StopLocation#getTransLink()} will be
   * a lazy proxy.
   * 
   * @param pattern the service pattern along which to retrieve stops
   * @return the ordered set of stops
   */
  public List<StopLocation> getStopLocationsByServicePattern(
      ServicePattern pattern) {
    return getStopLocationsByServicePattern(pattern, false);
  }

  /**
   * Return the ordered set of StopLocations that a bus will stop at following a
   * specified service pattern.
   * 
   * @param pattern the service pattern along which to retrieve stops
   * @param includeTransLinks if true, the {@link StopLocation#getTransLink()}
   *          object will be eargerly fetched, otherwise it will be a lazy proxy
   * @return the set of {@link StopLocation} objects
   */
  @SuppressWarnings("unchecked")
  public List<StopLocation> getStopLocationsByServicePattern(
      ServicePattern pattern, boolean includeTransLinks) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT stop FROM");
    query.append("  StopLocation stop,");
    query.append("  OrderedPatternStops ops,");
    query.append("  ServicePattern sp");
    if (includeTransLinks)
      query.append("  LEFT JOIN FETCH stop.transLink");
    query.append(" WHERE");
    query.append("  sp = :sp");
    query.append("  AND ops.route = sp.route");
    query.append("  AND ops.schedulePatternId = sp.schedulePatternId");
    query.append("  AND ops.stop = stop");
    query.append(" GROUP BY ops.sequence");
    query.append(" ORDER BY ops.sequence");

    return _t2.findByNamedParam(query.toString(), "sp", pattern);
  }

  public List<T2<StopLocation, Boolean>> getStopLocationsAndPptFlagByServicePattern(
      ServicePattern pattern, boolean includeTransLinks) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT");
    query.append(" stop, ops.pptFlag");
    query.append(" FROM");
    query.append("  StopLocation stop,");
    query.append("  OrderedPatternStops ops,");
    query.append("  ServicePattern sp");
    if (includeTransLinks)
      query.append("  LEFT JOIN FETCH stop.transLink");
    query.append(" WHERE");
    query.append("  sp = :sp");
    query.append("  AND ops.route = sp.route");
    query.append("  AND ops.schedulePatternId = sp.schedulePatternId");
    query.append("  AND ops.stop = stop");
    query.append(" GROUP BY ops.sequence");
    query.append(" ORDER BY ops.sequence");

    List<?> rows = _t2.findByNamedParam(query.toString(), "sp", pattern);
    List<T2<StopLocation, Boolean>> results = new ArrayList<T2<StopLocation, Boolean>>();

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      StopLocation stop = (StopLocation) elements[0];
      String pptFlag = (String) elements[1];
      boolean isPpt = pptFlag.equals("YES");
      results.add(T2.create(stop, isPpt));
    }
    return results;
  }

  /*****************************************************************************
   * Routes
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public List<Route> getAllRoutes() {
    return _t2.find("from Route route order by route.number");
  }

  public List<Route> getActiveRoutes() {
    return getRoutesByChangeDate(getCurrentServiceRevision());
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByChangeDate(ChangeDate serviceRevision) {

    StringBuilder sql = new StringBuilder();

    sql.append("SELECT route FROM");
    sql.append(" Route route,");
    sql.append(" ServicePattern servicePattern");
    sql.append(" WHERE route = servicePattern.route");
    sql.append(" AND servicePattern.id.changeDate = ?");
    sql.append(" GROUP BY route ORDER BY route.number");

    return _t2.find(sql.toString(), serviceRevision);
  }

  @SuppressWarnings("unchecked")
  public Route getRouteByNumber(int number) {
    List<Route> routes = _t2.find("from Route route where number = ?", number);
    if (routes.isEmpty())
      return null;
    return routes.get(0);
  }

  public List<Route> getActiveRoutesByStopId(int stopId) {
    return getRoutesByChangeDateAndStopId(getCurrentServiceRevision(), stopId);
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByChangeDateAndStopId(ChangeDate serviceRevision,
      int stopId) {
    StringBuilder query = new StringBuilder();
    query.append("SELECT route FROM");
    query.append(" Route route,");
    query.append(" OrderedPatternStops ops,");
    query.append(" ServicePattern sp");
    query.append(" WHERE ops.stop.id = ?");
    query.append(" AND ops.route = route");
    query.append(" AND route = sp.route");
    query.append(" AND sp.id.changeDate = ?");
    query.append(" GROUP BY route");
    query.append(" ORDER BY route.number");
    return _t2.find(query.toString(), new Object[] {stopId, serviceRevision});
  }

  public List<Route> getActiveRoutesByTimepointId(int timepointId) {
    return getRoutesByChangeDateAndTimepointId(getCurrentServiceRevision(),
        timepointId);
  }

  @SuppressWarnings("unchecked")
  public List<Route> getRoutesByChangeDateAndTimepointId(
      ChangeDate serviceRevision, int timepointId) {

    StringBuilder sql = new StringBuilder();

    sql.append("SELECT route");
    sql.append(" FROM ");
    sql.append(" Route route,");
    sql.append(" PatternTimepoints pt,");
    sql.append(" ServicePattern sp");
    sql.append(" WHERE ");
    sql.append(" pt.timepoint.id = ?");
    sql.append(" AND sp.id.changeDate = ?");
    sql.append(" AND pt.servicePattern = sp");
    sql.append(" AND route = sp.route");
    sql.append(" GROUP BY route");

    return _t2.find(sql.toString(), new Object[] {timepointId, serviceRevision});
  }

  /*****************************************************************************
   * Service Patterns
   ****************************************************************************/

  public ServicePattern getServicePatternById(ServicePatternKey id) {
    return (ServicePattern) _t2.get(ServicePattern.class, id);
  }

  public ServicePattern getActiveServicePatternById(int id) {
    return getServicePatternById(new ServicePatternKey(
        getCurrentServiceRevision(), id));
  }

  @SuppressWarnings("unchecked")
  public List<ServicePattern> getAllServicePatterns() {
    return _t2.find("from ServicePattern sp LEFT JOIN FETCH sp.route ORDER BY sp.route.number, sp.id");
  }

  @SuppressWarnings("unchecked")
  public List<ServicePattern> getActiveServicePatterns() {
    ChangeDate current = getCurrentServiceRevision();
    return _t2.find("from ServicePattern sp WHERE sp.id.changeDate = ?",
        current);
  }

  public List<ServicePattern> getActiveServicePatternsByRoute(Route route) {
    return getServicePatternsByChangeDateAndRoute(getCurrentServiceRevision(),
        route);
  }

  @SuppressWarnings("unchecked")
  public List<ServicePattern> getServicePatternsByChangeDateAndRoute(
      ChangeDate serviceRevision, Route route) {
    return _t2.find(
        "from ServicePattern sp WHERE sp.id.changeDate = ? AND sp.route = ?",
        new Object[] {serviceRevision, route});
  }

  public List<ServicePatternTimeBlock> getActiveServicePatternTimeBlocksByRoute(
      Route route) {
    return getServicePatternTimeBlocksByRoute(getCurrentServiceRevision(),
        route);
  }

  public List<ServicePatternTimeBlock> getServicePatternTimeBlocksByRoute(
      ChangeDate revision, Route route) {

    StringBuilder q = new StringBuilder();
    q.append("SELECT");
    q.append(" sp, trip.scheduleType, min(st.passingTime), max(st.passingTime)");
    q.append(" FROM");
    q.append(" ServicePattern sp,");
    q.append(" StopTime st,");
    q.append(" Trip trip");
    q.append(" WHERE");
    q.append(" sp.id.changeDate = ?");
    q.append(" AND sp.route = ?");
    q.append(" AND sp = st.servicePattern");
    q.append(" AND st.trip = trip");
    q.append(" GROUP BY sp.id.id, trip.scheduleType");
    q.append(" ORDER BY trip.scheduleType");

    List<?> data = _t2.find(q.toString(), new Object[] {revision, route});

    List<ServicePatternTimeBlock> blocks = new ArrayList<ServicePatternTimeBlock>(
        data.size());
    for (Object o : data) {
      Object[] row = (Object[]) o;
      ServicePattern sp = (ServicePattern) row[0];
      String scheduleType = (String) row[1];
      Double from = (Double) row[2];
      Double to = (Double) row[3];
      blocks.add(new ServicePatternTimeBlock(sp, scheduleType, from, to));
    }

    return blocks;
  }

  public List<ServicePatternTimeBlock> getActiveSegmentedServicePatternTimeBlocksByRoute(
      Route route) {
    return getSegmentedServicePatternTimeBlocksByRoute(
        getCurrentServiceRevision(), route);
  }

  @SuppressWarnings("unchecked")
  public List<ServicePatternTimeBlock> getSegmentedServicePatternTimeBlocksByRoute(
      ChangeDate revision, Route route) {

    StringBuilder q = new StringBuilder();
    q.append("SELECT");
    q.append(" sptb");
    q.append(" FROM");
    q.append(" ServicePatternTimeBlock sptb");
    q.append(" WHERE");
    q.append(" sptb.servicePattern.id.changeDate = ?");
    q.append(" AND sptb.servicePattern.route = ?");
    q.append(" ORDER BY sptb.scheduleType, sptb.minPassingTime");

    return _t2.find(q.toString(), new Object[] {revision, route});
  }

  /*****************************************************************************
   * Trips
   ****************************************************************************/

  public Trip getTripById(int id) {
    return (Trip) _t2.get(Trip.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Trip> getTripsByServicePattern(ServicePattern pattern) {
    return _t2.findByNamedParam(
        "SELECT t FROM Trip t where servicePattern = :sp", "sp", pattern);
  }

  /*****************************************************************************
   * Timepoints
   ****************************************************************************/

  public Timepoint getTimepointById(int id) {
    return (Timepoint) _t2.get(Timepoint.class, id);
  }

  @SuppressWarnings("unchecked")
  public List<Timepoint> getTimepointsById(final Collection<Integer> ids) {
    return (List<Timepoint>) _t2.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.createQuery("from Timepoint tp where tp.id in (:values)");
        query.setParameterList("values", ids);
        return query.list();
      }
    });
  }

  public List<Timepoint> getTimepointsByLocation(Geometry g) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<Timepoint> getTimepointsByServicePattern(ServicePattern pattern) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT tp FROM");
    query.append(" Timepoint tp,");
    query.append(" PatternTimepoints pt");
    query.append(" LEFT JOIN FETCH tp.transNode");
    query.append(" WHERE");
    query.append(" pt.servicePattern = :sp");
    query.append(" AND pt.timepoint = tp");
    query.append(" ORDER BY pt.patternTimepointPosition");

    return _t2.findByNamedParam(query.toString(), "sp", pattern);
  }

  @SuppressWarnings("unchecked")
  public List<Timepoint> getAllTimepoints() {
    return _t2.find("from Timepoint tp");
  }

  public Map<Timepoint, Number> getTimepointsWithFrequency() {
    List<?> objs = _t2.find("select st.timepoint, count(*) from StopTime st GROUP BY st.timepoint ORDER BY count(*)");
    Map<Timepoint, Number> timepointsWithFrequency = new HashMap<Timepoint, Number>();
    for (Object obj : objs) {
      Object[] row = (Object[]) obj;
      Timepoint tp = (Timepoint) row[0];
      Number freq = (Number) row[1];
      timepointsWithFrequency.put(tp, freq);
    }
    return timepointsWithFrequency;
  }

  /*****************************************************************************
   * {@link PatternTimepoints} Methods
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public List<PatternTimepoints> getPatternTimepointsByServicePattern(
      ServicePattern pattern) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT pt FROM");
    query.append(" PatternTimepoints pt");
    query.append(" WHERE pt.servicePattern = :sp");
    query.append(" ORDER BY pt.patternTimepointPosition");

    return _t2.findByNamedParam(query.toString(), "sp", pattern);
  }

  public Map<PatternTimepoints, List<TPIPath>> getPatternTimepointsAndTPIPathByServicePattern(
      ServicePattern pattern) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT pt, tpip, tpi FROM");
    query.append(" PatternTimepoints pt,");
    query.append(" TPIPath tpip,");
    query.append(" TPI tpi");
    query.append(" LEFT JOIN FETCH tpip.transLink");
    query.append(" LEFT JOIN FETCH tpip.transLink.transNodeFrom");
    query.append(" LEFT JOIN FETCH tpip.transLink.transNodeTo");
    query.append(" WHERE pt.servicePattern = :sp");
    query.append(" AND pt.tpi = tpi");
    query.append(" AND tpi = tpip.id.tpi");
    query.append(" ORDER BY pt.patternTimepointPosition, tpip.id.sequence");

    List<?> rows = _t2.findByNamedParam(query.toString(), "sp", pattern);

    Map<PatternTimepoints, List<TPIPath>> results = new LinkedHashMap<PatternTimepoints, List<TPIPath>>();

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      PatternTimepoints pt = (PatternTimepoints) elements[0];
      TPIPath path = (TPIPath) elements[1];
      List<TPIPath> paths = results.get(pt);
      if (paths == null) {
        paths = new ArrayList<TPIPath>();
        results.put(pt, paths);
      }
      paths.add(path);
    }

    return results;
  }

  /*****************************************************************************
   * {@link TPI}
   ****************************************************************************/

  public TPI getTPIById(int id) {
    return (TPI) _t2.get(TPI.class, id);
  }

  /****
   * {@link TransNode}
   ****/

  @SuppressWarnings("unchecked")
  public List<TransNode> getAllTransNodes() {
    return (List<TransNode>) _t2.find("from TransNode node");
  }

  /*****************************************************************************
   * {@link TransLink} Methods
   ****************************************************************************/

  public TransLink getTransLinkById(int id) {
    return (TransLink) _t2.get(TransLink.class, id);
  }

  /*****************************************************************************
   * {@link TransLinkShapePoint} Methods
   ****************************************************************************/

  public List<TransLinkShapePoint> getTransLinkShapePointsByServicePattern(
      ServicePattern pattern) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT tpip.id, tpip.flowDirection, tlsp FROM");
    query.append(" PatternTimepoints pt,");
    query.append(" TPIPath tpip,");
    query.append(" TransLinkShapePoint tlsp");
    query.append(" WHERE pt.servicePattern = :sp");
    query.append(" AND pt.tpi = tpip.id.tpi");
    query.append(" AND tpip.transLink = tlsp.id.transLink");
    query.append(" ORDER BY pt.patternTimepointPosition, tpip.id.sequence, tlsp.id.sequence");

    List<?> rows = _t2.findByNamedParam(query.toString(), "sp", pattern);

    TPIPathKey currentKey = null;
    LinkedList<TransLinkShapePoint> points = new LinkedList<TransLinkShapePoint>();
    List<TransLinkShapePoint> results = new ArrayList<TransLinkShapePoint>(
        rows.size());

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      TPIPathKey key = (TPIPathKey) elements[0];
      Integer flowDirection = (Integer) elements[1];
      TransLinkShapePoint point = (TransLinkShapePoint) elements[2];

      if (currentKey == null || !currentKey.equalsByProxy(key)) {
        currentKey = key;
        if (!points.isEmpty()) {
          results.addAll(points);
          points.clear();
        }
      }

      if (flowDirection == 0)
        points.addLast(point);
      else
        points.addFirst(point);
    }

    if (!points.isEmpty()) {
      results.addAll(points);
      points.clear();
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  public List<TransLinkShapePoint> getTransLinkShapePointsByTransLink(
      TransLink link) {

    StringBuilder q = new StringBuilder();
    q.append("SELECT tlsp FROM");
    q.append(" TransLinkShapePoint tlsp");
    q.append(" WHERE");
    q.append(" tlsp.id.transLink = :link");
    q.append(" ORDER BY tlsp.id.sequence");

    return _t2.findByNamedParam(q.toString(), "link", link);
  }

  /*****************************************************************************
   * {@link StreetName} Interface
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public StreetName getStreetNameByStopId(int stopId) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT sn FROM");
    query.append(" StreetName sn,");
    query.append(" TransLink tl,");
    query.append(" StopLocation stop");
    query.append(" WHERE stop.id = ?");
    query.append(" AND stop.transLink = tl");
    query.append(" AND tl.streetName = sn");

    List<StreetName> sns = _t2.find(query.toString(), stopId);
    if (sns.isEmpty())
      return null;
    return sns.get(0);
  }

  /*****************************************************************************
   * Stop Times
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByServicePattern(
      ServicePattern servicePattern) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT st");
    q.append(" FROM");
    q.append(" StopTime st");
    q.append(" WHERE st.servicePattern = :sp");
    q.append(" ORDER BY st.trip.id, st.stopTimePosition");
    return _t2.findByNamedParam(q.toString(), "sp", servicePattern);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByServicePatternTimeBlock(
      ServicePatternTimeBlock block) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT st");
    q.append(" FROM");
    q.append(" StopTime st");
    q.append(" WHERE");
    q.append(" st.servicePattern = :servicePattern");
    q.append(" AND st.trip.scheduleType = :scheduleType");
    q.append(" AND st.passingTime >= :minPassingTime");
    q.append(" AND st.passingTime <= :maxPassingTime");

    return _t2.findByValueBean(q.toString(), block);
  }

  @SuppressWarnings("unchecked")
  public List<Double> getPassingTimesByServicePatternTimeBlock(
      ServicePatternTimeBlock block) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT st.passingTime");
    q.append(" FROM");
    q.append(" StopTime st");
    q.append(" WHERE");
    q.append(" st.servicePattern = :servicePattern");
    q.append(" AND st.trip.scheduleType = :scheduleType");
    q.append(" AND st.passingTime >= :minPassingTime");
    q.append(" AND st.passingTime <= :maxPassingTime");

    return _t2.findByValueBean(q.toString(), block);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByTimepoint(Timepoint timepoint) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT st");
    q.append(" FROM");
    q.append(" StopTime st");
    q.append(" LEFT JOIN FETCH st.trip");
    q.append(" WHERE st.timepoint = :tp");
    return _t2.findByNamedParam(q.toString(), "tp", timepoint);
  }

  public List<StopTime> getActiveStopTimesByTimepointAndTimeRange(
      int timepointId, int target, int maxOffset) {
    return getStopTimesByServiceRevisionAndTimepointAndTimeRange(
        getCurrentServiceRevision(), timepointId, target, maxOffset);
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByServiceRevisionAndTimepointAndTimeRange(
      ChangeDate serviceRevision, int timepointId, double target,
      double maxOffset) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT st FROM");
    query.append(" StopTime st");
    query.append(" LEFT JOIN FETCH st.trip");
    query.append(" LEFT JOIN FETCH st.servicePattern");
    query.append(" LEFT JOIN FETCH st.servicePattern.route");
    query.append(" WHERE ");
    query.append(" st.timepoint.id = ?");
    query.append(" AND st.servicePattern.id.changeDate = ?");
    query.append(" AND st.trip = trip");
    query.append("  AND radial_mod( abs(st.passingTime-?) , 1440) < ? ");
    query.append(" ORDER BY st.passingTime");

    return _t2.find(query.toString(), new Object[] {
        timepointId, serviceRevision, target, maxOffset});
  }

  public Map<Timepoint, Set<StopTime>> getActiveStopTimesByTimepointsAndTimeRange(
      Collection<Integer> ids, double target, double maxOffset) {
    return getStopTimesByServiceRevisionAndTimepointsAndTimeRange(
        getCurrentServiceRevision(), ids, target, maxOffset);
  }

  @SuppressWarnings("unchecked")
  public Map<Timepoint, Set<StopTime>> getStopTimesByServiceRevisionAndTimepointsAndTimeRange(
      final ChangeDate serviceRevision, final Collection<Integer> ids,
      final double target, final double maxOffset) {

    if (ids.isEmpty())
      return new HashMap<Timepoint, Set<StopTime>>();

    List<StopTime> rows = (List<StopTime>) _t2.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {

        StringBuilder query = new StringBuilder();

        query.append("SELECT st FROM");
        query.append(" StopTime st");
        query.append(" LEFT JOIN FETCH st.trip");
        query.append(" WHERE ");
        query.append(" st.timepoint.id in (:ids)");
        query.append(" AND st.servicePattern.id.changeDate = (:serviceRevision)");
        query.append("  AND radial_mod( abs(st.passingTime-:target) , 1440) < :maxOffset ");
        query.append(" ORDER BY st.passingTime");

        Query q = session.createQuery(query.toString());

        q.setParameterList("ids", ids);
        q.setParameter("serviceRevision", serviceRevision);
        q.setParameter("target", target);
        q.setParameter("maxOffset", maxOffset);

        return q.list();
      }
    });

    Map<Timepoint, Set<StopTime>> results = new HashMap<Timepoint, Set<StopTime>>();

    for (StopTime st : rows) {
      Set<StopTime> ests = results.get(st.getTimepoint());
      if (ests == null) {
        ests = new HashSet<StopTime>();
        results.put(st.getTimepoint(), ests);
      }
      ests.add(st);
    }

    return results;
  }

  @SuppressWarnings("unchecked")
  public List<StopTime> getStopTimesByServicePatternsAndTimeRange(
      final Collection<ServicePatternKey> ids, final double target,
      final double maxOffset) {

    if (ids.isEmpty())
      return new ArrayList<StopTime>();

    return (List<StopTime>) _t2.execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {

        StringBuilder query = new StringBuilder();

        query.append("SELECT st FROM");
        query.append(" StopTime st");
        query.append(" LEFT JOIN FETCH st.trip");
        query.append(" WHERE ");
        query.append("  st.servicePattern.id in (:ids)");
        query.append("  AND radial_mod( abs(st.passingTime-:target) , 1440) < :maxOffset ");
        query.append(" ORDER BY st.passingTime");

        Query q = session.createQuery(query.toString());

        q.setParameterList("ids", ids);
        q.setParameter("target", target);
        q.setParameter("maxOffset", maxOffset);

        return q.list();
      }
    });
  }

  /*****************************************************************************
   * {@link InterpolatedStopTime} Methods
   ****************************************************************************/

  public Map<Integer, List<InterpolatedStopTime>> getInterpolatedStopTimesByChangeDateAndTimeRange(
      ChangeDate serviceRevision, double target, double maxOffset) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT sti,");
    query.append("  st1.trip,");
    query.append("  (st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)");
    query.append(" FROM ");
    query.append(" StopTimepointInterpolation sti,");
    query.append(" StopTime st1,");
    query.append(" StopTime st2");
    query.append(" WHERE");
    query.append("  sti.servicePattern.id.changeDate = ?");
    query.append("  AND sti.servicePattern = st1.servicePattern");
    query.append("  AND sti.fromTimepoint = st1.timepoint");
    query.append("  AND sti.fromTimepointSequence = st1.stopTimePosition");
    query.append("  AND sti.servicePattern = st2.servicePattern AND sti.toTimepoint = st2.timepoint");
    query.append("  AND sti.toTimepointSequence = st2.stopTimePosition");
    query.append("  AND st1.trip = st2.trip");
    query.append("  AND radial_mod( abs((st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)-?) , 1440) < ? ");

    Map<Integer, List<InterpolatedStopTime>> results = new HashMap<Integer, List<InterpolatedStopTime>>();

    double dTarget = target;
    double dMaxOffset = maxOffset;
    List<?> ro = _t2.find(query.toString(), new Object[] {
        serviceRevision, dTarget, dMaxOffset});

    for (Object obj : ro) {
      Object[] row = (Object[]) obj;
      StopTimepointInterpolation sti = (StopTimepointInterpolation) row[0];
      Trip trip = (Trip) row[1];
      double passingTime = ((Number) row[2]).doubleValue();
      int stopId = sti.getStop().getId();
      List<InterpolatedStopTime> ists = results.get(stopId);
      if (ists == null) {
        ists = new ArrayList<InterpolatedStopTime>();
        results.put(stopId, ists);
      }
      ists.add(new InterpolatedStopTime(sti, trip, passingTime));
    }

    return results;
  }

  public Map<Integer, List<InterpolatedStopTime>> getActiveInterpolatedStopTimesByTimeRange(
      double target, double maxOffset) {
    return getInterpolatedStopTimesByChangeDateAndTimeRange(
        getCurrentServiceRevision(), target, maxOffset);
  }

  public List<InterpolatedStopTime> getInterpolatedStopTimesByChangeDateAndStopAndTimeRange(
      ChangeDate serviceRevision, int stopId, double target, double maxOffset) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT sti,");
    query.append("  st1.trip,");
    query.append("  (st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)");
    query.append(" FROM ");
    query.append(" StopTimepointInterpolation sti,");
    query.append(" StopTime st1,");
    query.append(" StopTime st2");
    query.append(" LEFT JOIN FETCH sti.servicePattern");
    query.append(" LEFT JOIN FETCH sti.servicePattern.route");
    query.append(" WHERE sti.stop.id = ?");
    query.append("  AND sti.servicePattern.id.changeDate = ?");
    query.append("  AND sti.servicePattern = st1.servicePattern");
    query.append("  AND sti.fromTimepoint = st1.timepoint");
    query.append("  AND sti.fromTimepointSequence = st1.stopTimePosition");
    query.append("  AND sti.servicePattern = st2.servicePattern AND sti.toTimepoint = st2.timepoint");
    query.append("  AND sti.toTimepointSequence = st2.stopTimePosition");
    query.append("  AND st1.trip = st2.trip");
    query.append("  AND radial_mod( abs((st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)-?) , 1440) < ? ");

    List<InterpolatedStopTime> rows = new ArrayList<InterpolatedStopTime>();

    double dTarget = target;
    double dMaxOffset = maxOffset;
    List<?> ro = _t2.find(query.toString(), new Object[] {
        stopId, serviceRevision, dTarget, dMaxOffset});

    for (Object obj : ro) {
      Object[] row = (Object[]) obj;
      StopTimepointInterpolation sti = (StopTimepointInterpolation) row[0];
      Trip trip = (Trip) row[1];
      double passingTime = ((Number) row[2]).doubleValue();
      rows.add(new InterpolatedStopTime(sti, trip, passingTime));
    }

    return rows;
  }

  public InterpolatedStopTime getInterpolatedStopTime(Trip trip,
      ServicePattern pattern, StopLocation stop, int stopIndex) {

    StringBuilder q = new StringBuilder();

    q.append("SELECT sti,");
    q.append("  (st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)");
    q.append(" FROM ");
    q.append(" StopTimepointInterpolation sti,");
    q.append(" StopTime st1,");
    q.append(" StopTime st2");
    q.append(" WHERE");
    q.append("  sti.servicePattern = :servicePattern");
    q.append("  AND sti.stop = :stop");
    q.append("  AND sti.stopIndex = :stopIndex");
    q.append("  AND st1.trip = :trip");
    q.append("  AND st2.trip = :trip");
    q.append("  AND sti.fromTimepoint = st1.timepoint");
    q.append("  AND sti.fromTimepointSequence = st1.stopTimePosition");
    q.append("  AND sti.servicePattern = st2.servicePattern AND sti.toTimepoint = st2.timepoint");
    q.append("  AND sti.toTimepointSequence = st2.stopTimePosition");

    String[] params = {"servicePattern", "trip", "stop", "stopIndex"};
    Object[] values = {pattern, trip, stop, stopIndex};

    List<?> results = _t2.findByNamedParam(q.toString(), params, values);

    if (results.size() != 1)
      throw new IllegalStateException("expected one result");

    Object[] row = (Object[]) results.get(0);
    StopTimepointInterpolation sti = (StopTimepointInterpolation) row[0];
    double passingTime = ((Number) row[2]).doubleValue();

    return new InterpolatedStopTime(sti, trip, passingTime);
  }

  public InterpolatedStopTime getNextInterpolatedStopTime(Trip trip,
      ServicePattern pattern, StopLocation stop, int stopIndex) {

    StringBuilder q = new StringBuilder();

    q.append("SELECT sti,");
    q.append("  (st1.passingTime + (st2.passingTime - st1.passingTime) * sti.ratio)");
    q.append(" FROM ");
    q.append(" StopTimepointInterpolation sti,");
    q.append(" StopTime st1,");
    q.append(" StopTime st2");
    q.append(" WHERE");
    q.append("  sti.servicePattern = :servicePattern");
    q.append("  AND sti.stop = :stop");
    q.append("  AND sti.stopIndex = :stopIndex");
    q.append("  AND st1.trip = :trip");
    q.append("  AND st2.trip = :trip");
    q.append("  AND sti.fromTimepoint = st1.timepoint");
    q.append("  AND sti.fromTimepointSequence = st1.stopTimePosition");
    q.append("  AND sti.servicePattern = st2.servicePattern AND sti.toTimepoint = st2.timepoint");
    q.append("  AND sti.toTimepointSequence = st2.stopTimePosition");

    String[] params = {"servicePattern", "trip", "stop", "stopIndex"};
    Object[] values = {pattern, trip, stop, stopIndex};

    List<?> results = _t2.findByNamedParam(q.toString(), params, values);

    if (results.size() != 1)
      throw new IllegalStateException("expected one result");

    Object[] row = (Object[]) results.get(0);
    StopTimepointInterpolation sti = (StopTimepointInterpolation) row[0];
    double passingTime = ((Number) row[2]).doubleValue();

    return new InterpolatedStopTime(sti, trip, passingTime);
  }

  @SuppressWarnings("unchecked")
  public List<StopTimepointInterpolation> getStopTimeInterpolationByServicePattern(
      ServicePattern pattern) {

    StringBuilder q = new StringBuilder();
    q.append("SELECT sti FROM");
    q.append("  StopTimepointInterpolation sti");
    q.append(" WHERE");
    q.append("  sti.servicePattern = :sp");
    return _t2.findByNamedParam(q.toString(), "sp", pattern);
  }

  @SuppressWarnings("unchecked")
  public List<StopTimepointInterpolation> getStopTimeInterpolationByServicePatterns(
      final Collection<ServicePatternKey> ids) {

    if (ids.isEmpty())
      return new ArrayList<StopTimepointInterpolation>();

    return (List<StopTimepointInterpolation>) _t2.execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {

        StringBuilder query = new StringBuilder();

        query.append("SELECT sti FROM");
        query.append(" StopTimepointInterpolation sti");
        // query.append(" LEFT JOIN FETCH sti.stop");
        query.append(" WHERE ");
        query.append("  sti.servicePattern.id in (:ids)");

        Query q = session.createQuery(query.toString());

        q.setParameterList("ids", ids);

        return q.list();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public List<StopTimepointInterpolation> getStopTimeInterpolationByStops(
      final Collection<StopLocation> stops) {

    if (stops.isEmpty())
      return new ArrayList<StopTimepointInterpolation>();

    final Set<Integer> ids = new HashSet<Integer>();
    for (StopLocation stop : stops)
      ids.add(stop.getId());

    return (List<StopTimepointInterpolation>) _t2.execute(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {

        StringBuilder query = new StringBuilder();

        query.append("SELECT sti FROM");
        query.append(" StopTimepointInterpolation sti");
        query.append(" WHERE ");
        query.append("  sti.stop.id in (:ids)");

        Query q = session.createQuery(query.toString());

        q.setParameterList("ids", ids);

        return q.list();
      }
    });
  }

  public List<InterpolatedStopTime> getActiveInterpolatedStopTimesByStopAndTimeRange(
      int stopId, double target, double maxOffset) {
    return getInterpolatedStopTimesByChangeDateAndStopAndTimeRange(
        getCurrentServiceRevision(), stopId, target, maxOffset);
  }

  public List<T2<StopLocation, Integer>> getInterpolatedStopTimesFrequencyCountsByChangeDate(
      ChangeDate serviceRevision) {

    StringBuilder query = new StringBuilder();

    query.append("SELECT stop, count(st)");
    query.append(" FROM ");
    query.append(" StopLocation stop,");
    query.append(" StopTimepointInterpolation sti,");
    query.append(" StopTime st,");
    query.append(" WHERE");
    query.append("  stop = sti.stop");
    query.append("  AND sti.servicePattern.id.changeDate = ?");
    query.append("  AND sti.servicePattern = st.servicePattern");
    query.append("  AND sti.fromTimepoint = st.timepoint");
    query.append("  AND sti.fromTimepointSequence = st.stopTimePosition");
    query.append(" GROUP BY stop");

    List<T2<StopLocation, Integer>> rows = new ArrayList<T2<StopLocation, Integer>>();

    List<?> ro = _t2.find(query.toString(), serviceRevision);

    for (Object obj : ro) {
      Object[] row = (Object[]) obj;
      StopLocation stop = (StopLocation) row[0];
      Integer score = (Integer) row[1];
      rows.add(Tuple.create(stop, score));
    }

    return rows;
  }

  public List<Pair<Integer>> getTimepointPairs() {

    StringBuilder q = new StringBuilder();
    q.append("SELECT");
    q.append(" sti.fromTimepoint.id, sti.toTimepoint.id");
    q.append(" FROM");
    q.append(" StopTimepointInterpolation sti");
    q.append(" GROUP BY");
    q.append(" sti.fromTimepoint.id, sti.toTimepoint.id");

    List<?> rows = _t2.find(q.toString());
    List<Pair<Integer>> pairs = new ArrayList<Pair<Integer>>(rows.size());

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      Integer from = (Integer) elements[0];
      Integer to = (Integer) elements[1];
      pairs.add(Pair.createPair(from, to));
    }

    return pairs;
  }

  /*****************************************************************************
   * Regions
   ****************************************************************************/

  @SuppressWarnings("unchecked")
  public List<Region> getRegionsByLocation(Geometry location) {
    MySQLGeometryUserType type = new MySQLGeometryUserType();
    Object obj = type.conv2DBGeometry(location, null);
    return _t2.find("from Region r where contains(r.boundary,?) = 1", obj);
  }

  public Map<StopLocation, SortedMap<Layer, Region>> getStopsAndRegionsByServicePattern(
      ServicePattern pattern) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT stopRegion.stop, stopRegion.region FROM");
    query.append("  StopRegion stopRegion, ");
    query.append("  OrderedPatternStops ops,");
    query.append("  ServicePattern sp");
    query.append(" LEFT JOIN FETCH stopRegion.stop.mainStreetName");
    query.append(" LEFT JOIN FETCH stopRegion.stop.crossStreetName");
    query.append(" WHERE");
    query.append("  sp = :sp");
    query.append("  AND ops.route = sp.route");
    query.append("  AND ops.schedulePatternId = sp.schedulePatternId");
    query.append("  AND ops.stop = stopRegion.stop");
    query.append(" ORDER BY ops.sequence");

    List<?> rows = _t2.findByNamedParam(query.toString(), "sp", pattern);

    Map<StopLocation, SortedMap<Layer, Region>> regions = new LinkedHashMap<StopLocation, SortedMap<Layer, Region>>();

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      StopLocation stop = (StopLocation) elements[0];
      Region region = (Region) elements[1];
      SortedMap<Layer, Region> regionsByLayer = regions.get(stop);
      if (regionsByLayer == null) {
        regionsByLayer = new TreeMap<Layer, Region>();
        regions.put(stop, regionsByLayer);
      }
      regionsByLayer.put(region.getLayer(), region);
    }

    return regions;
  }

  /*****************************************************************************
   * Bookmarks
   ****************************************************************************/

  public LocationBookmarks getBookmarksByUserId(String id) {

    LocationBookmarks bookmarks = (LocationBookmarks) _t2.get(
        LocationBookmarks.class, id);
    if (bookmarks == null) {
      bookmarks = new LocationBookmarks();
      bookmarks.setId(id);
      _t2.save(bookmarks);
    }
    return bookmarks;
  }

  private final class PointToGeopointAdapter implements
      IAdapter<Point, IGeoPoint> {
    public IGeoPoint adapt(Point source) {
      return getLocationAsGeoPoint(source.getX(), source.getY());
    }
  }

}
