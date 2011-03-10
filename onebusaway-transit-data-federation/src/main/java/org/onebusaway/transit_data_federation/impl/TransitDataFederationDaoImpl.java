package org.onebusaway.transit_data_federation.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
class TransitDataFederationDaoImpl implements TransitDataFederationDao {

  private final Logger _log = LoggerFactory.getLogger(TransitDataFederationDaoImpl.class);

  protected HibernateTemplate _template;

  private ExtendedGtfsRelationalDao _gtfsDao;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _template = new HibernateTemplate(sessionFactory);
    _template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
  }

  @Autowired
  public void setGtfsDao(ExtendedGtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RouteCollection> getAllRouteCollections() {
    return _template.find("from RouteCollection");
  }

  @Override
  public RouteCollection getRouteCollectionForId(final AgencyAndId id) {
    return (RouteCollection) _template.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Object object = session.get(RouteCollection.class, id);
        if (object != null)
          session.setReadOnly(object, true);
        return object;
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<RouteCollection> getRouteCollectionsForStop(Stop stop) {
    List<Route> routes = _gtfsDao.getRoutesForStop(stop);
    if (routes.isEmpty())
      return new ArrayList<RouteCollection>();
    return _template.findByNamedQueryAndNamedParam("routeCollectionsForRoutes",
        "routes", routes);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<AgencyAndId> getRouteCollectionIdsForServiceId(
      AgencyAndId serviceId) {
    return _template.findByNamedQueryAndNamedParam(
        "routeCollectionsForServiceId", "serviceId", serviceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<AgencyAndId> getTripIdsForServiceIdAndRouteCollectionId(
      AgencyAndId serviceId, AgencyAndId routeCollectionId) {
    String[] names = {"serviceId", "routeId"};
    Object[] values = {serviceId, routeCollectionId};
    return _template.findByNamedQueryAndNamedParam(
        "tripsForServiceIdAndRouteId", names, values);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RouteCollection getRouteCollectionForRoute(Route route) {
    List<Route> routes = Arrays.asList(route);
    List<RouteCollection> routeCollections = _template.findByNamedQueryAndNamedParam(
        "routeCollectionsForRoutes", "routes", routes);
    if (routeCollections.isEmpty())
      return null;
    if (routeCollections.size() > 1)
      _log.warn("multiple route collections for route id=" + route.getId());
    return routeCollections.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AgencyAndId> getShapeIdsForStop(Stop stop) {
    return _template.findByNamedQueryAndNamedParam("shapeIdsForStop", "stop",
        stop);
  }
}
