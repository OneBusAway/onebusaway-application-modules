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
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheRecord;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationRecordCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class VehicleLocationRecordCacheImpl implements VehicleLocationRecordCache {

  private static Logger _log = LoggerFactory.getLogger(VehicleLocationRecordCacheImpl.class);

  private ConcurrentMap<BlockLocationRecordKey, VehicleLocationCacheRecord> _recordsByKey = new ConcurrentHashMap<BlockLocationRecordKey, VehicleLocationCacheRecord>();

  private ConcurrentMap<AgencyAndId, BlockLocationRecordKey> _keyByVehicleId = new ConcurrentHashMap<AgencyAndId, BlockLocationRecordKey>();

  private ConcurrentMap<BlockInstance, List<BlockLocationRecordKey>> _keysByBlockInstance = new ConcurrentHashMap<BlockInstance, List<BlockLocationRecordKey>>();

  /**
   * By default, we keep around 20 minutes of cache entries
   */
  private int _blockLocationRecordCacheWindowSize = 20 * 60;

  private int _cacheEvictionFrequency = 1;

  private ScheduledExecutorService _executor;

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
    _executor = Executors.newScheduledThreadPool(1);
    _evictionHandler = _executor.scheduleAtFixedRate(
        new CacheEvictionHandler(), _cacheEvictionFrequency,
        _cacheEvictionFrequency, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void stop() {
    if (_evictionHandler != null)
      _evictionHandler.cancel(true);
    if (_executor != null)
      _executor.shutdownNow();
  }

  /****
   * {@link VehicleLocationRecordCache} Interface
   ****/

  @Override
  public VehicleLocationCacheRecord getRecordForVehicleId(AgencyAndId vehicleId) {

    return getRecordFromMap(_keyByVehicleId, vehicleId);
  }

  @Override
  public List<VehicleLocationCacheRecord> getRecordsForBlockInstance(
      BlockInstance blockInstance) {

    return getRecordsFromMap(_keysByBlockInstance, blockInstance);
  }

  @Override
  public void addRecord(BlockInstance blockInstance,
      VehicleLocationRecord record) {

    AgencyAndId vehicleId = record.getVehicleId();

    BlockLocationRecordKey key = new BlockLocationRecordKey(blockInstance,
        vehicleId);

    VehicleLocationCacheRecord existingRecord = _recordsByKey.put(key,
        new VehicleLocationCacheRecord(blockInstance, record));

    if (existingRecord == null) {

      addKeyToMap(_keysByBlockInstance, blockInstance, key);
      _keyByVehicleId.put(key.getVehicleId(), key);
    }
  }

  @Override
  public void clearRecordsForVehicleId(AgencyAndId vehicleId) {

    BlockLocationRecordKey key = _keyByVehicleId.remove(vehicleId);

    if (key != null)
      removeRecordsForKey(key, true);
  }

  public void clearStaleRecords(long time) {
    Iterator<Entry<BlockLocationRecordKey, VehicleLocationCacheRecord>> it = _recordsByKey.entrySet().iterator();
    while (it.hasNext()) {
      Entry<BlockLocationRecordKey, VehicleLocationCacheRecord> entry = it.next();
      BlockLocationRecordKey key = entry.getKey();
      VehicleLocationCacheRecord value = entry.getValue();
      if (value.getMeasuredLastUpdateTime() < time) {
        if (_log.isDebugEnabled())
          _log.debug("pruning block location record cache for vehicle="
              + key.getVehicleId() + " block=" + key.getBlockInstance());
        it.remove();
        removeRecordsForKey(key, false);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private <K> VehicleLocationCacheRecord getRecordFromMap(
      ConcurrentMap<K, BlockLocationRecordKey> map, K subKey) {

    BlockLocationRecordKey key = map.get(subKey);

    if (key != null)
      return _recordsByKey.get(key);

    return null;
  }

  private <K> List<VehicleLocationCacheRecord> getRecordsFromMap(
      ConcurrentMap<K, List<BlockLocationRecordKey>> map, K subKey) {

    List<VehicleLocationCacheRecord> allRecords = new ArrayList<VehicleLocationCacheRecord>();
    List<BlockLocationRecordKey> keys = map.get(subKey);

    if (keys != null) {
      for (BlockLocationRecordKey key : keys) {
        VehicleLocationCacheRecord r = _recordsByKey.get(key);
        if (r != null)
          allRecords.add(r);
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
    _keyByVehicleId.remove(key.getVehicleId(), key);
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
