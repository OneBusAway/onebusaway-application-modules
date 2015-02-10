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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtimeConstants;

public class GtfsRealtimeTripLibraryTest {

  private GtfsRealtimeTripLibrary _library;
  private GtfsRealtimeEntitySource _entitySource;
  private BlockCalendarService _blockCalendarService;

  @Before
  public void before() {
    _library = new GtfsRealtimeTripLibrary();
    _library.setCurrentTime(8 * 60 * 60 * 1000);
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
    assertSame(blockA, group.block.getBlockEntry());
    assertEquals(2, group.tripUpdates.size());
    TripUpdate tripUpdate = group.tripUpdates.get(0);
    assertEquals("tripA", tripUpdate.getTrip().getTripId());
    tripUpdate = group.tripUpdates.get(1);
    assertEquals("tripB", tripUpdate.getTrip().getTripId());

    group = groups.get(1);
    assertSame(blockB, group.block.getBlockEntry());
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
    update.block.setBlockEntry(blockA);
    update.tripUpdates = Arrays.asList(tripUpdate);

    VehicleLocationRecord record = _library.createVehicleLocationRecordForUpdate(update);
    assertEquals(123456789000L, record.getTimeOfRecord());
    assertEquals(120, record.getScheduleDeviation(), 0.0);
  }

  private FeedMessage.Builder createFeed() {
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
}
