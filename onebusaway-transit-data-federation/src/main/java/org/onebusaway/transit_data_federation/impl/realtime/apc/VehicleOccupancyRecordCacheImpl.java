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
package org.onebusaway.transit_data_federation.impl.realtime.apc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.commons.lang.StringUtils;
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

    private static int DEFAULT_CACHE_TIMEOUT_SECONDS = 6 * 60;
    private int _cacheTimeoutSeconds = DEFAULT_CACHE_TIMEOUT_SECONDS;
    private Cache<String, VehicleOccupancyRecord> _routeCache;
    private Cache<AgencyAndId, VehicleOccupancyRecord> _vehicleCache;

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

        getVehicleCache().put(vehicleOccupancyRecord.getVehicleId(), vehicleOccupancyRecord);

        // route is mandatory -- direction is optional
        if (StringUtils.isBlank(vehicleOccupancyRecord.getRouteId())) {
            return;
        }

        getRouteCache().put(hash(vehicleOccupancyRecord), vehicleOccupancyRecord);
    }

    public VehicleOccupancyRecord getLastRecordForVehicleId(AgencyAndId vehicleId) {
        return getVehicleCache().getIfPresent(vehicleId);
    }

    @Override
    public VehicleOccupancyRecord getRecordForVehicleIdAndRoute(AgencyAndId vehicleId, String routeId, String directionId) {
        return getRouteCache().getIfPresent(hash(vehicleId, routeId, directionId));
    }

    @Override
    public boolean clearRecordForVehicle(AgencyAndId vehicleId) {
        boolean found = getVehicleCache().getIfPresent(vehicleId) != null;
        getVehicleCache().invalidate(vehicleId);
        return found;
    }

    @Override
    public boolean clearRecord(VehicleOccupancyRecord vor) {
        String hash = hash(vor.getVehicleId(), vor.getRouteId(), vor.getDirectionId());
        boolean found = getRouteCache().getIfPresent(hash) != null;
        getRouteCache().invalidate(hash);
        return found;
    }

    private String hash(AgencyAndId vehicleId, String routeId, String directionId) {
        if (!routeId.contains("_")) { // ensure we use qualified route_id
            routeId = new AgencyAndId(vehicleId.getAgencyId(), routeId).toString();
        }
        return vehicleId.toString() + "." + routeId + "." + directionId;
    }

    private String hash(VehicleOccupancyRecord vor) {
        return hash(vor.getVehicleId(), vor.getRouteId(), vor.getDirectionId());
    }

    private Cache<String, VehicleOccupancyRecord> getRouteCache() {
        if (_routeCache == null) {
            _routeCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(_cacheTimeoutSeconds, TimeUnit.SECONDS).build();
        }
        return _routeCache;
    }

    private Cache<AgencyAndId, VehicleOccupancyRecord> getVehicleCache() {
        if (_vehicleCache == null) {
            _vehicleCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(_cacheTimeoutSeconds, TimeUnit.SECONDS).build();
        }
        return _vehicleCache;
    }

}
