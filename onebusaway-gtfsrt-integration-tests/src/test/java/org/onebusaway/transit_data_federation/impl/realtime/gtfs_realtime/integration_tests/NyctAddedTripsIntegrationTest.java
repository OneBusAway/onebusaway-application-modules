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

import org.junit.Ignore;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NyctAddedTripsIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }
  @Ignore
  public void testAddedViaExtension() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("MTASBWY");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    BlockLocationServiceImpl blockLocationService = getBundleLoader().getApplicationContext().getBean(BlockLocationServiceImpl.class);

    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips/nyct_subways_gtfs_rt.pb";
    ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource.getURL());
    source.refresh(); // launch


    // check MonitoredResult for expected number of results
    assertFalse(source.getMonitoredResult().getAddedTripIds().isEmpty());

    assertEquals(252, listener.getRecords().size());

    for (VehicleLocationRecord vehicleLocationRecord : listener.getRecords()) {
      String tripId = vehicleLocationRecord.getTripId().toString();
      assertEquals(tripId, vehicleLocationRecord.getVehicleId().toString()); // vehicles are named the tripId
      assertEquals(tripId, vehicleLocationRecord.getTripId().toString());
      assertEquals(tripId, vehicleLocationRecord.getBlockId().toString());
    }

    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    long window = 75 * 60 * 1000; // 75 minutes

    // hard code an example to be repeatable
    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_250N"));
    long firstStopTime = 1683630090000l;
    long serviceDate = new ServiceDate(2023, 5, 9).getAsDate().getTime();
    long firstPrediction = serviceDate + 25710 * 1000;
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,
            new TargetTime(firstStopTime, firstStopTime), firstStopTime - window, firstStopTime + window);
    assertNotNull(list);
    // MTASBWY_040800_3..N01R   MTASBWY_042150_4..N06R    MTASBWY_042250_3..N01R    MTASBWY_042850_3..N01R
    int expectedDynamicTripsSize = 4;
    int actualDynamicTripsSize = 0;
    Set<String> expectedDynamicTrips = new HashSet<>();
    expectedDynamicTrips.add("MTASBWY_040800_3..N01R");
    expectedDynamicTrips.add("MTASBWY_042150_4..N06R");
    expectedDynamicTrips.add("MTASBWY_042250_3..N01R");
    expectedDynamicTrips.add("MTASBWY_042850_3..N01R");
    Set<BlockInstance> realtimeBlocks = new HashSet<>();
    boolean found = false;
    for (ArrivalAndDepartureInstance instance : list) {
      if (expectedDynamicTrips.contains(instance.getBlockInstance().getBlock().getTrips().get(0).getTrip().getId().toString())) {
        String tripId = instance.getBlockInstance().getBlock().getTrips().get(0).getTrip().getId().toString();
        actualDynamicTripsSize++;
        realtimeBlocks.add(instance.getBlockInstance());
        assertTrue(instance.getPredictedArrivalTime() > 0 || instance.getPredictedDepartureTime() > 0);
        if ("MTASBWY_042850_3..N01R".equals(instance.getBlockInstance().getBlock().getBlock().getId().toString())) {
          found = true;
          BlockLocation blockLocation = instance.getBlockLocation();
          assertNotNull(blockLocation);
          List<TimepointPredictionRecord> timepointPredictions = blockLocation.getTimepointPredictions();
          assertNotNull(timepointPredictions);
          for (TimepointPredictionRecord timepointPrediction : timepointPredictions) {
            assertTrue(timepointPrediction.getTimepointPredictedArrivalTime() > 0
                    || timepointPrediction.getTimepointPredictedDepartureTime() > 0);
            assertEquals("MTASBWY_042850_3..N01R", timepointPrediction.getTripId().toString());
            assertNotNull(timepointPrediction.getTimepointId());
          }

          // this is the first update we've seen, so the DaB will always be 0
          // we don't know the shape or stopping pattern of vehicle before now
          // so for our purposes the vehicle just started the block
          assertEquals(0.0, blockLocation.getDistanceAlongBlock(), 0.1);
        }
      }
    }
    if (expectedDynamicTripsSize != actualDynamicTripsSize) {
      _log.error("expected {}, actual {}", expectedDynamicTrips, realtimeBlocks);
    }
    assertEquals(expectedDynamicTripsSize, actualDynamicTripsSize);

    assertTrue(found);

    // now check for blockLocations of that trip
    for (BlockInstance blockInstance : realtimeBlocks) {
      BlockLocation locationForBlockInstance = blockLocationService.getLocationForBlockInstance(blockInstance, new TargetTime(firstStopTime));
      if (locationForBlockInstance == null) {
        _log.error("no location for blockInstance {}", blockInstance);
      }
    }

  }
}
