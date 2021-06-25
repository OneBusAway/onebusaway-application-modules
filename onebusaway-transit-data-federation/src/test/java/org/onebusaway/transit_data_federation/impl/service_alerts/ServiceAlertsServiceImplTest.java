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

package org.onebusaway.transit_data_federation.impl.service_alerts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.routeCollection;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.impl.ServiceAlertTimeRange;
import org.onebusaway.alerts.impl.ServiceAlertsCache;
import org.onebusaway.alerts.impl.ServiceAlertsCacheInMemoryImpl;
import org.onebusaway.alerts.impl.ServiceAlertsPersistenceDB;
import org.onebusaway.alerts.impl.ServiceAlertsServiceImpl;
import org.onebusaway.alerts.impl.ServiceAlertsSituationAffectsClause;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:service-alerts-data-sources.xml", 
    "classpath:org/onebusaway/transit_data_federation/application-context-services.xml"})
@Transactional(transactionManager = "transactionManager")
public class ServiceAlertsServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {


    @Autowired
    private ServiceAlertsService _service;
    private ServiceAlertsPersistenceDB _persister;
    private SessionFactory _sessionFactory;


    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    @Before
    public void setup() throws IOException {
        ServiceAlertsCache cache = new ServiceAlertsCacheInMemoryImpl();
        _service = new ServiceAlertsServiceImpl();
        ((ServiceAlertsServiceImpl)_service).setServiceAlertsCache(cache);
        _persister = new ServiceAlertsPersistenceDB();
        ((ServiceAlertsServiceImpl)_service).setServiceAlertsPersistence(_persister);
        _persister.setRefreshInterval(1000);
        _persister.setSessionFactory(_sessionFactory);

    }

  @Test
  public void testCreateServiceAlert() {
    ServiceAlertRecord alert = new ServiceAlertRecord();
    alert.setAgencyId("1");
    alert.setServiceAlertId("A");
    _service.createOrUpdateServiceAlert(alert);
    _service.getServiceAlertForId(AgencyAndIdLibrary.convertFromString("1_A"));
    assertTrue(alert.getCreationTime() > 0);
    assertTrue(alert.getId() > -1);
    assertEquals("1", alert.getAgencyId());
  }

  @Test
  public void testGetAllServiceAlerts() {
    ServiceAlertRecord alert1 = new ServiceAlertRecord();
    alert1.setAgencyId("1");
    alert1.setServiceAlertId("A");
    _service.createOrUpdateServiceAlert(alert1);

    ServiceAlertRecord alert2 = new ServiceAlertRecord();
    alert2.setAgencyId("1");
    alert2.setServiceAlertId("B");
    _service.createOrUpdateServiceAlert(alert2);


    List<ServiceAlertRecord> alerts = _service.getAllServiceAlerts();
    assertEquals(2, alerts.size());
    assertTrue(alerts.contains(alert1));
    assertTrue(alerts.contains(alert2));
  }

  private ServiceAlertTimeRange createTimeRange(long start, long end) {
    ServiceAlertTimeRange timeRange = new ServiceAlertTimeRange();
    if (start != 0)
        timeRange.setFromValue(start);
    if (end != 0)
      timeRange.setToValue(end);
    return timeRange;
  }

  private ServiceAlertRecord addServiceAlertWithTimeRange(String agencyId,
                                                          ServiceAlertTimeRange timeRange) {
      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId(agencyId);
      alert1.setServiceAlertId(UUID.randomUUID().toString());
      alert1.setPublicationWindows(new HashSet<ServiceAlertTimeRange>());
      alert1.setAllAffects(new HashSet<ServiceAlertsSituationAffectsClause>());
      alert1.getPublicationWindows().add(timeRange);
      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId(agencyId);
      alert1.getAllAffects().add(affectsClause);
      _service.createOrUpdateServiceAlert(alert1);
      return alert1;
  }

  @Test
  public void testGetServiceAlertsForFederatedAgencyId() {
      ServiceAlertRecord alert = new ServiceAlertRecord();
      alert.setAgencyId("1");
      alert.setServiceAlertId("A");
      _service.createOrUpdateServiceAlert(alert);

    List<ServiceAlertRecord> alerts = _service.getServiceAlertsForFederatedAgencyId("1");
    assertEquals(1, alerts.size());
    assertTrue(alerts.contains(alert));

    alerts = _service.getServiceAlertsForFederatedAgencyId("2");
    assertEquals(0, alerts.size());
  }

  @Test
  public void testGetServiceAlertForId() {
      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");
      _service.createOrUpdateServiceAlert(alert1);

      ServiceAlertRecord alert2 = new ServiceAlertRecord();
      alert2.setAgencyId("1");
      alert2.setServiceAlertId("B");
      _service.createOrUpdateServiceAlert(alert2);

      ServiceAlertRecord alert = _service.getServiceAlertForId(AgencyAndId.convertFromString("1_A"));
    assertSame(alert1, alert);

      alert = _service.getServiceAlertForId(AgencyAndId.convertFromString("1_B"));
    assertSame(alert2, alert);

    alert = _service.getServiceAlertForId(new AgencyAndId("1", "dne"));
    assertNull(alert);
  }



  @Test
  public void testGetServiceAlertsForAgencyId() {

      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("2");
      alert1.getAllAffects().add(affectsClause);

      ServiceAlertRecord serviceAlert = _service.createOrUpdateServiceAlert(alert1);

    List<ServiceAlertRecord> alerts = _service.getServiceAlertsForAgencyId(
        System.currentTimeMillis(), "1");
    assertEquals(0, alerts.size());

    alerts = _service.getServiceAlertsForAgencyId(System.currentTimeMillis(),
        "2");
    assertEquals(1, alerts.size());
    assertTrue(alerts.contains(serviceAlert));
  }

  @Test
  public void testGetServiceAlertsForStopCall() {

    /**
     * These alerts should match
     */

      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("1");
      affectsClause.setStopId("10020");
      affectsClause.setTripId("TripA");
      alert1.getAllAffects().add(affectsClause);
      alert1 = _service.createOrUpdateServiceAlert(alert1);

      ServiceAlertRecord alert2 = new ServiceAlertRecord();
      alert2.setAgencyId("1");
      alert2.setServiceAlertId("B");

      ServiceAlertsSituationAffectsClause affectsClause2 = new ServiceAlertsSituationAffectsClause();
      affectsClause2.setAgencyId("1");
      affectsClause2.setTripId("TripA");
      alert2.getAllAffects().add(affectsClause2);
      alert2 = _service.createOrUpdateServiceAlert(alert2);

      ServiceAlertRecord alert3 = new ServiceAlertRecord();
      alert3.setAgencyId("1");
      alert3.setServiceAlertId("C");

      ServiceAlertsSituationAffectsClause affectsClause3 = new ServiceAlertsSituationAffectsClause();
      affectsClause3.setAgencyId("1");
      affectsClause3.setRouteId("RouteX");
      alert3.getAllAffects().add(affectsClause3);
      alert3 = _service.createOrUpdateServiceAlert(alert3);

      ServiceAlertRecord alert4 = new ServiceAlertRecord();
      alert4.setAgencyId("1");
      alert4.setServiceAlertId("D");

      ServiceAlertsSituationAffectsClause affectsClause4 = new ServiceAlertsSituationAffectsClause();
      affectsClause4.setAgencyId("1");
      affectsClause4.setRouteId("RouteX");
      affectsClause4.setDirectionId("1");
      alert4.getAllAffects().add(affectsClause4);
      alert4 = _service.createOrUpdateServiceAlert(alert4);

      /**
       * These alerts shouldn't match
       */

      ServiceAlertRecord alert5 = new ServiceAlertRecord();
      alert5.setAgencyId("1");
      alert5.setServiceAlertId("E");

      ServiceAlertsSituationAffectsClause affectsClause5 = new ServiceAlertsSituationAffectsClause();
      affectsClause5.setAgencyId("1");
      affectsClause5.setStopId("10021");
      affectsClause5.setTripId("TripA");
      alert5.getAllAffects().add(affectsClause5);
      alert5 = _service.createOrUpdateServiceAlert(alert5);

      ServiceAlertRecord alert6 = new ServiceAlertRecord();
      alert6.setAgencyId("1");
      alert6.setServiceAlertId("F");

      ServiceAlertsSituationAffectsClause affectsClause6 = new ServiceAlertsSituationAffectsClause();
      affectsClause6.setAgencyId("1");
      affectsClause6.setStopId("10020");
      affectsClause6.setTripId("TripB");
      alert6.getAllAffects().add(affectsClause6);
      alert6 = _service.createOrUpdateServiceAlert(alert6);


      ServiceAlertRecord alert7 = new ServiceAlertRecord();
      alert7.setAgencyId("1");
      alert7.setServiceAlertId("G");

      ServiceAlertsSituationAffectsClause affectsClause7 = new ServiceAlertsSituationAffectsClause();
      affectsClause7.setAgencyId("1");
      affectsClause7.setTripId("TripB");
      alert7.getAllAffects().add(affectsClause7);
      alert7 = _service.createOrUpdateServiceAlert(alert7);

      ServiceAlertRecord alert8 = new ServiceAlertRecord();
      alert8.setAgencyId("1");
      alert8.setServiceAlertId("H");

      ServiceAlertsSituationAffectsClause affectsClause8 = new ServiceAlertsSituationAffectsClause();
      affectsClause8.setAgencyId("1");
      affectsClause8.setRouteId("RouteY");
      alert8.getAllAffects().add(affectsClause8);
      alert8 = _service.createOrUpdateServiceAlert(alert8);


      ServiceAlertRecord alert9 = new ServiceAlertRecord();
      alert9.setAgencyId("1");
      alert9.setServiceAlertId("I");

      ServiceAlertsSituationAffectsClause affectsClause9 = new ServiceAlertsSituationAffectsClause();
      affectsClause9.setAgencyId("1");
      affectsClause9.setRouteId("RouteX");
      affectsClause9.setDirectionId("0");
      alert9.getAllAffects().add(affectsClause9);
      alert9 = _service.createOrUpdateServiceAlert(alert9);

      RouteEntryImpl route = route("RouteX");
      routeCollection("RouteX", route);
      StopEntryImpl stop = stop("10020", 47.0, -122.0);
      TripEntryImpl trip = trip("TripA");
      trip.setRoute(route);
      trip.setDirectionId("1");
      stopTime(0, stop, trip, time(8, 53), 0);
      BlockEntryImpl block = block("block");
      BlockConfigurationEntry blockConfig = blockConfiguration(block,
              serviceIds(lsids("a"), lsids()), trip);

      BlockInstance blockInstance = new BlockInstance(blockConfig,
              System.currentTimeMillis());

      // TODO pull this out to ServiceAlertsBeanTest
//      List<ServiceAlertRecord> alerts = _service.getServiceAlertsForStopCall(
//              System.currentTimeMillis(), blockInstance,
//              blockConfig.getStopTimes().get(0), new AgencyAndId("1", "1111"));
//      assertEquals(4, alerts.size());
//      assertTrue(alerts.contains(alert1));
//      assertTrue(alerts.contains(alert2));
//      assertTrue(alerts.contains(alert3));
//      assertTrue(alerts.contains(alert4));
  }

  @Test
  public void testGetServiceAlertsForStopId() {

      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("1");
      affectsClause.setStopId("10020");
      alert1.getAllAffects().add(affectsClause);
      alert1 = _service.createOrUpdateServiceAlert(alert1);


    List<ServiceAlertRecord> alerts = _service.getServiceAlertsForStopId(
        System.currentTimeMillis(), new AgencyAndId("1", "10020"));
    assertEquals(1, alerts.size());
    assertTrue(alerts.contains(alert1));

    alerts = _service.getServiceAlertsForStopId(System.currentTimeMillis(),
        new AgencyAndId("1", "10021"));
    assertEquals(0, alerts.size());
  }

    @Test
    public void testGetServiceAlertsForVehicleJourney() {

        /**
         * These alerts should match
         */


        ServiceAlertRecord alert2 = new ServiceAlertRecord();
        alert2.setAgencyId("1");
        alert2.setServiceAlertId("B");

        ServiceAlertsSituationAffectsClause affectsClause2 = new ServiceAlertsSituationAffectsClause();
        affectsClause2.setAgencyId("1");
        affectsClause2.setTripId("TripA");
        alert2.getAllAffects().add(affectsClause2);
        alert2 = _service.createOrUpdateServiceAlert(alert2);

        ServiceAlertRecord alert3 = new ServiceAlertRecord();
        alert3.setAgencyId("1");
        alert3.setServiceAlertId("C");

        ServiceAlertsSituationAffectsClause affectsClause3 = new ServiceAlertsSituationAffectsClause();
        affectsClause3.setAgencyId("1");
        affectsClause3.setRouteId("RouteX");
        alert3.getAllAffects().add(affectsClause3);
        alert3 = _service.createOrUpdateServiceAlert(alert3);

        ServiceAlertRecord alert4 = new ServiceAlertRecord();
        alert4.setAgencyId("1");
        alert4.setServiceAlertId("D");

        ServiceAlertsSituationAffectsClause affectsClause4 = new ServiceAlertsSituationAffectsClause();
        affectsClause4.setAgencyId("1");
        affectsClause4.setRouteId("RouteX");
        affectsClause4.setDirectionId("1");
        alert4.getAllAffects().add(affectsClause4);
        alert4 = _service.createOrUpdateServiceAlert(alert4);


        /**
         * These alerts shouldn't match
         */

        ServiceAlertRecord alert5 = new ServiceAlertRecord();
        alert5.setAgencyId("1");
        alert5.setServiceAlertId("E");

        ServiceAlertsSituationAffectsClause affectsClause5 = new ServiceAlertsSituationAffectsClause();
        affectsClause5.setAgencyId("1");
        affectsClause5.setStopId("10021");
        affectsClause5.setTripId("TripA");
        alert5.getAllAffects().add(affectsClause5);
        alert5 = _service.createOrUpdateServiceAlert(alert5);

        ServiceAlertRecord alert6 = new ServiceAlertRecord();
        alert6.setAgencyId("1");
        alert6.setServiceAlertId("F");

        ServiceAlertsSituationAffectsClause affectsClause6 = new ServiceAlertsSituationAffectsClause();
        affectsClause6.setAgencyId("1");
        affectsClause6.setStopId("10020");
        affectsClause6.setTripId("TripB");
        alert6.getAllAffects().add(affectsClause6);
        alert6 = _service.createOrUpdateServiceAlert(alert6);


        ServiceAlertRecord alert7 = new ServiceAlertRecord();
        alert7.setAgencyId("1");
        alert7.setServiceAlertId("G");

        ServiceAlertsSituationAffectsClause affectsClause7 = new ServiceAlertsSituationAffectsClause();
        affectsClause7.setAgencyId("1");
        affectsClause7.setTripId("TripB");
        alert7.getAllAffects().add(affectsClause7);
        alert7 = _service.createOrUpdateServiceAlert(alert7);

        ServiceAlertRecord alert8 = new ServiceAlertRecord();
        alert8.setAgencyId("1");
        alert8.setServiceAlertId("H");

        ServiceAlertsSituationAffectsClause affectsClause8 = new ServiceAlertsSituationAffectsClause();
        affectsClause8.setAgencyId("1");
        affectsClause8.setRouteId("RouteY");
        alert8.getAllAffects().add(affectsClause8);
        alert8 = _service.createOrUpdateServiceAlert(alert8);


        ServiceAlertRecord alert9 = new ServiceAlertRecord();
        alert9.setAgencyId("1");
        alert9.setServiceAlertId("I");

        ServiceAlertsSituationAffectsClause affectsClause9 = new ServiceAlertsSituationAffectsClause();
        affectsClause9.setAgencyId("1");
        affectsClause9.setRouteId("RouteX");
        affectsClause9.setDirectionId("0");
        alert9.getAllAffects().add(affectsClause9);
        alert9 = _service.createOrUpdateServiceAlert(alert9);

        RouteEntryImpl route = route("RouteX");
        routeCollection("RouteX", route);
        StopEntryImpl stop = stop("10020", 47.0, -122.0);
        TripEntryImpl trip = trip("TripA");
        trip.setRoute(route);
        trip.setDirectionId("1");
        stopTime(0, stop, trip, time(8, 53), 0);
        BlockEntryImpl block = block("block");
        BlockConfigurationEntry blockConfig = blockConfiguration(block,
                serviceIds(lsids("a"), lsids()), trip);

        BlockTripInstance blockTripInstance = new BlockTripInstance(
                blockConfig.getTrips().get(0), new InstanceState(
                System.currentTimeMillis()));

        // TODO pull this out to ServiceAlertsBeanServiceTest
//        List<ServiceAlertRecord> alerts = _service.getServiceAlertsForVehicleJourney(
//                System.currentTimeMillis(), blockTripInstance, new AgencyAndId("1",
//                        "1111"));
//        assertEquals(3, alerts.size());
//        assertTrue(alerts.contains(alert2));
//        assertTrue(alerts.contains(alert3));
//        assertTrue(alerts.contains(alert4));
    }


  @Test
  public void testRemoveServiceAlertsForFederatedAgencyId() {

    ServiceAlertRecord alert1 = new ServiceAlertRecord();
    alert1.setAgencyId("1");
    alert1.setServiceAlertId("A");
    _service.createOrUpdateServiceAlert(alert1);

      ServiceAlertRecord alert2 = new ServiceAlertRecord();
      alert2.setAgencyId("1");
      alert2.setServiceAlertId("B");
      _service.createOrUpdateServiceAlert(alert2);


    _service.removeAllServiceAlertsForFederatedAgencyId("2");

    assertEquals(2, _service.getAllServiceAlerts().size());
    assertEquals(2, _service.getServiceAlertsForFederatedAgencyId("1").size());

    _service.removeAllServiceAlertsForFederatedAgencyId("1");

    assertEquals(0, _service.getAllServiceAlerts().size());
    assertEquals(0, _service.getServiceAlertsForFederatedAgencyId("1").size());
  }

  @Test
  public void testRemoveServiceAlert() {

      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");
      _service.createOrUpdateServiceAlert(alert1);

    _service.removeServiceAlert(AgencyAndId.convertFromString("1_A"));

    assertNull(_service.getServiceAlertForId(AgencyAndId.convertFromString("1_A")));
  }

  @Test
  public void testUpdateServiceAlert() {

      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("2");
      alert1.getAllAffects().add(affectsClause);
      alert1 = _service.createOrUpdateServiceAlert(alert1);

      alert1.setAllAffects(new HashSet<ServiceAlertsSituationAffectsClause>());
      affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("1");
      affectsClause.setStopId("10020");
      alert1.getAllAffects().add(affectsClause);
      alert1 = _service.createOrUpdateServiceAlert(alert1);


      List<ServiceAlertRecord> alerts = _service.getServiceAlertsForAgencyId(
              System.currentTimeMillis(), "2");

      assertEquals(0, alerts.size());

      alerts = _service.getServiceAlertsForStopId(
              System.currentTimeMillis(), AgencyAndId.convertFromString("1_10020"));
      assertEquals(1, alerts.size());
      assertTrue(alerts.contains(alert1));
  }

  @Test
  public void testRefresh() {
    long now = System.currentTimeMillis();

    // ensure we have no records;
    List<ServiceAlertRecord> alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());
    ServiceAlertRecord alert1 = new ServiceAlertRecord();
    alert1.setAgencyId("1");
    alert1.setServiceAlertId("A");

    // we need to set an effects clause for the agency index to triggeer
    ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
    affectsClause.setAgencyId("1");
    alert1.getAllAffects().add(affectsClause);

    _persister.saveOrUpdate(alert1);

    alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());
    
    // wait for the database to refresh
    try {
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    // see the update
    alerts = _service.getAllServiceAlerts();
    assertEquals(1, alerts.size());
    // ensure the sub-index has it as well
    alerts = _service.getServiceAlertsForAgencyId(now, alert1.getAgencyId());
    assertEquals(1, alerts.size());
  }

  @Test
  public void testAsyncDBUpdate() {
    
    // ensure we have no records;
    List<ServiceAlertRecord> alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());


      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("1");
      affectsClause.setStopId("10020");
      affectsClause.setTripId("TripA");
      alert1.getAllAffects().add(affectsClause);
    _persister.saveOrUpdate(alert1);

    // service should not have seen it
    alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());

    // wait for the database to refresh
    try {
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // see the update
    alerts = _service.getAllServiceAlerts();
    assertEquals(1, alerts.size());

  }

  @Test
  public void testAsyncDBDelete() {
    
    // ensure we have no records;
    List<ServiceAlertRecord> alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());


      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");
      _persister.saveOrUpdate(alert1);


    // wait for the database to refresh
    try {
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // see the update
    alerts = _service.getAllServiceAlerts();
    assertEquals(1, alerts.size());

    // delete that record
    _persister.delete(alert1);
    // service should not have seen it yet
    alerts = _service.getAllServiceAlerts();
    assertEquals(1, alerts.size());

    // wait for the database to refresh
    try {
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // now we see the update (a deletion)
    alerts = _service.getAllServiceAlerts();
    assertEquals(0, alerts.size());
  }

  @Test
  public void testAsyncPublicationWindow() {
      Long now = System.currentTimeMillis();
      // ensure we have no records;
      List<ServiceAlertRecord> alerts = _service.getAllServiceAlerts();
      assertEquals(0, alerts.size());

      // and for agency index
      alerts = _service.getServiceAlertsForAgencyId(now, "1");
      assertEquals(0, alerts.size());

      // and sto index
      alerts = _service.getServiceAlertsForStopId(
              now, AgencyAndId.convertFromString("1_10020"));
      assertEquals(0, alerts.size());

      // now create a service alert
      ServiceAlertRecord alert1 = new ServiceAlertRecord();
      alert1.setAgencyId("1");
      alert1.setServiceAlertId("A");

      // affects mandatory for update to work
      ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
      affectsClause.setAgencyId("1");
      affectsClause.setStopId("10020");
      alert1.getAllAffects().add(affectsClause);

      // test a publication window of a few seconds around now
      ServiceAlertTimeRange range = new ServiceAlertTimeRange();

      range.setFromValue(now - 1000);
      range.setToValue(now + 2 * 1000);
      alert1.setPublicationWindows(new HashSet<ServiceAlertTimeRange>());
      alert1.getPublicationWindows().add(range);
      _persister.saveOrUpdate(alert1);

      _service.loadServiceAlerts();

      // see the update
      alerts = _service.getAllServiceAlerts();
      assertEquals(1, alerts.size());

      // agency will not trigger as we have a stopid instead
      alerts = _service.getServiceAlertsForAgencyId(now, alert1.getAgencyId());
      assertEquals(0, alerts.size());

      alerts = _service.getServiceAlertsForStopId(
              now, AgencyAndId.convertFromString("1_10020"));
      assertEquals(1, alerts.size());

      // all service alerts bypasses filtering by time
      alerts = _service.getAllServiceAlerts();
      assertEquals(1, alerts.size());

      // still not there
      alerts = _service.getServiceAlertsForAgencyId(now + 3 * 1000, alert1.getAgencyId());
      assertEquals(0, alerts.size());

      // window expired
      alerts = _service.getServiceAlertsForStopId(
              now + 3 * 1000, AgencyAndId.convertFromString("1_10020"));
      assertEquals(0, alerts.size());

  }


    @Test
    public void testPublicationWindowServiceAlert() {
        Long now = System.currentTimeMillis();
        ServiceAlertRecord alert1 = new ServiceAlertRecord();
        alert1.setAgencyId("1");
        alert1.setServiceAlertId("A");

        ServiceAlertsSituationAffectsClause affectsClause = new ServiceAlertsSituationAffectsClause();
        affectsClause.setAgencyId("1");
        alert1.getAllAffects().add(affectsClause);
        alert1.getPublicationWindows().add(createTimeRange(now - 60 * 1000, now + 60 * 1000));

        alert1 = _service.createOrUpdateServiceAlert(alert1);

        List<ServiceAlertRecord> alerts = _service.getServiceAlertsForAgencyId(
                now, "1");
        assertEquals(1, alerts.size());

        alerts = _service.getServiceAlertsForStopId(
                now, AgencyAndId.convertFromString("1_10020"));
        assertEquals(1, alerts.size());
        assertTrue(alerts.contains(alert1));

        // too early
        alerts = _service.getServiceAlertsForAgencyId(
                now-61*1000, "1");
        alerts = _service.getServiceAlertsForStopId(
                now-61*1000, AgencyAndId.convertFromString("1_10020"));
        assertEquals(0, alerts.size());

        // too late
        alerts = _service.getServiceAlertsForAgencyId(
                now+61*1000, "1");
        alerts = _service.getServiceAlertsForStopId(
                now+61*1000, AgencyAndId.convertFromString("1_10020"));
        assertEquals(0, alerts.size());

        // re-assure ourselves we still get the correct answer
        alerts = _service.getServiceAlertsForAgencyId(
                now, "1");
        alerts = _service.getServiceAlertsForStopId(
                now, AgencyAndId.convertFromString("1_10020"));
        assertEquals(1, alerts.size());
        assertTrue(alerts.contains(alert1));

    }
}
