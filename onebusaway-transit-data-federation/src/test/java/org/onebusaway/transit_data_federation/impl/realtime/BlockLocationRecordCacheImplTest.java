package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.onebusaway.transit_data_federation.testing.MockEntryFactory.aid;

import java.util.List;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationRecord.Builder;
import org.onebusaway.transit_data_federation.model.ServiceDateAndId;

public class BlockLocationRecordCacheImplTest {

  @Test
  public void testSimpleOperations() {
    long serviceDate = System.currentTimeMillis();

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    List<BlockLocationRecordCollection> records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    cache.addRecord(record(20, "blockA", serviceDate, "vehicleA", 10.0));

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(1, records.size());
    BlockLocationRecordCollection collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(20, collection.getToTime());
    assertEquals(new ServiceDateAndId(serviceDate, aid("blockA")),
        collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());

    cache.addRecord(record(30, "blockA", serviceDate, "vehicleA", 20.0));

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(30, collection.getToTime());
    assertEquals(new ServiceDateAndId(serviceDate, aid("blockA")),
        collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());

    cache.addRecord(record(40, "blockA", serviceDate, "vehicleB", 5.0));

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(2, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(20, collection.getFromTime());
    assertEquals(30, collection.getToTime());
    assertEquals(new ServiceDateAndId(serviceDate, aid("blockA")),
        collection.getBlockInstance());
    assertEquals(aid("vehicleA"), collection.getVehicleId());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(1, records.size());
    collection = records.get(0);
    assertEquals(40, collection.getFromTime());
    assertEquals(40, collection.getToTime());
    assertEquals(new ServiceDateAndId(serviceDate, aid("blockA")),
        collection.getBlockInstance());
    assertEquals(aid("vehicleB"), collection.getVehicleId());

    cache.clearRecordsForVehicleId(aid("vehicleA"));

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(1, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(1, records.size());
  }

  @Test
  public void testClearCache() {

    long serviceDate = System.currentTimeMillis();

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(20);

    cache.addRecord(record(20, "blockA", serviceDate, "vehicleA", 10.0));
    cache.addRecord(record(30, "blockB", serviceDate, "vehicleB", 20.0));
    cache.addRecord(record(40, "blockA", serviceDate, "vehicleC", 20.0));
    cache.addRecord(record(50, "blockB", serviceDate, "vehicleD", 20.0));

    cache.clearStaleRecords(35);

    List<BlockLocationRecordCollection> records = cache.getRecordsForVehicleId(aid("vehicleA"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleB"));
    assertEquals(0, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleC"));
    assertEquals(1, records.size());

    records = cache.getRecordsForVehicleId(aid("vehicleD"));
    assertEquals(1, records.size());

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockA")));
    assertEquals(1, records.size());

    records = cache.getRecordsForBlockInstance(new ServiceDateAndId(
        serviceDate, aid("blockB")));
    assertEquals(1, records.size());
  }

  @Test
  public void testConcurrentOperations() {

    BlockLocationRecordCacheImpl cache = new BlockLocationRecordCacheImpl();
    cache.setBlockLocationRecordCacheWindowSize(2);

    long serviceDate = System.currentTimeMillis();
    int vid = 0;

    for (int i = 0; i < 20; i++) {

      AgencyAndId blockId = new AgencyAndId("1", Integer.toString(i));
      ServiceDateAndId blockInstance = new ServiceDateAndId(serviceDate,
          blockId);

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

    private ServiceDateAndId _blockInstance;

    private AgencyAndId _vehicleId;

    private BlockLocationRecordCacheImpl _cache;

    public RecordSource(ServiceDateAndId blockInstance, AgencyAndId vehicleId,
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
        b.setBlockId(_blockInstance.getId());
        b.setServiceDate(_blockInstance.getServiceDate());
        b.setVehicleId(_vehicleId);
        b.setDistanceAlongBlock(i * 100);
        b.setTime(i * 1000);

        _cache.addRecord(b.create());

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
