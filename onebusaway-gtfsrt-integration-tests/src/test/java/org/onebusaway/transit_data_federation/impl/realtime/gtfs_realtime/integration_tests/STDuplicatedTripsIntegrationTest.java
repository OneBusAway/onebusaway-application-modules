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
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class STDuplicatedTripsIntegrationTest extends AbstractJsonRealtimeIntegrationText  {
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
    String jsonFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/st_duplicated_trips/trip_update_1683740698.json";

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

    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("40_99914")); // 9:55 trip duplicated to 10:43
    long firstStopTime = 1683740698000L;

    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,new TargetTime(firstStopTime,firstStopTime),firstStopTime - window, firstStopTime + window);
    assertNotNull(list);
    // LLR_2023-03-18_Weekday_100479_1043  LLR_2023-03-18_Weekday_100479_2047

    int expectedDuplicatedTripsSize = 1;
    int actualDuplicatedTripsSize = 0;

    List<String> expectedduplicatedTrips = new ArrayList<>();
    expectedduplicatedTrips.add("LLR_2023-03-18_Weekday_100479_1043_Dup");
    expectedduplicatedTrips.add("LLR_2023-03-18_Weekday_100479_2047");

    List<TripEntry> DynamictripsFound = new ArrayList<>();
    for(ArrivalAndDepartureInstance instance : list){
      String tripId = instance.getStopTimeInstance().getStopTime().getTrip().getTrip().getId().getId();
      AgencyAndId stopID = instance.getStopTimeInstance().getStopTime().getStopTime().getStop().getId();
      if(expectedduplicatedTrips.contains(tripId)){
        DynamictripsFound.add(instance.getStopTimeInstance().getStopTime().getTrip().getTrip());
        actualDuplicatedTripsSize++;
        assertTrue(instance.getPredictedArrivalTime() > 0 || instance.getPredictedDepartureTime() > 0);
        if ("40_LLR_2023-03-18_Weekday_100479_1043_Dup".equals(instance.getBlockInstance().getBlock().getBlock().getId().toString())) {
          BlockLocation blockLocation =  instance.getBlockLocation();
          assertNotNull(blockLocation);
          List<TimepointPredictionRecord> timepointPredictions = blockLocation.getTimepointPredictions();
          assertNotNull(timepointPredictions);
          for(TimepointPredictionRecord timepointPrediction : timepointPredictions){
            assertTrue(timepointPrediction.getTimepointPredictedArrivalTime() > 0 ||
            timepointPrediction.getTimepointPredictedDepartureTime() > 0);
            assertEquals("40_LLR_2023-03-18_Weekday_100479_1043_Dup",timepointPrediction.getTripId().toString());
            assertNotNull(timepointPrediction.getTimepointId());
          }
        }
      }
    }
    if(expectedDuplicatedTripsSize != actualDuplicatedTripsSize){
      _log.error("expected {}, actual {}", expectedDuplicatedTripsSize, actualDuplicatedTripsSize);
    }
    assertEquals(expectedDuplicatedTripsSize,actualDuplicatedTripsSize);
  }


}
