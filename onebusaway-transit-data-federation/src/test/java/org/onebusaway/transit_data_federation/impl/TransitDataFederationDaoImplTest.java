/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.gtfs.impl.HibernateGtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class})
@ContextConfiguration(locations = {
    "classpath:org/onebusaway/transit_data_federation/application-context-common.xml",
    "classpath:org/onebusaway/transit_data_federation/impl/TransitDataFederationDaoImplTest.xml"})
public class TransitDataFederationDaoImplTest {

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
