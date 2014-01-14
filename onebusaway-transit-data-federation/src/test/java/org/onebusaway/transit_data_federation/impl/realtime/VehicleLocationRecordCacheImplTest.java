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
package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.aid;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElements;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class VehicleLocationRecordCacheImplTest {

  @Test
  public void testSimpleOperations() {
    long serviceDate = System.currentTimeMillis();

    BlockEntryImpl block = block("blockA");
    TripEntryImpl trip = trip("tripA", "serviceId");
    stopTime(0, null, trip, time(9, 00), 0);
    BlockConfigurationEntry blockConfig = linkBlockTrips(block, trip);
    BlockInstance blockInstance = new BlockInstance(blockConfig, serviceDate);

    VehicleLocationRecordCacheImpl cache = new VehicleLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    List<VehicleLocationCacheElements> records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(0, records.size());

    VehicleLocationCacheElements cacheRecord = cache.getRecordForVehicleId(aid("vehicleA"));
    assertNull(cacheRecord);

    cache.addRecord(blockInstance,
        record(20, "blockA", serviceDate, "vehicleA", 10.0), null, null);

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());
    cacheRecord = records.get(0);
    VehicleLocationRecord record = cacheRecord.getLastElement().getRecord();
    assertEquals(20, record.getTimeOfRecord());
    assertEquals(blockInstance, cacheRecord.getBlockInstance());
    assertEquals(aid("vehicleA"), record.getVehicleId());

    VehicleLocationCacheElements cacheRecord2 = cache.getRecordForVehicleId(aid("vehicleA"));
    assertSame(cacheRecord, cacheRecord2);

    cache.addRecord(blockInstance,
        record(30, "blockA", serviceDate, "vehicleA", 20.0), null, null);

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());
    cacheRecord = records.get(0);
    record = cacheRecord.getLastElement().getRecord();
    assertEquals(30, record.getTimeOfRecord());
    assertEquals(blockInstance, cacheRecord.getBlockInstance());
    assertEquals(aid("vehicleA"), record.getVehicleId());

    cacheRecord2 = cache.getRecordForVehicleId(aid("vehicleA"));
    assertSame(cacheRecord, cacheRecord2);

    cache.addRecord(blockInstance,
        record(40, "blockA", serviceDate, "vehicleB", 5.0), null, null);

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(2, records.size());

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleA"));
    record = cacheRecord.getLastElement().getRecord();
    assertEquals(30, record.getTimeOfRecord());
    assertEquals(blockInstance, cacheRecord.getBlockInstance());
    assertEquals(aid("vehicleA"), record.getVehicleId());

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleB"));
    record = cacheRecord.getLastElement().getRecord();
    assertEquals(40, record.getTimeOfRecord());
    assertEquals(blockInstance, cacheRecord.getBlockInstance());
    assertEquals(aid("vehicleB"), record.getVehicleId());

    cache.clearRecordsForVehicleId(aid("vehicleA"));

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleA"));
    assertNull(cacheRecord);

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleB"));
    assertEquals(aid("vehicleB"),
        cacheRecord.getLastElement().getRecord().getVehicleId());
  }

  @Test
  public void testClearCache() throws InterruptedException {

    long serviceDate = System.currentTimeMillis();

    BlockEntryImpl blockA = block("blockA");
    TripEntryImpl tripA = trip("tripA", "serviceId");
    stopTime(0, null, tripA, time(9, 00), 0);
    BlockConfigurationEntry blockConfigA = linkBlockTrips(blockA, tripA);
    BlockInstance instanceA = new BlockInstance(blockConfigA, serviceDate);

    BlockEntryImpl blockB = block("blockB");
    TripEntryImpl tripB = trip("tripB", "serviceId");
    stopTime(0, null, tripB, time(9, 00), 0);
    BlockConfigurationEntry blockConfigB = linkBlockTrips(blockB, tripB);
    BlockInstance instanceB = new BlockInstance(blockConfigB, serviceDate);

    VehicleLocationRecordCacheImpl cache = new VehicleLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    cache.addRecord(instanceA,
        record(20, "blockA", serviceDate, "vehicleA", 10.0), null, null);

    Thread.sleep(100);

    cache.addRecord(instanceB,
        record(30, "blockB", serviceDate, "vehicleB", 20.0), null, null);

    Thread.sleep(100);

    cache.addRecord(instanceA,
        record(40, "blockA", serviceDate, "vehicleC", 20.0), null, null);

    Thread.sleep(100);

    cache.addRecord(instanceB,
        record(50, "blockB", serviceDate, "vehicleD", 20.0), null, null);

    cache.clearStaleRecords(System.currentTimeMillis() - 150);

    VehicleLocationCacheElements cacheRecord = cache.getRecordForVehicleId(aid("vehicleA"));
    assertNull(cacheRecord);

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleB"));
    assertNull(cacheRecord);

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleC"));
    assertEquals(aid("vehicleC"),
        cacheRecord.getLastElement().getRecord().getVehicleId());

    cacheRecord = cache.getRecordForVehicleId(aid("vehicleD"));
    assertEquals(aid("vehicleD"),
        cacheRecord.getLastElement().getRecord().getVehicleId());

    List<VehicleLocationCacheElements> records = cache.getRecordsForBlockInstance(instanceA);
    assertEquals(1, records.size());

    records = cache.getRecordsForBlockInstance(instanceB);
    assertEquals(1, records.size());
  }

  @Test
  public void testConcurrentOperations() {

    VehicleLocationRecordCacheImpl cache = new VehicleLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(2);

    long serviceDate = System.currentTimeMillis();
    int vid = 0;

    for (int i = 0; i < 20; i++) {

      BlockEntryImpl block = block(Integer.toString(i));
      TripEntryImpl trip = trip(Integer.toString(i), "serviceId");
      stopTime(0, null, trip, time(9, 00), 0);
      BlockConfigurationEntry blockConfig = linkBlockTrips(block, trip);
      BlockInstance blockInstance = new BlockInstance(blockConfig, serviceDate);

      for (int j = 0; j < 5; j++) {

        AgencyAndId vehicleId = new AgencyAndId("1", Integer.toString(vid++));

        RecordSource source = new RecordSource(blockInstance, vehicleId, cache);
        Thread thread = new Thread(source);
        thread.run();
      }
    }
  }

  private VehicleLocationRecord record(long t, String blockId,
      long serviceDate, String vehicleId, double distanceAlongBlock) {
    VehicleLocationRecord r = new VehicleLocationRecord();
    r.setBlockId(new AgencyAndId("1", blockId));
    r.setServiceDate(serviceDate);
    r.setVehicleId(new AgencyAndId("1", vehicleId));
    r.setDistanceAlongBlock(distanceAlongBlock);
    r.setTimeOfRecord(t);
    return r;
  }

  private static class RecordSource implements Runnable {

    private BlockInstance _blockInstance;

    private AgencyAndId _vehicleId;

    private VehicleLocationRecordCacheImpl _cache;

    public RecordSource(BlockInstance blockInstance, AgencyAndId vehicleId,
        VehicleLocationRecordCacheImpl cache) {
      _blockInstance = blockInstance;
      _vehicleId = vehicleId;
      _cache = cache;
    }

    @Override
    public void run() {

      for (int i = 0; i < 100; i++) {

        if (i % 10 == 0)
          _cache.clearRecordsForVehicleId(_vehicleId);

        VehicleLocationRecord r = new VehicleLocationRecord();
        r.setBlockId(_blockInstance.getBlock().getBlock().getId());
        r.setServiceDate(_blockInstance.getServiceDate());
        r.setVehicleId(_vehicleId);
        r.setDistanceAlongBlock(i * 100);
        r.setTimeOfRecord(i * 1000);

        _cache.addRecord(_blockInstance, r, null, null);

        List<VehicleLocationCacheElements> records = _cache.getRecordsForBlockInstance(_blockInstance);
        VehicleLocationCacheElements cacheRecord = getCollectionForVehicleId(records);
        if (cacheRecord == null)
          fail();

        VehicleLocationRecord record = cacheRecord.getLastElement().getRecord();

        assertEquals(i * 1000L, record.getTimeOfRecord());
        assertEquals(_blockInstance, cacheRecord.getBlockInstance());
        assertEquals(_vehicleId, record.getVehicleId());

        Thread.yield();
      }
    }

    private VehicleLocationCacheElements getCollectionForVehicleId(
        List<VehicleLocationCacheElements> records) {
      for (VehicleLocationCacheElements cacheRecord : records) {
        if (_vehicleId.equals(cacheRecord.getLastElement().getRecord().getVehicleId()))
          return cacheRecord;
      }
      return null;
    }
  }
}
