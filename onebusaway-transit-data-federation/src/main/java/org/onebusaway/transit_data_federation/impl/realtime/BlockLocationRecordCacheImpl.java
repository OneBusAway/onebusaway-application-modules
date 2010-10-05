package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationRecordCache;
import org.springframework.stereotype.Component;

/**
 * The general assumption is that we won't get back to back updates from the
 * same vehicle on a short enough time-scale that it can cause serious race
 * condition issues. If we DO get lots of back to back updates, we'll just have
 * to assume some amount of data loss.
 * 
 * @param record record to add
 */
@Component
class BlockLocationRecordCacheImpl implements BlockLocationRecordCache {

  private ConcurrentMap<BlockLocationRecordKey, BlockLocationRecordCollection> _recordsByKey = new ConcurrentHashMap<BlockLocationRecordKey, BlockLocationRecordCollection>();

  private ConcurrentMap<AgencyAndId, List<BlockLocationRecordKey>> _keysByVehicleId = new ConcurrentHashMap<AgencyAndId, List<BlockLocationRecordKey>>();

  private ConcurrentMap<BlockInstance, List<BlockLocationRecordKey>> _keysByBlockInstance = new ConcurrentHashMap<BlockInstance, List<BlockLocationRecordKey>>();

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  private int _cacheEvictionFrequency = 1;

  private ScheduledFuture<?> _evictionHandler;

  /**
   * Controls how far back in time we include records in the
   * {@link BlockLocationRecordCollection} for each active trip.
   * 
   * @param windowSize in seconds
   */
  public void setBlockLocationRecordCacheWindowSize(int windowSize) {
    _blockLocationRecordCacheWindowSize = windowSize;
  }

  /**
   * 
   * @param cacheEvictionFrequency frequency, in minutes
   */
  public void setCacheEvictionFrequency(int cacheEvictionFrequency) {
    _cacheEvictionFrequency = cacheEvictionFrequency;
  }

  @PostConstruct
  public void start() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    _evictionHandler = executor.scheduleAtFixedRate(new CacheEvictionHandler(),
        _cacheEvictionFrequency, _cacheEvictionFrequency, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void stop() {
    if (_evictionHandler != null)
      _evictionHandler.cancel(true);
  }

  /****
   * {@link BlockLocationRecordCache} Interface
   ****/

  @Override
  public List<BlockLocationRecordCollection> getRecordsForVehicleId(
      AgencyAndId vehicleId) {

    return getRecordsFromMap(_keysByVehicleId, vehicleId);
  }

  @Override
  public List<BlockLocationRecordCollection> getRecordsForBlockInstance(
      BlockInstance blockInstance) {

    return getRecordsFromMap(_keysByBlockInstance, blockInstance);
  }

  @Override
  public void addRecord(BlockInstance blockInstance, BlockLocationRecord record) {

    AgencyAndId vehicleId = record.getVehicleId();

    BlockLocationRecordKey key = new BlockLocationRecordKey(blockInstance,
        vehicleId);

    BlockLocationRecordCollection records = _recordsByKey.get(key);

    if (records == null) {
      BlockLocationRecordCollection newRecords = BlockLocationRecordCollection.createFromRecords(
          blockInstance, Arrays.asList(record));
      records = _recordsByKey.putIfAbsent(key, newRecords);

      // If this was a new record, add its key to the various indices
      if (records == null) {
        addKeyToMap(_keysByVehicleId, vehicleId, key);
        addKeyToMap(_keysByBlockInstance, blockInstance, key);
      }
    }

    if (records != null) {
      records = records.addRecord(blockInstance, record,
          _blockLocationRecordCacheWindowSize * 1000);
      _recordsByKey.put(key, records);
    }
  }

  @Override
  public void clearRecordsForVehicleId(AgencyAndId vehicleId) {

    List<BlockLocationRecordKey> keysForVehicleId = _keysByVehicleId.remove(vehicleId);

    if (keysForVehicleId != null) {
      List<BlockLocationRecordKey> keys = new ArrayList<BlockLocationRecordKey>(
          keysForVehicleId);
      for (BlockLocationRecordKey key : keys)
        removeRecordsForKey(key, true);
    }
  }

  public void clearStaleRecords(long time) {
    Iterator<Entry<BlockLocationRecordKey, BlockLocationRecordCollection>> it = _recordsByKey.entrySet().iterator();
    while (it.hasNext()) {
      Entry<BlockLocationRecordKey, BlockLocationRecordCollection> entry = it.next();
      BlockLocationRecordKey key = entry.getKey();
      BlockLocationRecordCollection value = entry.getValue();
      if (value.getToTime() < time) {
        it.remove();
        removeRecordsForKey(key, false);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private <K> List<BlockLocationRecordCollection> getRecordsFromMap(
      ConcurrentMap<K, List<BlockLocationRecordKey>> map, K subKey) {

    List<BlockLocationRecordCollection> allRecords = new ArrayList<BlockLocationRecordCollection>();
    List<BlockLocationRecordKey> keys = map.get(subKey);

    if (keys != null) {
      for (BlockLocationRecordKey key : keys) {
        BlockLocationRecordCollection records = _recordsByKey.get(key);
        if (records != null)
          allRecords.add(records);
      }
    }

    return allRecords;
  }

  private <K> void addKeyToMap(
      ConcurrentMap<K, List<BlockLocationRecordKey>> map, K subKey,
      BlockLocationRecordKey key) {

    while (true) {

      List<BlockLocationRecordKey> keys = map.get(subKey);

      if (keys == null) {
        List<BlockLocationRecordKey> newKeys = Arrays.asList(key);
        keys = map.putIfAbsent(subKey, newKeys);
        if (keys == null)
          return;
      }

      List<BlockLocationRecordKey> origCopy = new ArrayList<BlockLocationRecordKey>(
          keys);

      if (origCopy.contains(key))
        return;

      List<BlockLocationRecordKey> extendedCopy = new ArrayList<BlockLocationRecordKey>(
          origCopy);
      extendedCopy.add(key);

      if (map.replace(subKey, origCopy, extendedCopy))
        return;
    }
  }

  private void removeRecordsForKey(BlockLocationRecordKey key,
      boolean removeRecords) {

    if (removeRecords)
      _recordsByKey.remove(key);

    removeKeyFromMap(_keysByBlockInstance, key, key.getBlockInstance());
    removeKeyFromMap(_keysByVehicleId, key, key.getVehicleId());
  }

  private <K> void removeKeyFromMap(
      ConcurrentMap<K, List<BlockLocationRecordKey>> map,
      BlockLocationRecordKey key, K subKey) {

    while (true) {

      List<BlockLocationRecordKey> keys = map.get(subKey);

      if (keys == null)
        return;

      List<BlockLocationRecordKey> origCopy = new ArrayList<BlockLocationRecordKey>(
          keys);

      if (!origCopy.contains(key))
        return;

      List<BlockLocationRecordKey> reducedCopy = new ArrayList<BlockLocationRecordKey>(
          origCopy);
      reducedCopy.remove(key);

      if (reducedCopy.isEmpty()) {
        if (map.remove(subKey, origCopy))
          return;
      } else {
        if (map.replace(subKey, origCopy, reducedCopy))
          return;
      }
    }
  }

  private class CacheEvictionHandler implements Runnable {

    @Override
    public void run() {
      clearStaleRecords(System.currentTimeMillis()
          - _blockLocationRecordCacheWindowSize * 1000);
    }
  }
}
