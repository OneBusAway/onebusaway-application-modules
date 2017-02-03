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
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationRecord.Builder;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class BlockLocationRecordCacheImplTest {

  @Test
  public void testSimpleOperations() {
    long serviceDate = System.currentTimeMillis();

    BlockEntryImpl block = block("blockA");
    TripEntryImpl trip = trip("tripA", "serviceId");
    stopTime(0, null, trip, time(9, 00), 0);
    BlockConfigurationEntry blockConfig = linkBlockTrips(block, trip);
    BlockInstance blockInstance = new BlockInstance(blockConfig, serviceDate);

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    List<BlockLocationRecordCollection> records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    cache.addRecord(blockInstance,
        record(20, "blockA", serviceDate, "vehicleA", 10.0));

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());
    BlockLocationRecordCollection collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(20, collection.getToTime());
    assertEquals(blockInstance, collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());

    cache.addRecord(blockInstance,
        record(30, "blockA", serviceDate, "vehicleA", 20.0));

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(30, collection.getToTime());
    assertEquals(blockInstance, collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());

    cache.addRecord(blockInstance,
        record(40, "blockA", serviceDate, "vehicleB", 5.0));

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(2, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(30, collection.getToTime());
    assertEquals(blockInstance, collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(40, collection.getFromTime());
    assertEquals(40, collection.getToTime());
    assertEquals(blockInstance, collection.getBlockInstance());
    assertEquals(aid("vehicleB"), collection.getVehicleId());

    cache.clearRecordsForVehicleId(aid("vehicleA"));

    records = cache.getRecordsForBlockInstance(blockInstance);
    assertEquals(1, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(1, records.size());
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

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    cache.addRecord(instanceA,
        record(20, "blockA", serviceDate, "vehicleA", 10.0));
    
    Thread.sleep(100);
    
    cache.addRecord(instanceB,
        record(30, "blockB", serviceDate, "vehicleB", 20.0));
    
    Thread.sleep(100);
    
    cache.addRecord(instanceA,
        record(40, "blockA", serviceDate, "vehicleC", 20.0));
    
    Thread.sleep(100);
    
    cache.addRecord(instanceB,
        record(50, "blockB", serviceDate, "vehicleD", 20.0));
    
    
    cache.clearStaleRecords(System.currentTimeMillis()-150);

    List<BlockLocationRecordCollection> records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleC"));
    assertEquals(1, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleD"));
    assertEquals(1, records.size());

    records = cache.getRecordsForBlockInstance(instanceA);
    assertEquals(1, records.size());

    records = cache.getRecordsForBlockInstance(instanceB);
    assertEquals(1, records.size());
  }

  @Test
  public void testConcurrentOperations() {

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
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

  private BlockLocationRecord record(long t, String blockId, long serviceDate,
      String vehicleId, double distanceAlongBlock) {
    Builder b = BlockLocationRecord.builder();
    b.setBlockId(new AgencyAndId("1", blockId));
    b.setServiceDate(serviceDate);
    b.setVehicleId(new AgencyAndId("1", vehicleId));
    b.setDistanceAlongBlock(distanceAlongBlock);
    b.setTime(t);
    return b.create();
  }

  private static class RecordSource implements Runnable {

    private BlockInstance _blockInstance;

    private AgencyAndId _vehicleId;

    private BlockLocationRecordCacheImpl _cache;

    public RecordSource(BlockInstance blockInstance, AgencyAndId vehicleId,
        BlockLocationRecordCacheImpl cache) {
      _blockInstance = blockInstance;
      _vehicleId = vehicleId;
      _cache = cache;
    }

    @Override
    public void run() {

      for (int i = 0; i < 100; i++) {

        if (i % 10 == 0)
          _cache.clearRecordsForVehicleId(_vehicleId);

        Builder b = BlockLocationRecord.builder();
        b.setBlockId(_blockInstance.getBlock().getBlock().getId());
        b.setServiceDate(_blockInstance.getServiceDate());
        b.setVehicleId(_vehicleId);
        b.setDistanceAlongBlock((double)i * 100);
        b.setTime(i * 1000);

        _cache.addRecord(_blockInstance, b.create());

        List<BlockLocationRecordCollection> records = _cache.getRecordsForBlockInstance(_blockInstance);
        BlockLocationRecordCollection collection = getCollectionForVehicleId(records);
        if (collection == null)
          fail();

        assertEquals(i * 1000L, collection.getToTime());
        assertEquals(_blockInstance, collection.getBlockInstance());
        assertEquals(_vehicleId, collection.getVehicleId());

        Thread.yield();
      }
    }

    private BlockLocationRecordCollection getCollectionForVehicleId(
        List<BlockLocationRecordCollection> records) {
      for (BlockLocationRecordCollection collection : records) {
        if (_vehicleId.equals(collection.getVehicleId()))
          return collection;
      }
      return null;
    }
  }
}
