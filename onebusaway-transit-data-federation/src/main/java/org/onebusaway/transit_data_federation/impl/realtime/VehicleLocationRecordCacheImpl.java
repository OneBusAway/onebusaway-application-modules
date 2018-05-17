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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheElements;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationCacheEntry;
import org.onebusaway.transit_data_federation.services.realtime.VehicleLocationRecordCache;
import org.onebusaway.util.SystemTime;
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
public class VehicleLocationRecordCacheImpl implements VehicleLocationRecordCache {

  private static Logger _log = LoggerFactory.getLogger(VehicleLocationRecordCacheImpl.class);

  private ConcurrentMap<AgencyAndId, VehicleLocationCacheEntry> _entriesByVehicleId = new ConcurrentHashMap<AgencyAndId, VehicleLocationCacheEntry>();

  private ConcurrentMap<BlockInstance, Set<AgencyAndId>> _vehicleIdsByBlockInstance = new ConcurrentHashMap<BlockInstance, Set<AgencyAndId>>();

  private Map<AgencyAndId, VehicleLocationRecord> rawPositionMap = new HashMap<>();

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
  public void addRawPosition(AgencyAndId vehicleId, VehicleLocationRecord point) {
    rawPositionMap.put(vehicleId, point);
  }

  @Override
  public VehicleLocationRecord getRawPosition(AgencyAndId vehicleId) {
    return rawPositionMap.get(vehicleId);
  }


  @Override
  public VehicleLocationCacheElements getRecordForVehicleId(
      AgencyAndId vehicleId) {
    VehicleLocationCacheEntry entry = _entriesByVehicleId.get(vehicleId);
    if (entry == null)
      return null;
    return entry.getElements();
  }

  @Override
  public List<VehicleLocationCacheElements> getRecordsForBlockInstance(
      BlockInstance blockInstance) {

    Set<AgencyAndId> vehicleIds = _vehicleIdsByBlockInstance.get(blockInstance);

    List<VehicleLocationCacheElements> records = new ArrayList<VehicleLocationCacheElements>();
    if (vehicleIds != null) {
      for (AgencyAndId vehicleId : vehicleIds) {
        VehicleLocationCacheEntry record = _entriesByVehicleId.get(vehicleId);

        if (record != null && record.getBlockInstance().equals(blockInstance))
          records.add(record.getElements());
      }
    }

    return records;
  }

  @Override
  public VehicleLocationCacheElements addRecord(BlockInstance blockInstance,
      VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation,
      ScheduleDeviationSamples samples) {

    AgencyAndId vehicleId = record.getVehicleId();

    while (true) {

      VehicleLocationCacheEntry newCacheEntry = new VehicleLocationCacheEntry(
          blockInstance);

      VehicleLocationCacheEntry cacheEntry = _entriesByVehicleId.putIfAbsent(
          vehicleId, newCacheEntry);

      if (cacheEntry == null) {

        cacheEntry = newCacheEntry;

        /**
         * Since we're adding a new entry, we indicate the connection between
         * this block instance and vehicleId
         */
        ConcurrentCollectionsLibrary.addToMapValueSet(
            _vehicleIdsByBlockInstance, blockInstance, vehicleId);
      }

      /**
       * If the block instance of a vehicle has changed mid-stream, we close off
       * the cache entry and remove the block=>vid mapping
       */
      if (cacheEntry.isClosedBecauseBlockInstanceChanged(blockInstance)) {

        _entriesByVehicleId.remove(vehicleId);

        ConcurrentCollectionsLibrary.removeFromMapValueSet(
            _vehicleIdsByBlockInstance, cacheEntry.getBlockInstance(),
            vehicleId);

        continue;
      }

      /**
       * If the element failed to add because the entry is closed, we loop.
       * Someone closed the entry while we were in the process of requesting it
       * from the map. On the next loop, it should no longer be in the map.
       */
      if (!cacheEntry.addElement(record, scheduledBlockLocation, samples))
        continue;

      BlockInstance existingBlockInstance = cacheEntry.getBlockInstance();
      if (!blockInstance.equals(existingBlockInstance))
        ConcurrentCollectionsLibrary.removeFromMapValueSet(
            _vehicleIdsByBlockInstance, existingBlockInstance, vehicleId);

      // Ensure the block => vehicle mapping is set

      return cacheEntry.getElements();

    }
  }

  @Override
  public void clearRecordsForVehicleId(AgencyAndId vehicleId) {

    VehicleLocationCacheEntry record = _entriesByVehicleId.remove(vehicleId);

    if (record != null) {
      ConcurrentCollectionsLibrary.removeFromMapValueSet(
          _vehicleIdsByBlockInstance, record.getBlockInstance(), vehicleId);
    }
  }

  public void clearStaleRecords(long time) {

    Iterator<Entry<AgencyAndId, VehicleLocationCacheEntry>> it = _entriesByVehicleId.entrySet().iterator();

    while (it.hasNext()) {

      Entry<AgencyAndId, VehicleLocationCacheEntry> entry = it.next();
      AgencyAndId vehicleId = entry.getKey();
      VehicleLocationCacheEntry cacheEntry = entry.getValue();

      if (cacheEntry.closeIfStale(time)) {

        if (_log.isDebugEnabled())
          _log.debug("pruning block location record cache for vehicle="
              + vehicleId + " block=" + cacheEntry.getBlockInstance());
        it.remove();
        ConcurrentCollectionsLibrary.removeFromMapValueSet(
            _vehicleIdsByBlockInstance, cacheEntry.getBlockInstance(),
            vehicleId);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private class CacheEvictionHandler implements Runnable {

    @Override
    public void run() {
      clearStaleRecords(SystemTime.currentTimeMillis()
          - _blockLocationRecordCacheWindowSize * 1000);
    }
  }

}
