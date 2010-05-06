package org.onebusaway.where.impl;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import com.vividsolutions.jts.geom.Point;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernatespatial.mysql.MySQLGeometryUserType;
import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.where.model.LocationBookmarks;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.Timepoint;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
class WhereDaoImpl implements WhereDao {

  private HibernateTemplate _template;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
  }

  public void save(Object object) {
    _template.save(object);
  }

  public void saveOrUpdate(Object object) {
    _template.saveOrUpdate(object);
  }

  public void update(Object object) {
    _template.update(object);
  }

  public <T> void saveOrUpdateAllEntities(List<T> updates) {
    _template.saveOrUpdateAll(updates);
  }

  @SuppressWarnings("unchecked")
  public List<Timepoint> getTimepointsByTripIds(final Set<String> tripIds) {

    if (tripIds.isEmpty())
      return new ArrayList<Timepoint>();

    return _template.executeFind(new HibernateCallback() {

      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        String hql = "SELECT tp FROM Timepoint tp WHERE tp.id.tripId IN (:tripIds)";
        Query query = session.createQuery(hql);
        query.setParameterList("tripIds", tripIds);
        return query.list();
      }
    });
  }

  public LocationBookmarks getBookmarksByUserId(String userId) {
    LocationBookmarks bookmarks = (LocationBookmarks) _template.get(
        LocationBookmarks.class, userId);
    if (bookmarks == null) {
      bookmarks = new LocationBookmarks();
      bookmarks.setId(userId);
      _template.save(bookmarks);
    }
    return bookmarks;
  }

  @SuppressWarnings("unchecked")
  public List<StopSequenceBlock> getStopSequenceBlocksByRoute(Route route) {
    return _template.findByNamedParam(
        "SELECT block FROM StopSequenceBlock block WHERE block.id.route = :route",
        "route", route);
  }

  @SuppressWarnings("unchecked")
  public List<StopSequenceBlock> getStopSequenceBlocksByStop(Stop stop) {
    return _template.findByNamedParam(
        "SELECT block FROM StopSequenceBlock block, StopSequence ss WHERE :stop in elements(ss.stops) AND ss in elements(block.stopSequences)",
        "stop", stop);
  }

  @SuppressWarnings("unchecked")
  public List<StopSequence> getStopSequencesByRoute(Route route) {
    return _template.findByNamedParam(
        "SELECT ss FROM StopSequence ss WHERE ss.route = :route", "route",
        route);
  }

  @SuppressWarnings("unchecked")
  public List<StopSequence> getStopSequencesByRouteAndDirectionId(Route route,
      String directionId) {
    String[] params = {"route", "directionId"};
    Object[] values = {route, directionId};
    return _template.findByNamedParam(
        "SELECT ss FROM StopSequence ss, Trip trip WHERE ss.route = :route AND trip IN elements(ss.trips) AND trip.directionId = :directionId GROUP BY ss",
        params, values);
  }

  @SuppressWarnings("unchecked")
  public List<Region> getRegionsByLocation(Point location) {
    MySQLGeometryUserType type = new MySQLGeometryUserType();
    Object obj = type.conv2DBGeometry(location, null);
    return _template.find("from Region r where contains(r.boundary,?) = 1", obj);
  }

  public Map<Stop, SortedMap<Layer, Region>> getRegionsByStops(List<Stop> stops) {

    if( stops.isEmpty() )
      return new HashMap<Stop, SortedMap<Layer,Region>>();
    
    Map<String, Stop> stopsById = CollectionsLibrary.mapToValue(stops, "id",
        String.class);

    StringBuilder query = new StringBuilder();
    query.append("SELECT stopRegion.stop.id, stopRegion.region FROM");
    query.append("  StopRegion stopRegion");
    query.append(" WHERE");
    query.append("  stopRegion.stop.id IN (:ids)");

    List<?> rows = _template.findByNamedParam(query.toString(), "ids",
        stopsById.keySet());

    Map<Stop, SortedMap<Layer, Region>> regions = new LinkedHashMap<Stop, SortedMap<Layer, Region>>();

    for (Object row : rows) {
      Object[] elements = (Object[]) row;
      String id = (String) elements[0];
      Stop stop = stopsById.get(id);
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

  @SuppressWarnings("unchecked")
  public SortedMap<Layer, Region> getRegionsByStop(Stop stop) {

    StringBuilder query = new StringBuilder();
    query.append("SELECT stopRegion.region FROM");
    query.append("  StopRegion stopRegion");
    query.append(" WHERE");
    query.append("  stopRegion.stop = :stop");
    List<Region> rows = _template.findByNamedParam(query.toString(), "stop",
        stop);
    SortedMap<Layer, Region> r = new TreeMap<Layer, Region>();
    for (Region region : rows)
      r.put(region.getLayer(), region);
    return r;
  }

}
