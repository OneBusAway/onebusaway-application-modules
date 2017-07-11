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
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.VehicleOccupancyRecordCacheImpl;

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
        assertNull(cache.getRecordForVehicleId(vehicle));

        VehicleOccupancyRecord record1 = new VehicleOccupancyRecord();
        cache.addRecord(record1);
        // if we add a record without a vehicleId its rejected
        assertNull(cache.getRecordForVehicleId(vehicle));
        record1.setVehicleId(vehicle);
        cache.addRecord(record1);

        assertEquals(record1, cache.getRecordForVehicleId(vehicle));

        sleep(CACHE_TIMEOUT * 1000);
        // cache has expired!
        assertNull(cache.getRecordForVehicleId(vehicle));

        // add it back
        cache.addRecord(record1);
        assertEquals(record1, cache.getRecordForVehicleId(vehicle));
        // then clear it
        assertTrue(cache.clearRecord(vehicle));
        assertNull(cache.getRecordForVehicleId(vehicle));
    }

}
