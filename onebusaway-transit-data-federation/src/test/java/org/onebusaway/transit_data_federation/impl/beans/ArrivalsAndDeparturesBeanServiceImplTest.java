/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import static org.junit.Assert.*;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeNegativeArrivals;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeNegativeArrivalsImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative.Builder;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.model.StopTimeInstance;

public class ArrivalsAndDeparturesBeanServiceImplTest {

  private TransitGraphDao _transitGraphDao;
  private ArrivalsAndDeparturesBeanServiceImpl _service;
  private NarrativeService _narrativeService;
  private ArrivalAndDepartureService _arrivalAndDepartureService;
  private TripBeanService _tripBeanService;
  private StopBeanService _stopBeanService;
  private TripDetailsBeanService _tripDetailsBeanService;
  private ServiceAlertsBeanService _serviceAlertsBeanService;
  private GtfsRealtimeNegativeArrivals _gtfsRealtimeNegativeArrivals;

  @Before
  public void setup() {

    _service = new ArrivalsAndDeparturesBeanServiceImpl();

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_transitGraphDao);

    _arrivalAndDepartureService = Mockito.mock(ArrivalAndDepartureService.class);
    _service.setArrivalAndDepartureService(_arrivalAndDepartureService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

    _tripBeanService = Mockito.mock(TripBeanService.class);
    _service.setTripBeanService(_tripBeanService);

    _stopBeanService = Mockito.mock(StopBeanService.class);
    _service.setStopBeanService(_stopBeanService);

    _tripDetailsBeanService = Mockito.mock(TripDetailsBeanService.class);
    _service.setTripDetailsBeanService(_tripDetailsBeanService);

    _serviceAlertsBeanService = Mockito.mock(ServiceAlertsBeanService.class);
    _service.setServiceAlertsBeanService(_serviceAlertsBeanService);
    
    _gtfsRealtimeNegativeArrivals = new GtfsRealtimeNegativeArrivalsImpl();
    _service.setGtfsRealtimeNegativeArrivals(_gtfsRealtimeNegativeArrivals);
  }

  @Test
  public void test() {

    long t = dateAsLong("2010-10-05 16:30");
    long serviceDate = dateAsLong("2010-10-05 00:00");
    int minutesBefore = 5;
    int minutesAfter = 30;

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.0, -122.0);
    StopEntryImpl stopC = stop("stopC", 47.0, -122.0);

    Mockito.when(_transitGraphDao.getStopEntryForId(stopA.getId(), true)).thenReturn(
        stopA);
    Mockito.when(_transitGraphDao.getStopEntryForId(stopB.getId(), true)).thenReturn(
        stopB);
    Mockito.when(_transitGraphDao.getStopEntryForId(stopC.getId(), true)).thenReturn(
        stopC);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "sA", 3000);

    stopTime(0, stopA, tripA, time(16, 30), time(16, 35), 1000,50.0);
    StopTimeEntryImpl stopTimeAB = stopTime(1, stopB, tripA, time(16, 40),
        time(16, 45), 2000,75.0);

    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds(lsids("sA"), lsids()), tripA);
    BlockStopTimeEntry bstAA = blockConfigA.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfigA.getStopTimes().get(1);

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("blockB");
    TripEntryImpl tripB = trip("tripB", "sA", 3000);

    stopTime(2, stopA, tripB, time(16, 40), time(16, 45), 1000,50.0);
    StopTimeEntryImpl stopTimeBB = stopTime(3, stopB, tripB, time(16, 50),
        time(16, 55), 2000,75.0);

    BlockConfigurationEntry blockConfigB = blockConfiguration(blockB,
        serviceIds(lsids("sA"), lsids()), tripB);
    BlockStopTimeEntry bstBA = blockConfigB.getStopTimes().get(0);
    BlockStopTimeEntry bstBB = blockConfigB.getStopTimes().get(1);

    /****
     * Block C
     ****/

    BlockEntryImpl blockC = block("blockC");
    TripEntryImpl tripC = trip("tripC", "sA", 3000);

    stopTime(4, stopC, tripC, time(16, 40), time(16, 45), 1000,50.0);
    StopTimeEntryImpl stopTimeCB = stopTime(5, stopC, tripC, time(16, 50), time(16, 55), 2000, 75.0);

    BlockConfigurationEntry blockConfigC = blockConfiguration(blockC,
        serviceIds(lsids("sA"), lsids()), tripC);
    BlockStopTimeEntry bstCA = blockConfigC.getStopTimes().get(0);
    BlockStopTimeEntry bstCB = blockConfigC.getStopTimes().get(1);

    /****
     * 
     ****/

    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, serviceDate);
    long lastUpdateTime = dateAsLong("2010-10-05 16:15");

    BlockLocation blockLocationA = new BlockLocation();
    blockLocationA.setActiveTrip(bstAA.getTrip());
    blockLocationA.setBlockInstance(blockInstanceA);
    blockLocationA.setClosestStop(bstAA);
    blockLocationA.setDistanceAlongBlock(500);
    blockLocationA.setInService(true);
    blockLocationA.setLastUpdateTime(lastUpdateTime);
    blockLocationA.setNextStop(bstAA);
    blockLocationA.setPredicted(true);
    blockLocationA.setScheduledDistanceAlongBlock(600);
    blockLocationA.setScheduleDeviation(10 * 60);
    blockLocationA.setVehicleId(aid("vehicle"));

    /****
     * 
     ****/

    BlockInstance blockInstanceB = new BlockInstance(blockConfigB, serviceDate);

    BlockLocation blockLocationB = new BlockLocation();
    blockLocationB.setActiveTrip(bstBA.getTrip());
    blockLocationB.setBlockInstance(blockInstanceA);
    blockLocationB.setClosestStop(bstBA);
    blockLocationB.setDistanceAlongBlock(400);
    blockLocationB.setInService(true);
    blockLocationB.setNextStop(bstAA);
    blockLocationB.setPredicted(false);
    blockLocationB.setScheduledDistanceAlongBlock(400);

    /****
     * 
     ****/

    BlockInstance blockInstanceC = new BlockInstance(blockConfigC, serviceDate);

    BlockLocation blockLocationC = new BlockLocation();
    blockLocationC.setActiveTrip(bstCA.getTrip());
    blockLocationC.setBlockInstance(blockInstanceC);
    blockLocationC.setClosestStop(bstCB);
    blockLocationC.setDistanceAlongBlock(400);
    blockLocationC.setInService(true);
    blockLocationC.setNextStop(bstCB);
    blockLocationC.setPredicted(false);
    blockLocationC.setScheduledDistanceAlongBlock(400);

    /****
     *
     ****/

    long stopTimeFrom = t - minutesBefore * 60 * 1000;
    long stopTimeTo = t + minutesAfter * 60 * 1000;

    StopTimeInstance sti1 = new StopTimeInstance(bstAB,blockInstanceA.getState());
    ArrivalAndDepartureInstance in1 = new ArrivalAndDepartureInstance(sti1);
    in1.setBlockLocation(blockLocationA);
    in1.setPredictedArrivalTime((long) (in1.getScheduledArrivalTime() + 5 * 60 * 1000));
    in1.setPredictedDepartureTime((long) (in1.getScheduledDepartureTime()));

    StopTimeInstance sti2 = new StopTimeInstance(bstBB, blockInstanceB.getState());
    ArrivalAndDepartureInstance in2 = new ArrivalAndDepartureInstance(sti2);
    in2.setBlockLocation(blockLocationB);

    StopTimeInstance sti3 = new StopTimeInstance(bstCB, blockInstanceC.getState());
    ArrivalAndDepartureInstance in3 = new ArrivalAndDepartureInstance(sti3);
    in3.setBlockLocation(blockLocationC);

    TargetTime target = new TargetTime(t, t);

    Mockito.when(
        _arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(
            stopB, target, stopTimeFrom, stopTimeTo)).thenReturn(
        Arrays.asList(in1, in2, in3));

    Mockito.when(
        _arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(
            stopC, target, stopTimeFrom, stopTimeTo)).thenReturn(
        Arrays.asList(in3));

    /****
     * 
     ****/

    Builder stopTimeNarrative = StopTimeNarrative.builder();
    stopTimeNarrative.setStopHeadsign("Downtown");
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeAB)).thenReturn(
        stopTimeNarrative.create());

    stopTimeNarrative = StopTimeNarrative.builder();
    stopTimeNarrative.setRouteShortName("XX");
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeBB)).thenReturn(
        stopTimeNarrative.create());

    stopTimeNarrative = StopTimeNarrative.builder();
    stopTimeNarrative.setRouteShortName("YY");
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeCB)).thenReturn(
        stopTimeNarrative.create());

    /****
     * 
     ****/

    StopBean stopABean = new StopBean();
    stopABean.setId("1_stopA");
    Mockito.when(_stopBeanService.getStopForId(stopA.getId(), null)).thenReturn(
        stopABean);

    StopBean stopBBean = new StopBean();
    stopBBean.setId("1_stopB");
    Mockito.when(_stopBeanService.getStopForId(stopB.getId(), null)).thenReturn(
        stopBBean);

    StopBean stopCBean = new StopBean();
    stopCBean.setId("1_stopC");
    Mockito.when(_stopBeanService.getStopForId(stopC.getId(), null)).thenReturn(
        stopCBean);

    /****
     * 
     ****/

    TripBean tripABean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("tripA"))).thenReturn(
        tripABean);

    TripBean tripBBean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("tripB"))).thenReturn(
        tripBBean);

    TripBean tripCBean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("tripC"))).thenReturn(
        tripCBean);

    /****
     * 
     ****/

    TripStatusBean tripStatusBeanA = new TripStatusBean();
    TripStatusBean tripStatusBeanB = new TripStatusBean();
    TripStatusBean tripStatusBeanC = new TripStatusBean();

    Mockito.when(
        _tripDetailsBeanService.getBlockLocationAsStatusBean(blockLocationA, t)).thenReturn(
        tripStatusBeanA);

    Mockito.when(
        _tripDetailsBeanService.getBlockLocationAsStatusBean(blockLocationB, t)).thenReturn(
        tripStatusBeanB);

    Mockito.when(
        _tripDetailsBeanService.getBlockLocationAsStatusBean(blockLocationC, t)).thenReturn(
        tripStatusBeanC);

    /****
     * 
     ****/

    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    query.setTime(t);
    query.setMinutesBefore(minutesBefore);
    query.setMinutesAfter(minutesAfter);
    query.setFrequencyMinutesBefore(minutesBefore);
    query.setFrequencyMinutesAfter(minutesAfter);

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _service.getArrivalsAndDeparturesByStopId(
        stopB.getId(), query);

    assertEquals(3, arrivalsAndDepartures.size());

    ArrivalAndDepartureBean bean = arrivalsAndDepartures.get(0);
    assertEquals(1500, bean.getDistanceFromStop(), 0.0);
    assertEquals(lastUpdateTime, bean.getLastUpdateTime().longValue());
    assertEquals(1, bean.getNumberOfStopsAway());
    assertEquals(dateAsLong("2010-10-05 16:45"), bean.getPredictedArrivalTime());
    assertEquals(dateAsLong("2010-10-05 16:45"),
        bean.getPredictedDepartureTime());
    assertNull(bean.getRouteShortName());
    assertEquals(dateAsLong("2010-10-05 16:40"), bean.getScheduledArrivalTime());
    assertEquals(dateAsLong("2010-10-05 16:45"),
        bean.getScheduledDepartureTime());
    assertEquals(serviceDate, bean.getServiceDate());
    assertEquals("default", bean.getStatus());
    assertSame(stopBBean, bean.getStop());
    assertSame(tripABean, bean.getTrip());
    assertSame(tripStatusBeanA, bean.getTripStatus());
    assertEquals("Downtown", bean.getTripHeadsign());
    assertEquals("1_vehicle", bean.getVehicleId());

    bean = arrivalsAndDepartures.get(1);
    assertEquals(1600, bean.getDistanceFromStop(), 0.0);
    assertNull(bean.getLastUpdateTime());
    assertEquals(1, bean.getNumberOfStopsAway());
    assertEquals(0L, bean.getPredictedArrivalTime());
    assertEquals(0L, bean.getPredictedDepartureTime());
    assertEquals("XX", bean.getRouteShortName());
    assertEquals(dateAsLong("2010-10-05 16:50"), bean.getScheduledArrivalTime());
    assertEquals(dateAsLong("2010-10-05 16:55"),
        bean.getScheduledDepartureTime());
    assertEquals(serviceDate, bean.getServiceDate());
    assertEquals("default", bean.getStatus());
    assertSame(stopBBean, bean.getStop());
    assertSame(tripBBean, bean.getTrip());
    assertSame(tripStatusBeanB, bean.getTripStatus());
    assertNull(bean.getTripHeadsign());
    assertNull(bean.getVehicleId());

    bean = arrivalsAndDepartures.get(2);
    assertSame(tripCBean, bean.getTrip());
    assertSame(stopCBean, bean.getStop());
    assertEquals(serviceDate, bean.getServiceDate());
    assertEquals("YY", bean.getRouteShortName());
    assertEquals("default", bean.getStatus());
  }
}
