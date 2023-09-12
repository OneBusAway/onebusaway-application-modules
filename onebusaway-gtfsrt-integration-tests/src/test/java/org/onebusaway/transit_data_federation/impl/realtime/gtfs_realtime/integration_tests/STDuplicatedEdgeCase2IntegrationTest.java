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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Duplicated Trip Edge Case 2: No Stop times provided!
 */
public class STDuplicatedEdgeCase2IntegrationTest extends AbstractJsonRealtimeIntegrationText {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/st_duplicated_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  public String getTimezone() {
    return "America/Los_Angeles";
  }

  @Test
  public void testDuplicatedTrips() throws Exception {
    setupTest();
  }

  public void runTestInTimezone() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("40");

    TestVehicleLocationListener listener = setupTestListener(source);

    // example is in json, convert to protocol buffer
    String jsonFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/st_duplicated_trips/TripUpdate-DUPLICATED-Empty-1690865133.json";

    URL tmpFeedLocation = readJson(jsonFilename);
    source.setTripUpdatesUrl(tmpFeedLocation);
    source.refresh(); // launch

    for (VehicleLocationRecord vehicleLocationRecord : listener.getRecords()) {
      // for now we confirm some elements are present
      assertNotNull(vehicleLocationRecord.getVehicleId());
      assertNotNull(vehicleLocationRecord.getBlockId());
    }

    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    long window = 75 * 60 * 1000; // 75 minutes

    /*
trip_id,stop_id,arrival_time,departure_time,stop_sequence,stop_headsign,timepoint
LLR_2023-03-18_Weekday_100479_1015,99256,06:00:30,06:01:00,1,,1
LLR_2023-03-18_Weekday_100479_1015,99260,06:02:30,06:03:00,2,,1
LLR_2023-03-18_Weekday_100479_1015,621,06:04:30,06:05:00,3,,1
     */

    long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime(); // 21:45
    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("40_99256"));
    assertNotNull(firstStop);
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,new TargetTime(firstStopTime,firstStopTime),firstStopTime - window, firstStopTime + window);
    assertNotNull(list);
    assertEquals(0, list.size());

    StopEntry lastStop = graph.getStopEntryForId(AgencyAndId.convertFromString("40_990006"));
    assertNotNull(lastStop);
    list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(lastStop,new TargetTime(firstStopTime,firstStopTime),firstStopTime - window, firstStopTime + window);
    assertNotNull(list);
    for (ArrivalAndDepartureInstance arrivalAndDepartureInstance : list) {
      boolean isDynamic = isDynamic(arrivalAndDepartureInstance.getBlockTrip().getTrip());
      if (isDynamic) {
        _log.error("found unexpected dynamic trip {}", arrivalAndDepartureInstance.getBlockTrip());
      }
      assertFalse(isDynamic);
    }

    assertEquals(0, list.size());

  }

  private boolean isDynamic(TripEntry trip) {
    return trip.getId().getId().contains("_Dup");
  }
}
