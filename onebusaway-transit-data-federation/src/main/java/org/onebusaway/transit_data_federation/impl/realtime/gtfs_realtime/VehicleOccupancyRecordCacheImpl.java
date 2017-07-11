/**
 * Copyright (C) 2017 Metropolitan Transportation Authority
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of cache for APC data.
 *
 * We only keep the last record -- and we carefully expire that record after
 * _cacheTimeoutSeconds.
 */
@Component
public class VehicleOccupancyRecordCacheImpl implements VehicleOccupancyRecordCache {

    private static int DEFAULT_CACHE_TIMEOUT_SECONDS = 300;
    private int _cacheTimeoutSeconds = DEFAULT_CACHE_TIMEOUT_SECONDS;
    private Cache<AgencyAndId, VehicleOccupancyRecord> _cache;

    /**
     * set when records should expire.  A reasonable default is preset,
     * so set this only if tuning is desired.
     * @param timeoutInSeconds
     */
    public void setCacheTimeoutSeconds(int timeoutInSeconds) {
        if (timeoutInSeconds <= 0) {
            throw new UnsupportedOperationException("Non-expiring cache not supported");
        }
        _cacheTimeoutSeconds = timeoutInSeconds;
    }

    @Override
    public void addRecord(VehicleOccupancyRecord vehicleOccupancyRecord) {
        if (vehicleOccupancyRecord == null || vehicleOccupancyRecord.getVehicleId() == null) {
            // protect the cache against nulls
            return;
        }
        getCache().put(vehicleOccupancyRecord.getVehicleId(), vehicleOccupancyRecord);
    }

    @Override
    public VehicleOccupancyRecord getRecordForVehicleId(AgencyAndId vehicleId) {
        return getCache().getIfPresent(vehicleId);
    }

    @Override
    public boolean clearRecord(AgencyAndId vehicleId) {
        boolean found = getCache().getIfPresent(vehicleId) != null;
        getCache().invalidate(vehicleId);
        return found;
    }

    private Cache<AgencyAndId, VehicleOccupancyRecord> getCache() {
        if (_cache == null) {
            _cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(_cacheTimeoutSeconds, TimeUnit.SECONDS).build();
        }
        return _cache;
    }

}
