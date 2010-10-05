package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.dateAsLong;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
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
  private StopTimeService _stopTimeService;
  private TripBeanService _tripBeanService;

  @Before
  public void setup() {

    _service = new ArrivalsAndDeparturesBeanServiceImpl();

    _blockLocationService = Mockito.mock(BlockLocationService.class);
    _service.setBlockLocationService(_blockLocationService);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _service.setNarrativeService(_narrativeService);

    _stopTimeService = Mockito.mock(StopTimeService.class);
    _service.setStopTimeService(_stopTimeService);

    _tripBeanService = Mockito.mock(TripBeanService.class);
    _service.setTripBeanService(_tripBeanService);
  }

  @Test
  public void test() {

    AgencyAndId stopId = aid("stop");
    long t = dateAsLong("2010-10-05 16:18");
    long serviceDate = dateAsLong("2010-10-05 00:00");
    int minutesBefore = 5;
    int minutesAfter = 30;

    Date stopTimeFrom = new Date(
        t
            - (ArrivalsAndDeparturesBeanServiceImpl.MINUTES_BEFORE_BUFFER + minutesBefore)
            * 60 * 1000);
    Date stopTimeTo = new Date(
        t
            + (ArrivalsAndDeparturesBeanServiceImpl.MINUTES_AFTER_BUFFER + minutesAfter)
            * 60 * 1000);

    StopEntryImpl stopA = stop("stopA", 47.0, -122.0);
    StopEntryImpl stopB = stop("stopB", 47.0, -122.0);

    BlockEntryImpl blockA = block("block");
    TripEntryImpl tripA = trip("trip", "sA", 3000);

    stopTime(0, stopA, tripA, time(16, 30), 1000);
    StopTimeEntryImpl stopTimeB = stopTime(0, stopB, tripA, time(16, 40), 2000);

    BlockConfigurationEntry blockConfig = blockConfiguration(blockA,
        serviceIds(lsids("sA"), lsids()), tripA);
    BlockStopTimeEntry bstA = blockConfig.getStopTimes().get(0);
    BlockStopTimeEntry bstB = blockConfig.getStopTimes().get(1);

    /****
     * 
     ****/

    StopTimeInstanceProxy sti = new StopTimeInstanceProxy(bstB, serviceDate);
    Mockito.when(
        _stopTimeService.getStopTimeInstancesInTimeRange(stopId, stopTimeFrom,
            stopTimeTo)).thenReturn(Arrays.asList(sti));

    /****
     * 
     ****/

    BlockInstance blockInstance = new BlockInstance(blockConfig, serviceDate);
    long lastUpdateTime = dateAsLong("2010-10-05 16:15");

    BlockLocation blockLocation = new BlockLocation();
    blockLocation.setActiveTrip(bstA.getTrip());
    blockLocation.setBlockInstance(blockInstance);
    blockLocation.setClosestStop(bstA);
    blockLocation.setDistanceAlongBlock(500);
    blockLocation.setInService(true);
    blockLocation.setLastUpdateTime(lastUpdateTime);
    blockLocation.setNextStop(bstA);
    blockLocation.setPredicted(true);
    blockLocation.setScheduledDistanceAlongBlock(600);
    blockLocation.setScheduleDeviation(60);
    blockLocation.setVehicleId(aid("vehicle"));

    Mockito.when(
        _blockLocationService.getLocationsForBlockInstance(blockInstance, t)).thenReturn(
        Arrays.asList(blockLocation));

    /****
     * 
     ****/

    Builder stopTimeNarrative = StopTimeNarrative.builder();
    stopTimeNarrative.setStopHeadsign("Downtowm");
    Mockito.when(_narrativeService.getStopTimeForEntry(stopTimeB)).thenReturn(
        stopTimeNarrative.create());

    /****
     * 
     ****/

    TripBean tripBean = new TripBean();
    Mockito.when(_tripBeanService.getTripForId(aid("trip"))).thenReturn(
        tripBean);

    /****
     * 
     ****/

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _service.getArrivalsAndDeparturesByStopId(
        stopId, new Date(t), minutesBefore, minutesAfter);

    assertEquals(1, arrivalsAndDepartures.size());

    ArrivalAndDepartureBean bean = arrivalsAndDepartures.get(0);
    assertEquals(1500, bean.getDistanceFromStop(), 0.0);
    assertEquals(lastUpdateTime, bean.getLastUpdateTime().longValue());
    assertEquals(1, bean.getNumberOfStopsAway());
    assertEquals(serviceDate, bean.getServiceDate());
    assertEquals("default", bean.getStatus());
    assertEquals("1_stopB", bean.getStopId());
    assertSame(tripBean, bean.getTrip());
    assertEquals("1_vehicle", bean.getVehicleId());
  }
}
