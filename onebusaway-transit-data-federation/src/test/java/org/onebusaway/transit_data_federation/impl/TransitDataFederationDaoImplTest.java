package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.testing.OneBusAwayBaseTest;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {
    "classpath:org/onebusaway/transit_data_federation/application-context-common.xml",
    "classpath:test-data-sources.xml"})
public class TransitDataFederationDaoImplTest extends OneBusAwayBaseTest {

  private SessionFactory _sessionFactory;

  @Autowired
  public void setSessionFactory(SessionFactory sessionFactory) {
    _sessionFactory = sessionFactory;
  }

  @Test
  public void testGetRouteCollectionsForRoute() {

    HibernateGtfsRelationalDaoImpl gtfsDao = new HibernateGtfsRelationalDaoImpl();
    gtfsDao.setSessionFactory(_sessionFactory);

    TransitDataFederationMutableDaoImpl dao = new TransitDataFederationMutableDaoImpl();
    dao.setSessionFactory(_sessionFactory);

    Agency agency = new Agency();
    agency.setId("agency");
    agency.setName("Agency");
    gtfsDao.saveEntity(agency);

    Route route = new Route();
    route.setAgency(agency);
    route.setId(new AgencyAndId("agency", "1"));
    route.setLongName("Route One");
    route.setShortName("1");
    route.setType(3);
    gtfsDao.saveEntity(route);

    RouteCollection rc = new RouteCollection();
    rc.setId(new AgencyAndId("agency", "1"));
    rc.setLongName("Route 1");
    rc.setShortName("1");
    rc.setType(3);
    rc.setRoutes(Arrays.asList(route));
    dao.save(rc);

    RouteCollection rc2 = dao.getRouteCollectionForRoute(route);
    assertEquals(rc.getId(), rc2.getId());
  }
}
