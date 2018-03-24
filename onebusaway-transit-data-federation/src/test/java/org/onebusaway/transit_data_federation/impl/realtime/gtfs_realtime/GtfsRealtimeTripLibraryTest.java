/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.blockConfiguration;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.realtime.api.TimepointPredictionRecord;


import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtimeConstants;


import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GtfsRealtimeTripLibraryTest {

  private GtfsRealtimeTripLibrary _library;
  private GtfsRealtimeEntitySource _entitySource;
  private BlockCalendarService _blockCalendarService;

  @Before
  public void before() {
    _library = new GtfsRealtimeTripLibrary();
    _library.setCurrentTime(8 * 60 * 60 * 1000);
    _library.setValidateCurrentTime(false);  // tell library its a test
    _entitySource = Mockito.mock(GtfsRealtimeEntitySource.class);
    _library.setEntitySource(_entitySource);

    _blockCalendarService = Mockito.mock(BlockCalendarService.class);
    _library.setBlockCalendarService(_blockCalendarService);
  }

  @Test
  public void test() {

    FeedEntity tripUpdateEntityA = createTripUpdate("tripA", "stopA", 60, true);
    FeedEntity tripUpdateEntityB = createTripUpdate("tripB", "stopB", 120, true);
    FeedEntity tripUpdateEntityC = createTripUpdate("tripC", "stopA", 30, true);
    FeedEntity tripUpdateEntityD = createTripUpdate("tripD", "stopB", 90, true);

    FeedMessage.Builder tripUpdates = createFeed();
    tripUpdates.addEntity(tripUpdateEntityA);
    tripUpdates.addEntity(tripUpdateEntityB);
    tripUpdates.addEntity(tripUpdateEntityC);
    tripUpdates.addEntity(tripUpdateEntityD);

    TripEntryImpl tripA = trip("tripA");
    TripEntryImpl tripB = trip("tripB");
    TripEntryImpl tripC = trip("tripC");
    TripEntryImpl tripD = trip("tripD");

    StopEntryImpl stopA = stop("stopA", 0, 0);
    StopEntryImpl stopB = stop("stopB", 0, 0);

    stopTime(0, stopA, tripA, time(7, 30), 0.0);
    stopTime(1, stopB, tripB, time(8, 30), 0.0);
    stopTime(2, stopA, tripC, time(8, 30), 0.0);
    stopTime(3, stopB, tripD, time(9, 30), 0.0);

    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    Mockito.when(_entitySource.getTrip("tripB")).thenReturn(tripB);
    Mockito.when(_entitySource.getTrip("tripC")).thenReturn(tripC);
    Mockito.when(_entitySource.getTrip("tripD")).thenReturn(tripD);

    BlockEntryImpl blockA = block("blockA");
    BlockEntryImpl blockB = block("blockB");

    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA, tripB);
    BlockConfigurationEntry blockConfigB = blockConfiguration(blockB,
        serviceIds("s1"), tripC, tripD);

    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    BlockInstance blockInstanceB = new BlockInstance(blockConfigB, 0L);

    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(
        Arrays.asList(blockInstanceA));
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockB.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(
        Arrays.asList(blockInstanceB));

    FeedMessage.Builder vehiclePositions = createFeed();
    // FeedEntity.Builder vehiclePositionEntity = FeedEntity.newBuilder();
    // vehiclePositions.addEntity(vehiclePositionEntity);

    List<CombinedTripUpdatesAndVehiclePosition> groups = _library.groupTripUpdatesAndVehiclePositions(
        tripUpdates.build(), vehiclePositions.build());

    assertEquals(2, groups.size());

    Collections.sort(groups);

    CombinedTripUpdatesAndVehiclePosition group = groups.get(0);
    assertSame(blockA, group.block.getBlockInstance().getBlock().getBlock());
    assertEquals(2, group.tripUpdates.size());
    TripUpdate tripUpdate = group.tripUpdates.get(0);
    assertEquals("tripA", tripUpdate.getTrip().getTripId());
    tripUpdate = group.tripUpdates.get(1);
    assertEquals("tripB", tripUpdate.getTrip().getTripId());

    group = groups.get(1);
    assertSame(blockB, group.block.getBlockInstance().getBlock().getBlock());
    assertEquals(2, group.tripUpdates.size());
    tripUpdate = group.tripUpdates.get(0);
    assertEquals("tripC", tripUpdate.getTrip().getTripId());
    tripUpdate = group.tripUpdates.get(1);
    assertEquals("tripD", tripUpdate.getTrip().getTripId());

    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(groups.get(0));
    assertEquals(blockA.getId(), record.getBlockId());
    assertEquals(120, record.getScheduleDeviation(), 0.0);
    assertEquals(0L, record.getServiceDate());
    assertEquals(blockA.getId(), record.getVehicleId());

    record = _library.createVehicleLocationRecordForUpdate(groups.get(1));
    assertEquals(blockB.getId(), record.getBlockId());
    assertEquals(30, record.getScheduleDeviation(), 0.0);
    assertEquals(0L, record.getServiceDate());
    assertEquals(blockB.getId(), record.getVehicleId());
  }

  @Test
  public void testCreateVehicleLocationRecordForUpdate_NoStopTimeUpdates() {
    TripUpdate tripUpdate = TripUpdate.newBuilder()
        .setTrip(TripDescriptor.newBuilder().setTripId("tripA"))
        .setDelay(120)
        .setTimestamp(123456789)
        .build();

    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(
        Arrays.asList(blockInstanceA));

    CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();
    update.block = new BlockDescriptor();
    update.block.setBlockInstance(blockInstanceA);
    update.tripUpdates = Arrays.asList(tripUpdate);

    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    assertEquals(123456789000L, record.getTimeOfRecord());
    assertEquals(120, record.getScheduleDeviation(), 0.0);
  }
  
  @Test
  public void testCreateVehicleLocationRecordForUpdate_WithStopTimeUpdates() {
    
    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
    stopTimeUpdate.setStopId("stopA");
    StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
    stopTimeEvent.setDelay(180);
    stopTimeUpdate.setDeparture(stopTimeEvent);
    
    TripUpdate tripUpdate = TripUpdate.newBuilder()
        .setTrip(TripDescriptor.newBuilder().setTripId("tripA"))
        .setDelay(120)
        .setTimestamp(123456789)
        .addStopTimeUpdate(stopTimeUpdate)
        .build();

    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(
        Arrays.asList(blockInstanceA));

    CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();
    update.block = new BlockDescriptor();
    update.block.setBlockInstance(blockInstanceA);
    update.tripUpdates = Arrays.asList(tripUpdate);

    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    
    TimepointPredictionRecord tpr = record.getTimepointPredictions().get(0);
    long departure = tpr.getTimepointPredictedDepartureTime();
    assertEquals(departure, time(7, 33) * 1000); // 7:30 plus 3 min delay, + now we are in ms.
  }

  @Test
  public void testStopRewriting() {

    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
    stopTimeUpdate.setStopId("replaceA");
    StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
    stopTimeEvent.setDelay(180);
    stopTimeUpdate.setDeparture(stopTimeEvent);
    stopTimeUpdate.setStopSequence(0);

    TripUpdate tripUpdate = TripUpdate.newBuilder()
            .setTrip(TripDescriptor.newBuilder().setTripId("tripA"))
            .setDelay(120)
            .setTimestamp(123456789)
            .addStopTimeUpdate(stopTimeUpdate)
            .build();

    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
            serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    Mockito.when(
            _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
                    Mockito.anyLong(), Mockito.anyLong())).thenReturn(
            Collections.singletonList(blockInstanceA));

    CombinedTripUpdatesAndVehiclePosition update = new CombinedTripUpdatesAndVehiclePosition();
    update.block = new BlockDescriptor();
    update.block.setBlockInstance(blockInstanceA);
    update.tripUpdates = Collections.singletonList(tripUpdate);

    StopModificationStrategy strategy = Mockito.mock(StopModificationStrategy.class);
    Mockito.when(strategy.convertStopId("replaceA")).thenReturn("stopA");

    _library.setStopModificationStrategy(strategy);

    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    assertEquals(123456789000L, record.getTimeOfRecord());
    assertEquals(120, record.getScheduleDeviation(), 0.0);

    TimepointPredictionRecord tpr = record.getTimepointPredictions().get(0);
    long departure = tpr.getTimepointPredictedDepartureTime();
    assertEquals(departure, time(7, 33) * 1000);
  }

  // Ensure that if we get an update for a future day we propagate a prediction for that day.
  // (This is equivalent to timestamp on feed being early incorrectly, since currentTime in
  // GtfsRealtimeTripLibrary is set via the timestamp.)
  @Test
  public void testCreateVehicleLocationRecordForUpdate_FutureDay() {
    
    final long day = TimeUnit.DAYS.toMillis(1);
    
    StopTimeUpdate.Builder stopTimeUpdate = stopTimeUpdateWithDepartureDelay("stopA", 180);
   
    TripUpdate.Builder tripUpdate = tripUpdate("tripA", (_library.getCurrentTime() + day)/1000, 
        120, stopTimeUpdate);

    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    BlockInstance blockInstanceB = new BlockInstance(blockConfigA, day);
    
    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.longThat(new ArgumentMatcher<Long>() {
              @Override
              public boolean matches(Object argument) {
                return ((Long) argument) < day;
              }
            }))).thenReturn(Arrays.asList(blockInstanceA));
    
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.longThat(new ArgumentMatcher<Long>() {
              @Override
              public boolean matches(Object argument) {
                return ((Long) argument) >= day;
              }
            }))).thenReturn(Arrays.asList(blockInstanceB));
    
    
    FeedMessage.Builder TU = createFeed();
    TU.addEntity(feed(tripUpdate));
    
    FeedMessage.Builder VP = createFeed();
    
    List<CombinedTripUpdatesAndVehiclePosition> updates = 
        _library.groupTripUpdatesAndVehiclePositions(TU.build(), VP.build());

    CombinedTripUpdatesAndVehiclePosition update = updates.get(0);
        
    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    
    TimepointPredictionRecord tpr = record.getTimepointPredictions().get(0);
    long departure = tpr.getTimepointPredictedDepartureTime();
    assertEquals(departure, time(7, 33) * 1000 + day); // 7:30 + 3 min delay + on next day
  }
  
  /**
   * This method tests that we create timepoint prediction records for stops
   * that have not been served yet if there are TPRs downstream. If TPRs exist
   * downstream of a stop, the bus is assumed to be ahead of that stop. This
   * assumption is not necessarily true for stop time updates. We require that
   * the trip update delay indicates realtime schedule adherence for this 
   * behavior to make sense.
   * 
   * Current time = 7:31. Trip update delay = 2 minutes
   *          Schedule time    Real-time from feed  Timepoint predicted departure time
   * Stop A   7:30             -----                7:32
   * Stop B   7:40             7:43                 7:43
   */
  @Test
  public void testTprInterpolation_0() {
    
    _library.setCurrentTime(time(7, 31) * 1000);
    
    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    stopTime(1, stop("stopB", 0, 0), tripA, time(7, 40), 10.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    
    StopTimeUpdate.Builder stopTimeUpdate = stopTimeUpdateWithDepartureDelay("stopB", 180);
    TripUpdate.Builder tripUpdate = tripUpdate("tripA", _library.getCurrentTime()/1000,  120, stopTimeUpdate);
   
    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(Arrays.asList(blockInstanceA));
    
    VehicleLocationRecord record = vehicleLocationRecord(tripUpdate);
    
    long stopADept = getPredictedDepartureTimeByStopId(record, "stopA");
    assertEquals(stopADept, time(7, 32) * 1000);
    
    long stopBDept = getPredictedDepartureTimeByStopId(record, "stopB");
    assertEquals(stopBDept, time(7, 43) * 1000);
  }
  
  /**
   * Same as above, but we should NOT create new timepoint prediction records
   * because the stop has already been served. Only thing different is current
   * time.
   * 
   * Current time = 7:33. Trip update delay = 2 minutes
   *          Schedule time    Real-time from feed  Timepoint predicted departure time
   * Stop A   7:30             -----                ----
   * Stop B   7:40             7:43                 7:43
   */
  @Test
  public void testTprInterpolation_1() {
    
    _library.setCurrentTime(time(7, 33) * 1000);
    
    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    stopTime(1, stop("stopB", 0, 0), tripA, time(7, 40), 10.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    
    StopTimeUpdate.Builder stopTimeUpdate = stopTimeUpdateWithDepartureDelay("stopB", 180);
    TripUpdate.Builder tripUpdate = tripUpdate("tripA", _library.getCurrentTime()/1000,  120, stopTimeUpdate);
   
    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(Arrays.asList(blockInstanceA));
    
    VehicleLocationRecord record = vehicleLocationRecord(tripUpdate);
    
    long stopADept = getPredictedDepartureTimeByStopId(record, "stopA");
    assertEquals(stopADept, -1); // no tpr for this stop
    
    long stopBDept = getPredictedDepartureTimeByStopId(record, "stopB");
    assertEquals(stopBDept, time(7, 43) * 1000);
  }
  
  /**
   * Test that we do NOT create new timepoint prediction record when it 
   * already exists.
   * 
   * Current time = 7:25. Trip update delay = 2 minutes
   *          Schedule time    Real-time from feed  Timepoint predicted departure time
   * Stop A   7:30             7:33                 7:33 (and only one)
   */
  @Test
  public void testTprInterpolation_2() {
    
    _library.setCurrentTime(time(7, 25) * 1000);
    
    TripEntryImpl tripA = trip("tripA");
    stopTime(0, stop("stopA", 0, 0), tripA, time(7, 30), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
        serviceIds("s1"), tripA);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);
    
    StopTimeUpdate.Builder stopTimeUpdate = stopTimeUpdateWithDepartureDelay("stopA", 180);
    TripUpdate.Builder tripUpdate = tripUpdate("tripA", _library.getCurrentTime()/1000,  120, stopTimeUpdate);
   
    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    
    Mockito.when(
        _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
            Mockito.anyLong(), Mockito.anyLong())).thenReturn(Arrays.asList(blockInstanceA));
    
    VehicleLocationRecord record = vehicleLocationRecord(tripUpdate);
    
    long stopADept = getPredictedDepartureTimeByStopId(record, "stopA");
    assertEquals(stopADept, time(7, 33) * 1000);
    
    assertEquals(record.getTimepointPredictions().size(), 1);
  }

  /**
   * This method tests that we propagate a time point prediction record
   * when it comes from a trip that hasn't started yet.
   *
   * Current time = 7:31. Trip update delay = 2 minutes
   *                  Schedule time    Real-time from feed
   * Stop A (trip A)  7:30             7:33
   * Stop A (trip B)  7:40             7:44
   */
  @Test
  public void testTprOnFutureTrip() {

    _library.setCurrentTime(time(7, 31) * 1000);

    TripEntryImpl tripA = trip("tripA");
    TripEntryImpl tripB = trip("tripB");
    StopEntryImpl stopA = stop("stopA", 0, 0);
    stopTime(0, stopA, tripA, time(7, 30), 0.0);
    stopTime(0, stopA, tripB, time(7, 40), 0.0);
    BlockEntryImpl blockA = block("blockA");
    BlockConfigurationEntry blockConfigA = blockConfiguration(blockA,
            serviceIds("s1"), tripA, tripB);
    BlockInstance blockInstanceA = new BlockInstance(blockConfigA, 0L);

    StopTimeUpdate.Builder stuA = stopTimeUpdateWithDepartureDelay("stopA", 180);
    TripUpdate.Builder tuA = tripUpdate("tripA", _library.getCurrentTime()/1000,  120, stuA);

    StopTimeUpdate.Builder stuB = stopTimeUpdateWithDepartureDelay("stopA", 240);
    TripUpdate.Builder tuB = tripUpdate("tripB", _library.getCurrentTime()/1000,  0, stuB);

    tuA.setVehicle(vehicle("bus1"));
    tuB.setVehicle(vehicle("bus1"));

    Mockito.when(_entitySource.getTrip("tripA")).thenReturn(tripA);
    Mockito.when(_entitySource.getTrip("tripB")).thenReturn(tripB);

    Mockito.when(
            _blockCalendarService.getActiveBlocks(Mockito.eq(blockA.getId()),
                    Mockito.anyLong(), Mockito.anyLong())).thenReturn(Arrays.asList(blockInstanceA));

    VehicleLocationRecord record = vehicleLocationRecord(tuA, tuB);

    long tripADept = getPredictedDepartureTimeByStopIdAndTripId(record, "stopA", "tripA");
    assertEquals(tripADept, time(7, 33) * 1000);

    long tripBDept = getPredictedDepartureTimeByStopIdAndTripId(record, "stopA", "tripB");
    assertEquals(tripBDept, time(7, 44) * 1000);
  }
 
  private static FeedMessage.Builder createFeed() {
    FeedMessage.Builder builder = FeedMessage.newBuilder();
    FeedHeader.Builder header = FeedHeader.newBuilder();
    header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
    builder.setHeader(header);
    return builder;
  }

  private FeedEntity createTripUpdate(String tripId, String stopId, int delay,
      boolean arrival) {

    TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();

    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
    if (stopId != null)
      stopTimeUpdate.setStopId(stopId);
    StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
    stopTimeEvent.setDelay(delay);
    if (arrival) {
      stopTimeUpdate.setArrival(stopTimeEvent);
    } else {
      stopTimeUpdate.setDeparture(stopTimeEvent);
    }

    tripUpdate.addStopTimeUpdate(stopTimeUpdate);

    TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
    tripDescriptor.setTripId(tripId);
    tripUpdate.setTrip(tripDescriptor);

    FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
    tripUpdateEntity.setId(tripId);
    tripUpdateEntity.setTripUpdate(tripUpdate);
    return tripUpdateEntity.build();
  }
  
  private static FeedEntity feed(TripUpdate.Builder tripUpdate) {
    FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
    tripUpdateEntity.setId(tripUpdate.getTrip().getTripId());
    tripUpdateEntity.setTripUpdate(tripUpdate);
    return tripUpdateEntity.build();
  }
  
  private static StopTimeUpdate.Builder stopTimeUpdateWithDepartureDelay(String stopId, int delay) {
    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
    stopTimeUpdate.setStopId(stopId);
    StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
    stopTimeEvent.setDelay(delay);
    stopTimeUpdate.setDeparture(stopTimeEvent);
    return stopTimeUpdate;
  }

  private static GtfsRealtime.VehicleDescriptor vehicle(String id) {
    return GtfsRealtime.VehicleDescriptor.newBuilder().setId(id).build();
  }
  
  private static TripUpdate.Builder tripUpdate(String tripId, long timestamp, int delay,
      StopTimeUpdate.Builder... stopTimeUpdates) {
    TripUpdate.Builder tu = TripUpdate.newBuilder()
        .setTrip(TripDescriptor.newBuilder().setTripId(tripId))
        .setDelay(delay)
        .setTimestamp(timestamp);
    
    for (StopTimeUpdate.Builder stu : stopTimeUpdates) {
        tu.addStopTimeUpdate(stu);
    }
    
    return tu;
  }
  
  private VehicleLocationRecord vehicleLocationRecord(TripUpdate.Builder... tripUpdates) {
    FeedMessage.Builder TU = createFeed();

    for (TripUpdate.Builder tu : tripUpdates) {
      TU.addEntity(feed(tu));
    }

    FeedMessage.Builder VP = createFeed();
    
    List<CombinedTripUpdatesAndVehiclePosition> updates = 
        _library.groupTripUpdatesAndVehiclePositions(TU.build(), VP.build());

    CombinedTripUpdatesAndVehiclePosition update = updates.get(0);
    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    
    return record;
  }
  
  private static long getPredictedDepartureTimeByStopId(VehicleLocationRecord record, String stopId) {
    return getPredictedDepartureTimeByStopIdAndTripId(record, stopId, null);
  }

  private static long getPredictedDepartureTimeByStopIdAndTripId(VehicleLocationRecord record, String stopId, String tripId) {
    for (TimepointPredictionRecord tpr : record.getTimepointPredictions()) {
      if (tpr.getTimepointId().getId().equals(stopId)) {
        if (tripId == null || tpr.getTripId().getId().equals(tripId)) {
          return tpr.getTimepointPredictedDepartureTime();
        }
      }
    }
    return -1;
  }
}
