/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Run random traces here and test for expected arrivals
 */
public class NyctRandomIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void test1() throws Exception {
    // missing 0, 11, 15? arrivals on "1" compared to 1.3
    // for stop : "59 St - Columbus Circle"
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_1","MTASBWY_2","MTASBWY_3");
    String expectedStopId = "MTASBWY_125N";
    String expectedRouteId = "MTASBWY_1";
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.2024-02-11T00:28:07-04:00.pb";

    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, name);
    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 1);
    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 11);
    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 15);

  }

  @Test
  public void test2() throws Exception {
    // Stops 123N and 110S not showing service after midnight
    // GTFS-RT trip start date can't be trusted -- we need to compute service day
    // based on first stop time to avoid negative arrival times
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_1","MTASBWY_2","MTASBWY_3");
    String expectedStopId = "MTASBWY_123N";
    String expectedRouteId = "MTASBWY_2";
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.2024-02-16T00:53:20-04:00.pb";

    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, name);
    // 147200_1..N03R
    // start date 20240216
    // arrival 1708063139 / Fri Feb 16 00:58:59 EST 2024
    // first stop 1708062869
    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 4);
    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 5);
//    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 11);
//    expectArrival(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId, 15);

  }

  @Test
  public void test3() throws Exception {
  // this pattern breaks head-signs (and A/D on prod)
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_J","MTASBWY_Z");
    String expectedStopId = "MTASBWY_J12N";
    String expectedRouteId = "MTASBWY_J";
    String expectedTripId = "MTASBWY_055000_J..N43R";
    String expectedHeadsign = "Jamaica Center-Parsons/Archer"; // error if "Broadway Junction"
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.2024-02-21T10:00:33-04:00.pb";

    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, name);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            expectedTripId, expectedHeadsign, 0);
  }
}
