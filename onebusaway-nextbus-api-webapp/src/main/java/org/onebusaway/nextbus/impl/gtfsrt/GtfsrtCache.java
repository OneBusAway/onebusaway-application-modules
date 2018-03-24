/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.gtfsrt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class GtfsrtCache {

    Cache<CacheKey, FeedMessage> _cache;

    public enum CacheKey{
        TRIP_UPDATES,
        VEHICLE_POSITIONS,
        ALERTS
    }

    @PostConstruct
    public void setup(){
        _cache = CacheBuilder.newBuilder().expireAfterWrite(10,
                TimeUnit.SECONDS).build();

    }

    public void putTripUpdates(FeedMessage feedMessage){
        _cache.put(CacheKey.TRIP_UPDATES, feedMessage);
    }

    public void putVehiclePositions(FeedMessage feedMessage){
        _cache.put(CacheKey.VEHICLE_POSITIONS, feedMessage);
    }

    public void putAlerts(FeedMessage feedMessage){
        _cache.put(CacheKey.ALERTS, feedMessage);
    }

    public FeedMessage getTripUpdates(){
        return _cache.getIfPresent(CacheKey.TRIP_UPDATES);
    }

    public FeedMessage getVehiclePositions(){
        return _cache.getIfPresent(CacheKey.VEHICLE_POSITIONS);
    }

    public FeedMessage getAlerts(){
        return _cache.getIfPresent(CacheKey.ALERTS);
    }


}
