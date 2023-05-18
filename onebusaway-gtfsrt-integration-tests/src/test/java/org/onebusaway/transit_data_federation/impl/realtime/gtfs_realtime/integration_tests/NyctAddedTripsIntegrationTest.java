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
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.springframework.core.io.ClassPathResource;

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
    source.setVehicleLocationListener(listener);
    MonitoredResult testResult = new MonitoredResult();
    source.setMonitoredResult(testResult);

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips/nyct_subways_gtfs_rt.pb";
    ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource.getURL());
    source.refresh(); // launch


    // check MonitoredResult for expected number of results
    assertFalse(testResult.getAddedTripIds().isEmpty()); // this doesn't work yet!

    assertEquals(5, listener.getRecords().size()); // todo

    VehicleLocationRecord vehicleLocationRecord = listener.getRecords().get(0);
    assertEquals("vehicle1", vehicleLocationRecord.getVehicleId()); // todo
    assertEquals("MTASBWY_trip1", vehicleLocationRecord.getTripId().toString()); // todo
    assertEquals("MTASBWY_trip1", vehicleLocationRecord.getBlockId().toString()); // todo
    // * vehicleLocationListener needs to make some determination about the schedule deviation
  }
}
