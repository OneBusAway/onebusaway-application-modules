/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.StopTripDirectionBean;
import org.onebusaway.transit_data_federation.impl.ExtendedCalendarServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexFactoryServiceImpl;
import org.onebusaway.transit_data_federation.impl.blocks.BlockIndexServiceImpl;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteCollectionEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

public class RouteScheduleBeanServiceImplTest {

  private static final String AGENCY_ID = "1"; // UnitTestingSupport assumes this
  private static final String ROUTE_ID = "TLINK";

  private NarrativeServiceImpl narrativeService = null;
  private BlockIndexServiceImpl blockIndexService = null;
  private ExtendedCalendarServiceImpl calendarService = null;
  private ServiceAlertsBeanServiceImpl serviceAlertsService = null;
  private TransitGraphDao dao = null;
  private BlockIndexFactoryServiceImpl factory = null;
  private BlockTripIndex blockIndexWeekday = null;
  private BlockTripIndex blockIndexSaturday = null;
  private BlockTripIndex blockIndexSunday = null;

  @Test
  public void runTestSuite() {
    this.testGetScheduledArrivalsForDateStopTrips();
  }

  public void testGetScheduledArrivalsForDateStopTrips() {
    RouteScheduleBeanServiceImpl impl = getImpl();

    RouteCollectionEntry route = createRoutes(AGENCY_ID, ROUTE_ID);
    assertNotNull(blockIndexWeekday.getTrips());
    assertEquals(2, blockIndexWeekday.getTrips().size());
    when(blockIndexService.getBlockTripIndicesForRouteCollectionId(new AgencyAndId(AGENCY_ID, ROUTE_ID)))
            .thenReturn(Arrays.asList(blockIndexWeekday, blockIndexSaturday, blockIndexSunday));

    TransitGraphDao graph = Mockito.mock(TransitGraphDao.class);
    CalendarServiceData data = new CalendarServiceData();
    data.putDatesForLocalizedServiceId(lsid("WD_TL"),
            Arrays.asList(date("2020-11-30 00:00"), date("2020-12-04 00:00")));
    data.putDatesForLocalizedServiceId(lsid("SA"),
            Arrays.asList(date("2009-12-05 00:00")));
    data.putDatesForLocalizedServiceId(lsid("SU"),
            Arrays.asList(date("2009-12-06 00:00")));

    CalendarServiceImpl simpleCalendarService = new CalendarServiceImpl();
    simpleCalendarService.setData(data);

    this.calendarService = new ExtendedCalendarServiceImpl();
    this.calendarService.setCalendarService(simpleCalendarService);
    this.calendarService.setTransitGraphDao(dao);
    this.calendarService.start();
    impl.setCalendarService(this.calendarService);

    ServiceDate serviceDate = new ServiceDate(2020, 11, 30);
    RouteScheduleBean bean = impl.getScheduledArrivalsForDate(new AgencyAndId(AGENCY_ID, ROUTE_ID),
            serviceDate);
    assertNotNull(bean);
    assertEquals(route.getId(), bean.getRouteId());
    assertEquals(Arrays.asList(aId("WD_TL")), bean.getServiceIds());
    assertEquals(serviceDate, bean.getScheduleDate());

    assertEquals(2, bean.getStopTripDirections().size());
    StopTripDirectionBean stdb1 = getStopTripDirection(bean.getStopTripDirections(), "Theater District Station", "0");
    assertNotNull(stdb1);

    assertEquals(Arrays.asList(aId( "TacL1000")), stdb1.getTripIds());
    assertEquals(Arrays.asList(aId("TL_TD"), aId("TL_25"), aId("TL_US"), aId("TL_CC")),
            stdb1.getStopIds());
    assertNotNull(stdb1.getStopTimes());
    assertEquals(4, stdb1.getStopTimes().size());
    assertNotNull(stdb1.getStopTimes().get(0).getTripId());
    assertEquals("1_TacL1000", stdb1.getStopTimes().get(0).getTripId());
    assertEquals(Arrays.asList((long)time(5,0,0),
            (long)time(5, 2, 0),
            (long)time(5, 4, 0),
            (long)time(5, 6, 0)),
            Arrays.asList(stdb1.getStopTimes().get(0).getArrivalTime(),
                    stdb1.getStopTimes().get(1).getArrivalTime(),
                    stdb1.getStopTimes().get(2).getArrivalTime(),
                    stdb1.getStopTimes().get(3).getArrivalTime()));

    StopTripDirectionBean stdb2 = getStopTripDirection(bean.getStopTripDirections(), "Tacoma Dome Station", "1");
    assertNotNull(stdb2);

    assertEquals(Arrays.asList(aId("TacL1001")), stdb2.getTripIds());
    assertEquals(Arrays.asList(aId("TL_CC"), aId("TL_US"), aId("TL_25"), aId("TL_TD")),
            stdb2.getStopIds());
    assertNotNull(stdb2.getStopTimes());
    assertEquals(4, stdb2.getStopTimes().size());
    assertNotNull(stdb2.getStopTimes().get(0).getTripId());
    assertEquals("1_TacL1001", stdb2.getStopTimes().get(0).getTripId());
    StopTimeInstanceBean stib2 = stdb2.getStopTimes().get(0);
    assertEquals(time(5, 16, 0), stib2.getArrivalTime());
    assertEquals(time(5, 16, 0), stib2.getDepartureTime());

    assertEquals(Arrays.asList((long)time(5,16,0),
            (long)time(5, 18, 0),
            (long)time(5, 20, 0),
            (long)time(5, 22, 0)),
            Arrays.asList(stdb2.getStopTimes().get(0).getArrivalTime(),
                    stdb2.getStopTimes().get(1).getArrivalTime(),
                    stdb2.getStopTimes().get(2).getArrivalTime(),
                    stdb2.getStopTimes().get(3).getArrivalTime()));

    // sanity check the references now
    assertEquals(1, bean.getAgencies().size());
    assertEquals(1, bean.getRoutes().size());
    assertEquals(4, bean.getStops().size());
    assertEquals(2, bean.getTrips().size());
    assertEquals(8, bean.getStopTimes().size());
  }

  private StopTripDirectionBean getStopTripDirection(List<StopTripDirectionBean> stopTripDirections, String headsign, String direction) {
    for (StopTripDirectionBean stdb: stopTripDirections) {
      if (stdb.getTripHeadsign().equals(headsign)
          && stdb.getDirectionId().equals(direction))
        return stdb;
    }
    return null;
  }

  private AgencyAndId aId(String id) {
    return new AgencyAndId(AGENCY_ID, id);
  }


  private RouteScheduleBeanServiceImpl getImpl() {
    RouteScheduleBeanServiceImpl impl = new RouteScheduleBeanServiceImpl();

    factory = new BlockIndexFactoryServiceImpl();

    narrativeService = Mockito.mock(NarrativeServiceImpl.class);
    impl.setNarrativeService(narrativeService);

    blockIndexService = Mockito.mock(BlockIndexServiceImpl.class);
    impl.setBlockIndexService(blockIndexService);

    // calendarService has dependencies and is set elsewhere

    dao = Mockito.mock(TransitGraphDao.class);
    impl.setTransitGraphDao(dao);

    serviceAlertsService = Mockito.mock(ServiceAlertsBeanServiceImpl.class);
    impl.setServiceAlertsBeanService(serviceAlertsService);

    return impl;
  }

  private RouteCollectionEntry createRoutes(String agencyId,
                                            String routeId) {

    RouteEntryImpl route  = route(ROUTE_ID);
    BlockEntryImpl b1 = block("TacL1000");
    TripEntryImpl t1 = trip("TacL1000", "WD_TL", 0);
    t1.setDirectionId("0");
    t1.setRoute(route);
    BlockEntryImpl b2 = block("TacL1001");
    TripEntryImpl t2 = trip("TacL1001", "WD_TL", 0);
    t2.setDirectionId("1");
    t2.setRoute(route);

    BlockEntryImpl b3 = block("TacL6000");
    TripEntryImpl t3 = trip("TacL6000", "SA", 0);
    t3.setDirectionId("0");
    t3.setRoute(route);

    BlockEntryImpl b4 = block("TacL6001");
    TripEntryImpl t4 = trip("TacL6001", "SA", 0);
    t4.setDirectionId("1");
    t4.setRoute(route);

    BlockEntryImpl b5 = block("TacL7000");
    TripEntryImpl t5 = trip("TacL7000", "SU", 0);
    t5.setDirectionId("1");
    t5.setRoute(route);

    BlockEntryImpl b6 = block("TacL7001");
    TripEntryImpl t6 = trip("TacL7001", "SU", 0);
    t6.setDirectionId("1");
    t6.setRoute(route);

    StopEntryImpl std = stop("TL_TD", 47.239868,-122.428118);
    StopEntryImpl s25 = stop("TL_25", 47.239081,-122.434202);
    StopEntryImpl sus = stop("TL_US", 47.244865,-122.436623);
    StopEntryImpl scc = stop("TL_CC", 47.249496,-122.438552);

    StopTimeEntryImpl st_1_1 = stopTime(1, std, t1, time(5, 0, 0), time(5, 0,0 ), 0);
    StopTimeEntryImpl st_1_2 = stopTime(1, s25, t1, time(5, 2, 0), time(5, 2,0 ), 0);
    StopTimeEntryImpl st_1_3 = stopTime(1, sus, t1, time(5, 4, 0), time(5, 4,0 ), 0);
    StopTimeEntryImpl st_1_4 = stopTime(1, scc, t1, time(5, 6, 0), time(5, 6,0 ), 0);

    StopTimeEntryImpl st_2_1 = stopTime(1, scc, t2, time(5, 16, 0), time(5, 16,0 ), 0);
    StopTimeEntryImpl st_2_2 = stopTime(1, sus, t2, time(5, 18, 0), time(5, 18,0 ), 0);
    StopTimeEntryImpl st_2_3 = stopTime(1, s25, t2, time(5, 20, 0), time(5, 20,0 ), 0);
    StopTimeEntryImpl st_2_4 = stopTime(1, std, t2, time(5, 22, 0), time(5, 22,0 ), 0);

    StopTimeEntryImpl st_3_1 = stopTime(1, std, t3, time(6, 0, 0), time(5, 0,0 ), 0);
    StopTimeEntryImpl st_3_2 = stopTime(1, s25, t3, time(6, 2, 0), time(5, 2,0 ), 0);
    StopTimeEntryImpl st_3_3 = stopTime(1, sus, t3, time(6, 4, 0), time(5, 4,0 ), 0);
    StopTimeEntryImpl st_3_4 = stopTime(1, scc, t3, time(6, 6, 0), time(5, 6,0 ), 0);

    StopTimeEntryImpl st_4_1 = stopTime(1, scc, t4, time(6, 16, 0), time(5, 16,0 ), 0);
    StopTimeEntryImpl st_4_2 = stopTime(1, sus, t4, time(6, 18, 0), time(5, 18,0 ), 0);
    StopTimeEntryImpl st_4_3 = stopTime(1, s25, t4, time(6, 20, 0), time(5, 20,0 ), 0);
    StopTimeEntryImpl st_4_4 = stopTime(1, std, t4, time(6, 22, 0), time(5, 22,0 ), 0);

    StopTimeEntryImpl st_5_1 = stopTime(1, std, t5, time(7, 0, 0), time(5, 0,0 ), 0);
    StopTimeEntryImpl st_5_2 = stopTime(1, s25, t5, time(7, 2, 0), time(5, 2,0 ), 0);
    StopTimeEntryImpl st_5_3 = stopTime(1, sus, t5, time(7, 4, 0), time(5, 4,0 ), 0);
    StopTimeEntryImpl st_5_4 = stopTime(1, scc, t5, time(7, 6, 0), time(5, 6,0 ), 0);

    StopTimeEntryImpl st_6_1 = stopTime(1, scc, t6, time(7, 16, 0), time(5, 16,0 ), 0);
    StopTimeEntryImpl st_6_2 = stopTime(1, sus, t6, time(7, 18, 0), time(5, 18,0 ), 0);
    StopTimeEntryImpl st_6_3 = stopTime(1, s25, t6, time(7, 20, 0), time(5, 20,0 ), 0);
    StopTimeEntryImpl st_6_4 = stopTime(1, std, t6, time(7, 22, 0), time(5, 22,0 ), 0);

    ServiceIdActivation serviceIdActivationWeekday = serviceIds(lsids("WD_TL"), lsids());
    ServiceIdActivation serviceIdActivationSaturday = serviceIds(lsids("SA"), lsids());
    ServiceIdActivation serviceIdActivationSunday = serviceIds(lsids("SU"), lsids());

    BlockConfigurationEntry bc1 = blockConfiguration(b1, serviceIdActivationWeekday, t1);
    BlockConfigurationEntry bc2 = blockConfiguration(b2, serviceIdActivationWeekday, t2);
    BlockConfigurationEntry bc3 = blockConfiguration(b3, serviceIdActivationSaturday, t3);
    BlockConfigurationEntry bc4 = blockConfiguration(b4, serviceIdActivationSaturday, t4);
    BlockConfigurationEntry bc5 = blockConfiguration(b5, serviceIdActivationSunday, t5);
    BlockConfigurationEntry bc6 = blockConfiguration(b6, serviceIdActivationSunday, t6);
    RouteCollectionEntryImpl routeCollection = routeCollection(ROUTE_ID, route);
    route.setTrips(Arrays.asList(t1, t2, t3, t4, t5, t6));

    List<BlockTripEntry> weekdayTrips = new ArrayList<BlockTripEntry>();

    for (BlockConfigurationEntry blockConfig : Arrays.asList(bc1,bc2)) {
      weekdayTrips.add(blockConfig.getTrips().get(0));
    }
    blockIndexWeekday = factory.createTripIndexForGroupOfBlockTrips(weekdayTrips);
    List<BlockTripEntry> saturdayTrips = new ArrayList<BlockTripEntry>();
    for (BlockConfigurationEntry blockConfig : Arrays.asList(bc3,bc4)) {
      saturdayTrips.add(blockConfig.getTrips().get(0));
    }
    blockIndexSaturday = factory.createTripIndexForGroupOfBlockTrips(saturdayTrips);
    List<BlockTripEntry> sundayTrips = new ArrayList<BlockTripEntry>();
    for (BlockConfigurationEntry blockConfig : Arrays.asList(bc5,bc6)) {
      sundayTrips.add(blockConfig.getTrips().get(0));
    }
    blockIndexSunday = factory.createTripIndexForGroupOfBlockTrips(sundayTrips);

    TripNarrative.Builder tnA = TripNarrative.builder();
    tnA.setTripHeadsign("Theater District Station");
    TripNarrative.Builder tnB = TripNarrative.builder();
    tnB.setTripHeadsign("Tacoma Dome Station");

    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL1000"))).thenReturn(tnA.create());
    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL1001"))).thenReturn(tnB.create());
    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL6000"))).thenReturn(tnA.create());
    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL6001"))).thenReturn(tnB.create());
    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL7000"))).thenReturn(tnA.create());
    Mockito.when(narrativeService.getTripForId(new AgencyAndId(AGENCY_ID, "TacL7001"))).thenReturn(tnB.create());
    AgencyNarrative.Builder narrative = AgencyNarrative.builder();
    narrative.setLang("en");
    narrative.setName("ACTA");
    narrative.setPhone("123 123-1234");
    narrative.setEmail("abuse@example.com");
    narrative.setTimezone("America/Los_Angeles"); // we need to stay consistent with UnitTestingSupport
    narrative.setUrl("http://example.com");
    narrative.setFareUrl("http:/example.com");

    Mockito.when(narrativeService.getAgencyForId(AGENCY_ID)).thenReturn(narrative.create());

    Mockito.when(dao.getRouteCollectionForId(new AgencyAndId(AGENCY_ID, ROUTE_ID))).thenReturn(routeCollection);

    // no service alerts
    Mockito.when(serviceAlertsService.getServiceAlerts(any())).thenReturn(Collections.emptyList());

    return route.getParent();
  }
}
