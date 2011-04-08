package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.collections.ConcurrentCollectionsLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationSamples;
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

  private ConcurrentMap<AgencyAndId, VehicleLocationCacheRecord> _recordsByVehicleId = new ConcurrentHashMap<AgencyAndId, VehicleLocationCacheRecord>();

  private ConcurrentMap<BlockInstance, Set<AgencyAndId>> _vehicleIdsByBlockInstance = new ConcurrentHashMap<BlockInstance, Set<AgencyAndId>>();

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
    return _recordsByVehicleId.get(vehicleId);
  }

  @Override
  public List<VehicleLocationCacheRecord> getRecordsForBlockInstance(
      BlockInstance blockInstance) {

    Set<AgencyAndId> vehicleIds = _vehicleIdsByBlockInstance.get(blockInstance);

    List<VehicleLocationCacheRecord> records = new ArrayList<VehicleLocationCacheRecord>();
    if (vehicleIds != null) {
      for (AgencyAndId vehicleId : vehicleIds) {
        VehicleLocationCacheRecord record = _recordsByVehicleId.get(vehicleId);

        if (record != null && record.getBlockInstance().equals(blockInstance))
          records.add(record);
      }
    }

    return records;
  }

  @Override
  public void addRecord(BlockInstance blockInstance,
      VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation,
      ScheduleDeviationSamples samples) {

    AgencyAndId vehicleId = record.getVehicleId();

    VehicleLocationCacheRecord cacheRecord = new VehicleLocationCacheRecord(
        blockInstance, record, scheduledBlockLocation, samples);
    VehicleLocationCacheRecord existing = _recordsByVehicleId.put(vehicleId,
        cacheRecord);

    if (existing != null) {
      BlockInstance existingBlockInstance = existing.getBlockInstance();
      if (!blockInstance.equals(existingBlockInstance))
        ConcurrentCollectionsLibrary.removeFromMapValueSet(
            _vehicleIdsByBlockInstance, existingBlockInstance, vehicleId);
    }

    // Ensure the block => vehicle mapping is set
    ConcurrentCollectionsLibrary.addToMapValueSet(_vehicleIdsByBlockInstance,
        blockInstance, vehicleId);
  }

  @Override
  public void clearRecordsForVehicleId(AgencyAndId vehicleId) {

    VehicleLocationCacheRecord record = _recordsByVehicleId.remove(vehicleId);

    if (record != null) {
      ConcurrentCollectionsLibrary.removeFromMapValueSet(
          _vehicleIdsByBlockInstance, record.getBlockInstance(), vehicleId);
    }
  }

  public void clearStaleRecords(long time) {
    Iterator<Entry<AgencyAndId, VehicleLocationCacheRecord>> it = _recordsByVehicleId.entrySet().iterator();
    while (it.hasNext()) {
      Entry<AgencyAndId, VehicleLocationCacheRecord> entry = it.next();
      AgencyAndId vehicleId = entry.getKey();
      VehicleLocationCacheRecord value = entry.getValue();
      if (value.getMeasuredLastUpdateTime() < time) {
        if (_log.isDebugEnabled())
          _log.debug("pruning block location record cache for vehicle="
              + vehicleId + " block=" + value.getBlockInstance());
        it.remove();
        ConcurrentCollectionsLibrary.removeFromMapValueSet(
            _vehicleIdsByBlockInstance, value.getBlockInstance(), vehicleId);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private class CacheEvictionHandler implements Runnable {

    @Override
    public void run() {
      clearStaleRecords(System.currentTimeMillis()
          - _blockLocationRecordCacheWindowSize * 1000);
    }
  }

}
