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

import com.google.transit.realtime.GtfsRealtime;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRtBuilder;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class STDuplicatedTripsIntegrationTest extends AbstractGtfsRealtimeIntegrationTest  {
  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/st_duplicated_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void testDuplicatedTrips() throws Exception {
    TimeZone aDefault = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
      runTest();
    } finally {
      TimeZone.setDefault(aDefault);
    }
  }

  public void runTest() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("40");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);
    MonitoredResult testResult = new MonitoredResult();
    source.setMonitoredResult(testResult);

    // example is in json, convert to protocol buffer
    String jsonFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/st_duplicated_trips/trip_update_1683740698.json";
    ClassPathResource gtfsRtResource = new ClassPathResource(jsonFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(jsonFilename + " not found in classpath!");
    GtfsRtBuilder builder = new GtfsRtBuilder();
    GtfsRealtime.FeedMessage feed = builder.readJson(gtfsRtResource.getURL());
    URL tmpFeedLocation = createFeedLocation();
    writeFeed(feed, tmpFeedLocation);
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
    long serviceDate = new ServiceDate(2023,5,10).getAsDate().getTime();

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
//
//    assertTrue(foundDuplicate);
  }

  private void writeFeed(GtfsRealtime.FeedMessage feed, URL feedLocation) throws IOException {
    feed.writeTo(Files.newOutputStream(Path.of(feedLocation.getFile())));
  }

  private URL createFeedLocation() throws IOException {
    return File.createTempFile("trip_updates", "pb").toURI().toURL();
  }
}
