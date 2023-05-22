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
  @Test
  public void testAddedViaExtension() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("MTASBWY");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    BlockLocationServiceImpl blockLocationService = getBundleLoader().getApplicationContext().getBean(BlockLocationServiceImpl.class);
    // todo had to make VehicleStatusServiceImpl public due to non-unique interfaces -- see if there is a better way
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

    assertEquals(148, listener.getRecords().size());

    VehicleLocationRecord firstRecord = null;
    long firstStopTime = -1;
    for (VehicleLocationRecord vehicleLocationRecord : listener.getRecords()) {
      if (firstRecord == null) {
        firstRecord = vehicleLocationRecord;
        firstStopTime = vehicleLocationRecord.getTimepointPredictions().get(0).getTimepointPredictedArrivalTime();
      }
      String tripId = vehicleLocationRecord.getTripId().toString();
      assertEquals(tripId, vehicleLocationRecord.getVehicleId().toString()); // vehicles are named the tripId
      assertEquals(tripId, vehicleLocationRecord.getTripId().toString());
      assertEquals(tripId, vehicleLocationRecord.getBlockId().toString());
    }

    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    long window = 75 * 60 * 1000; // 75 minutes

    // hard code an example to be repeatable
    StopEntry secondStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_250N"));
    long secondStopTime = 1683630090000l;
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(secondStop,
            new TargetTime(secondStopTime), secondStopTime - window, secondStopTime + window);
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
    for (ArrivalAndDepartureInstance instance : list) {
      if (expectedDynamicTrips.contains(instance.getBlockInstance().getBlock().getTrips().get(0).getTrip().getId().toString())) {
        actualDynamicTripsSize++;
        realtimeBlocks.add(instance.getBlockInstance());
      }
    }
    assertEquals(expectedDynamicTripsSize, actualDynamicTripsSize);

    // next pick the first in the list -- it will be different each time
    StopEntry firstStop = graph.getStopEntryForId(firstRecord.getTimepointPredictions().get(0).getTimepointId(), true);
    list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,
            new TargetTime(firstStopTime), firstStopTime - window, firstStopTime + window);
    assertNotNull(list);


    // now check for blockLocations of that trip
    for (BlockInstance blockInstance : realtimeBlocks) {
      BlockLocation locationForBlockInstance = blockLocationService.getLocationForBlockInstance(blockInstance, new TargetTime(secondStopTime));
      assertNotNull(locationForBlockInstance);
    }

  }
}
