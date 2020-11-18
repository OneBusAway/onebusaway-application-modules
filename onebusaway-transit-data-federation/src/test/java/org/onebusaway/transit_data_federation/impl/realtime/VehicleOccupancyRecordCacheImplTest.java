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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data_federation.impl.realtime.apc.VehicleOccupancyRecordCacheImpl;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the cache expires properly
 */
public class VehicleOccupancyRecordCacheImplTest {


    @Test
    public void testExpire() throws InterruptedException {
        final int CACHE_TIMEOUT = 2;
        VehicleOccupancyRecordCacheImpl cache = new VehicleOccupancyRecordCacheImpl();

        try {
            cache.setCacheTimeoutSeconds(0);
            fail("expected unsupported operation");

        } catch (UnsupportedOperationException uoe) {
            // success
        }

        cache.setCacheTimeoutSeconds(CACHE_TIMEOUT);

        AgencyAndId vehicle = new AgencyAndId("ACTA", "1111");
        String routeId = "a1";
        String directionId = null;
        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));

        VehicleOccupancyRecord record1 = new VehicleOccupancyRecord();
        cache.addRecord(record1);
        // if we add a record without a vehicleId its rejected
        assertNull(cache.getLastRecordForVehicleId(vehicle));
        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));
        record1.setVehicleId(vehicle);
        cache.addRecord(record1);



        assertEquals(record1, cache.getLastRecordForVehicleId(vehicle));
        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));

        // direction should be optional
        record1.setRouteId(routeId);
        cache.addRecord(record1);
        assertEquals(record1, cache.getLastRecordForVehicleId(vehicle));
        assertEquals(record1, cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));

        // but used if present
        directionId = "0";
        record1.setDirectionId("1");
        cache.addRecord(record1);
        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));
        directionId = "1";
        assertEquals(record1, cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));

        sleep(CACHE_TIMEOUT * 1000);
        // cache has expired!
        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));
        assertNull(cache.getLastRecordForVehicleId(vehicle));

        // add it back
        cache.addRecord(record1);
        assertEquals(record1, cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));
        assertEquals(record1, cache.getLastRecordForVehicleId(vehicle));
        // then clear it
        assertTrue(cache.clearRecordForVehicle(vehicle));
        assertEquals(record1, cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));

        // clear it in route cache
        assertTrue(cache.clearRecord(record1));

        assertNull(cache.getRecordForVehicleIdAndRoute(vehicle, routeId, directionId));
        assertNull(cache.getLastRecordForVehicleId(vehicle));
    }

}
