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
            expectedTripId, null, expectedHeadsign, 0);
  }

  @Test
  public void test4() throws Exception {
    // this pattern breaks head-signs (and A/D on prod)
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_A","MTASBWY_B","MTASBWY_C","MTASBWY_D");
    String expectedStopId = "MTASBWY_A15S";
    String expectedRouteId = "MTASBWY_D";
    String expectedTripId = "MTASBWY_053550_D..S14R";
    String expectedHeadsign = "Coney Island-Stillwell Av";
    String expectedVehicleId = "MTASBWY_1D 0855+ 205/STL";
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.2024-02-28T09:11:26:00.pb";

    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, name);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            expectedTripId, expectedVehicleId, expectedHeadsign, 4);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_054050_D..S07R", "MTASBWY_1D 0900+ 205/STL", expectedHeadsign, 16);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_055100_D..S07R", "MTASBWY_1D 0911 205/STL", expectedHeadsign, 27);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_056100_D..S07R", "MTASBWY_1D 0921 205/STL", expectedHeadsign, 37);
  }

  @Test
  // MTASBWY_049500_D..S14R -> MTASBWY_049900_D..S14R MTASBWY_1D 0815 205/STL -> MTASBWY_1D 0819 BPK/STL
  // MTASBWY_050100_D..S14R -> MTASBWY_050500_D..S14R MTASBWY_1D 0821 205/STL -> MTASBWY_1D 0825 BPK/STL
  public void test5() throws Exception {
    // another example of missing D trips
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_A","MTASBWY_B","MTASBWY_C","MTASBWY_D");
    String expectedStopId = "MTASBWY_A15S";
    String expectedRouteId = "MTASBWY_D";
    String[] expectedTripIds = Arrays.asList(/*"MTASBWY_044700_D..S14R", "MTASBWY_045700_D..S14R", "MTASBWY_046300_D..S14R",
            "MTASBWY_047050_D..S14R", "MTASBWY_047700_D..S14R", "MTASBWY_048600_D..S14R",*/
            "MTASBWY_049900_D..S14R", "MTASBWY_050500_D..S14R", "MTASBWY_050850_D..S14R").toArray(new String[0]);
    String expectedHeadsign = "Coney Island-Stillwell Av";
    String[] expectedVehicleIds = Arrays.asList(/*"MTASBWY_1D 0727 BPK/STL", "MTASBWY_1D 0737 BPK/STL", "MTASBWY_1D 0743 205/STL",
            "MTASBWY_1D 0750+ 205/STL", "MTASBWY_1D 0757 205/STL", "MTASBWY_1D 0806 205/STL",*/
            "MTASBWY_1D 0819 BPK/STL", "MTASBWY_1D 0825 BPK/STL", "MTASBWY_1D 0828+ 205/STL").toArray(new String[0]);
    int[] expectedArrivals = {7, 14, 21};
    String path = getIntegrationTestPath() + File.separator;
    String name = "nyct_subways_gtfs_rt.2024-03-04T08:28:35:00.pb";
    /* Mon Mar  4 08:28:35 EST 2024
    -A15S 044700_D..S14R     D     044700_D..S14R     Coney Island-Stillwell Av       Ar 0744 (3m)   1D 0727 BPK/STL    A3
    -A15S 045700_D..S14R     D     045700_D..S14R     Coney Island-Stillwell Av       Ar 0754 (13m)  1D 0737 BPK/STL    A3
    -A15S 046300_D..S14R     D     046300_D..S14R     Coney Island-Stillwell Av       Ar 0805 (24m)  1D 0743 205/STL    A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
    -A15S 047050_D..S14R     D     047050_D..S14R     Coney Island-Stillwell Av       Ar 0812 (31m)  1D 0750+ 205/STL   A3
    -A15S 047700_D..S14R     D     047700_D..S14R     Coney Island-Stillwell Av       Ar 0819 (38m)  1D 0757 205/STL    A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
    -A15S 048600_D..S14R     D     048600_D..S14R     Coney Island-Stillwell Av       Ar 0825 (44m)  1D 0806 205/STL    A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
    A15S 049500_D..S14R     D     049500_D..S14R     Coney Island-Stillwell Av       Ar 0837 (56m)  1D 0815 205/STL    A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
    A15S 050100_D..S14R     D     050100_D..S14R     Coney Island-Stillwell Av       Ar 0843 (1h)   1D 0821 205/STL    A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
    A15S 050850_D..S14R     D     050850_D..S14R     Coney Island-Stillwell Av       Ar 0850 (1h)   1D 0828+ 205/STL   A3                             --- MISSING TRIP ---                                                                            --- MISSING TRIP ---
     */

    assertEquals(expectedTripIds.length, expectedVehicleIds.length);
    assertEquals(expectedTripIds.length, expectedArrivals.length);
    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, name);
    for (int i=0; i < expectedTripIds.length; i++ ) {
      expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
              expectedTripIds[i], expectedVehicleIds[i], expectedHeadsign, expectedArrivals[i]);
    }
  }

  /**
   * receive 4 updates of mutating tripIds and confirm expected tripIds are seen each time
   * @throws Exception
   */
  @Test
  public void test7() throws Exception {
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_A","MTASBWY_B","MTASBWY_C","MTASBWY_D");
    String expectedStopId = "MTASBWY_D14S";
    String expectedRouteId = "MTASBWY_D";
    String path = getIntegrationTestPath() + File.separator;

    // part I: expect 043300_D..S14R  1D 0713 205/STL 60mins
    // D01S at 7:13 (only lists 2 stops!!!), next D03S
    String part1 = "nyct_subways_gtfs_rt.2024-03-05T06:14:33.pb";
    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, "MTASBWY_D01S", path, part1);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), "MTASBWY_D01S", expectedRouteId,
            "MTASBWY_043300_D..S14R", "MTASBWY_1D 0713 205/STL", "Bedford Park Blvd", 58);

    // part II: expect 043300_D..S14R  1D 0713 205/STL 57mins
    String part2 = "nyct_subways_gtfs_rt.2024-03-05T06:17:43.pb";
    // now has 30 stops, D01S at 7:13:30
    // too far in future?
    //source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, part2);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), "MTASBWY_D01S", expectedRouteId,
            "MTASBWY_043300_D..S14R", "MTASBWY_1D 0713 205/STL", "Bedford Park Blvd", 58);
    // AND D14S at 7:44:30
    // too far in future?
//    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
//            "MTASBWY_043300_D..S14R", "MTASBWY_1D 0713 205/STL", "Bedford Park Blvd", 200);



    // part III: expect 043300_D..S14R  1D 0713 205/STL 3mins
    String part3 = "nyct_subways_gtfs_rt.2024-03-05T07:17:39.pb";
    source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, part3);
    // ok things get interesting here!  we loose the trip, even tho the feed still hav a D14S 7:44 arrival
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_043300_D..S14R", "MTASBWY_1D 0713 205/STL", "Bedford Park Blvd", 26);

    // part IV: expect 043700_D..S14R  1D 0717 BPK/STL (departed)
    // grab a downstream stop....D10S is first guess
    String part4 = "nyct_subways_gtfs_rt.2024-03-05T07:43:01.pb";
    source = runRealtime(routeIdsToCancel, expectedRouteId, "MTASBWY_D10S", path, part4);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_043700_D..S14R", "MTASBWY_1D 0717 BPK/STL", "Coney Island-Stillwell Av", 0);

  }

  @Test
  /*
   * If the first stop on a trip is a wrong way concurrency verify that predictions still match.
   */
  public void test8() throws Exception {
    List<String> routeIdsToCancel = Arrays.asList("MTASBWY_B","MTASBWY_D","MTASBWY_F","MTASBWY_M");
    String expectedStopId = "MTASBWY_M11N"; // wrong way stop
    String expectedRouteId = "MTASBWY_M";
    String path = getIntegrationTestPath() + File.separator;
    String part1 = "nyct_subways_gtfs_rt.2024-03-05T19:58:50.pb";
    GtfsRealtimeSource source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, part1);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_118550_M..S20X009", "MTASBWY_1M 1945+ 576/MET", "Middle Village-Metropolitan Av", 16);
    // when train passes Broadway-Lafayette it drops out of system
    // Missing data for Essex St to Myrtle Av
    // (also late night trips running from Myrtle Ave to Met don't show Myrtle Ave
    // M stopping pattern
    // Delancey St-Essex St: M18S (but actually M18N for wrong way concurrency)
    // Myrtle: M11S
    String part2 = "nyct_subways_gtfs_rt.2024-03-05T19:59:50.pb";
    source = runRealtime(routeIdsToCancel, expectedRouteId, expectedStopId, path, part2);
    expectArrivalAndTripAndHeadsign(source.getGtfsRealtimeTripLibrary().getCurrentTime(), expectedStopId, expectedRouteId,
            "MTASBWY_118550_M..S20X009", "MTASBWY_1M 1945+ 576/MET", "Middle Village-Metropolitan Av", 15);
  }
}
