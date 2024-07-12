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
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test that SS Express trips are re-mapped to route SI.
 * If they are not re-mapped they will be silently dropped.
 */
public class NyctExpressRemapIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {
  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void testSStoSi() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("MTASBWY");
    Map<String, String> remaps = new HashMap<>();
    remaps.put("SS", "SI");
    source.setRouteRemap(remaps);

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_added_trips/gtfs-si-10192023-073001.pb";
    ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource.getURL());
    source.refresh(); // launch

    assertEquals(20, listener.getRecords().size()); // would be 9 if just SI
    // now check for a specific SS trip in A/D to confirm it flowed through system
    // MTASBWY_048000_SS..N
    // stop MTASBWY_S16N
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry expectedStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_S16N"));
    Map<AgencyAndId, Integer> actualTripIds = verifyTripRange("expected SS trip", expectedStop, source.getGtfsRealtimeTripLibrary().getCurrentTime(), null);
    assertTrue(actualTripIds.containsKey(AgencyAndId.convertFromString("MTASBWY_048000_SS..N")));
  }
}
