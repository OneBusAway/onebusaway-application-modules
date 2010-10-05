package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative.Builder;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class ArrivalsAndDeparturesBeanServiceImplTest {

  private ArrivalsAndDeparturesBeanServiceImpl _service;
  private BlockLocationService _blockLocationService;
  private NarrativeService _narrativeService;
  private ScheduledBlockLocationService _scheduledBlockLocationService;
  private StopTimeService _stopTimeService;
  private TripBeanService _tripBeanService;

  @Before
  public void setup() {

    _service = new ArrivalsAndDeparturesBeanServiceImpl();

    _blockLocationService = Mockito.mock(BlockLocationService.class);
    _service.setBlockLocationService(_blockLocationService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

    _scheduledBlockLocationService = Mockito.mock(ScheduledBlockLocationService.class);
    _service.setScheduledBlockLocationService(_scheduledBlockLocationService);

    _stopTimeService = Mockito.mock(StopTimeService.class);
    _service.setStopTimeService(_stopTimeService);

    _tripBeanService = Mockito.mock(TripBeanService.class);
    _service.setTripBeanService(_tripBeanService);
  }

  @Test
  public void test() {

    long t = dateAsLong("2010-10-05 16:30");
    long serviceDate = dateAsLong("2010-10-05 00:00");
    int minutesBefore = 5;
    int minutesAfter = 30;

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.0, -122.0);

    /****
     * Block A
     ****/

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "sA", 3000);

    stopTime(0, stopA, tripA, time(16, 30), time(16, 35), 1000);
    StopTimeEntryImpl stopTimeAB = stopTime(1, stopB, tripA, time(16, 40),
        time(16, 45), 2000);

    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds(lsids("sA"), lsids()), tripA);
    BlockStopTimeEntry bstAA = blockConfigA.getStopTimes().get(0);
    BlockStopTimeEntry bstAB = blockConfigA.getStopTimes().get(1);

    /****
     * Block B
     ****/

    BlockEntryImpl blockB = block("blockB");
    TripEntryImpl tripB = trip("tripB", "sA", 3000);

    stopTime(2, stopA, tripB, time(16, 40), time(16, 45), 1000);
    StopTimeEntryImpl stopTimeBB = stopTime(3, stopB, tripB, time(16, 50),
        time(16, 55), 2000);

    BlockConfigurationEntry blockConfigB = blockConfiguration(blockB,
        serviceIds(lsids("sA"), lsids()), tripB);
    BlockStopTimeEntry bstBA = blockConfigB.getStopTimes().get(0);
    BlockStopTimeEntry bstBB = blockConfigB.getStopTimes().get(1);

    /****
     * 
     ****/

    int expandedMinutesBefore = ArrivalsAndDeparturesBeanServiceImpl.MINUTES_BEFORE_BUFFER
        + minutesBefore;
    int expandedMinutesAfter = ArrivalsAndDeparturesBeanServiceImpl.MINUTES_AFTER_BUFFER
        + minutesAfter;

    Date stopTimeFrom = new Date(t - expandedMinutesBefore * 60 * 1000);
    Date stopTimeTo = new Date(t + expandedMinutesAfter * 60 * 1000);

    StopTimeInstanceProxy sti1 = new StopTimeInstanceProxy(bstAB, serviceDate);
    StopTimeInstanceProxy sti2 = new StopTimeInstanceProxy(bstBB, serviceDate);

    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(stopB.getId(),
            stopTimeFrom, stopTimeTo)).thenReturn(Arrays.asList(sti1, sti2));

    /****
     * 
     ****/

    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, serviceDate);
    long lastUpdateTime = dateAsLong("2010-10-05 16:15");

    BlockLocation blockLocation = new BlockLocation();
    blockLocation.setActiveTrip(bstAA.getTrip());
    blockLocation.setBlockInstance(blockInstanceA);
    blockLocation.setClosestStop(bstAA);
    blockLocation.setDistanceAlongBlock(500);
    blockLocation.setInService(true);
    blockLocation.setLastUpdateTime(lastUpdateTime);
    blockLocation.setNextStop(bstAA);
    blockLocation.setPredicted(true);
    blockLocation.setScheduledDistanceAlongBlock(600);
    blockLocation.setScheduleDeviation(10 * 60);
    blockLocation.setVehicleId(aid("vehicle"));

    Mockito.when(
        _blockLocationService.getLocationsForBlockInstance(blockInstanceA, t)).thenReturn(
        Arrays.asList(blockLocation));

    /****
     * 
     ****/

    ScheduledBlockLocation sbl = new ScheduledBlockLocation();
    sbl.setActiveTrip(bstAB.getTrip());
    sbl.setClosestStop(bstBA);
    sbl.setDistanceAlongBlock(400);
    sbl.setNextStop(bstBA);
    sbl.setScheduledTime(time(16, 30));

    Mockito.when(
        _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
            blockConfigB.getStopTimes(), time(16, 30))).thenReturn(sbl);

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

    /****
     * 
     ****/

    TripBean tripABean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("tripA"))).thenReturn(
        tripABean);

    TripBean tripBBean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("tripB"))).thenReturn(
        tripBBean);

    /****
     * 
     ****/

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _service.getArrivalsAndDeparturesByStopId(
        stopB.getId(), new Date(t), minutesBefore, minutesAfter);

    assertEquals(2, arrivalsAndDepartures.size());

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
    assertEquals("1_stopB", bean.getStopId());
    assertSame(tripABean, bean.getTrip());
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
    assertEquals("1_stopB", bean.getStopId());
    assertSame(tripBBean, bean.getTrip());
    assertNull(bean.getTripHeadsign());
    assertNull(bean.getVehicleId());
  }
}
