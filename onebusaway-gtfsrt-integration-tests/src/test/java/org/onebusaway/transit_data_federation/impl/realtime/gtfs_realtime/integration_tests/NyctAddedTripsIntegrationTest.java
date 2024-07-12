/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.integration_tests;

import org.junit.Test;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NyctAddedTripsIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }
  @Test
  public void testAddedViaExtension() throws Exception {
    // verify some expected service to ensure we parsed feed properly
    // these are all ADDED trips via NYCT extension

    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_1","MTASBWY_2","MTASBWY_3");
    String expectedRouteId = "MTASBWY_3";
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.pb";

    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, null, path, name);
    long currentTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();
    expectArrivalAndTrip(currentTime, "MTASBWY_250N", "MTASBWY_3", "MTASBWY_040800_3..N01R", diffInMinutes(currentTime, 1683629924l));
    expectArrivalAndTrip(currentTime, "MTASBWY_250N", "MTASBWY_4", "MTASBWY_042150_4..N06R", diffInMinutes(currentTime, 1683630090l));
    expectArrivalAndTrip(currentTime, "MTASBWY_257N", "MTASBWY_3", "MTASBWY_042250_3..N01R", diffInMinutes(currentTime, 1683630150l));
    // this is a duplicate trip -- a bug in the feed!
    //expectArrivalAndTrip(currentTime, "MTASBWY_257N", "MTASBWY_3", "MTASBWY_042850_3..N01R", diffInMinutes(currentTime, 1683630150l));

    // check MonitoredResult for expected number of results
    assertFalse(source.getMonitoredResult().getAddedTripIds().isEmpty());

    assertEquals(257, getListener().getRecords().size());

    for (VehicleLocationRecord vehicleLocationRecord : getListener().getRecords()) {
      String tripId = vehicleLocationRecord.getTripId().toString();
      assertNotEquals(tripId, vehicleLocationRecord.getVehicleId().toString()); // vehicles are train Id, not tripId
      assertEquals(tripId, vehicleLocationRecord.getTripId().toString());
      assertEquals(tripId, vehicleLocationRecord.getBlockId().toString());
    }

  }

  private int diffInMinutes(long currentTimeMillis, long predictionTimeSeconds) {
    return Math.toIntExact((predictionTimeSeconds * 1000 - currentTimeMillis) / 1000 / 60) + 1;
  }
}
