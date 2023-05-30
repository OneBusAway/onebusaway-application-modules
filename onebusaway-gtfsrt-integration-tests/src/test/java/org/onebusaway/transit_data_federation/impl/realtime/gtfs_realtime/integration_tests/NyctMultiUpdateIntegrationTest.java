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
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test successive GTFS-RT updates to verify distance along block, schedule deviation, and
 * other fields in ADDED trip support
 */
public class NyctMultiUpdateIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void testMultiUpdatesViaExtension() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("MTASBWY");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    BlockLocationServiceImpl blockLocationService = getBundleLoader().getApplicationContext().getBean(BlockLocationServiceImpl.class);

    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename1 = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips/nyct_subways_gtfs_rt.2023-05-29T14:03:24-04:00.pb";
    ClassPathResource gtfsRtResource1 = new ClassPathResource(gtfsrtFilename1);
    if (!gtfsRtResource1.exists()) throw new RuntimeException(gtfsrtFilename1 + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource1.getURL());
    source.refresh(); // launch

    // 10 minutes later
    String gtfsrtFilename2 = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips/nyct_subways_gtfs_rt.2023-05-29T14:13:29-04:00.pb";
    ClassPathResource gtfsRtResource2 = new ClassPathResource(gtfsrtFilename2);
    if (!gtfsRtResource2.exists()) throw new RuntimeException(gtfsrtFilename2 + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource2.getURL());
    source.refresh(); // launch


    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_250N"));
    long firstStopTime = 1685384106000l;  //14:15 -- 2 minutes in future

    long window = 75 * 60 * 1000; // 75 minutes
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,
            new TargetTime(firstStopTime), firstStopTime - window, firstStopTime + window);
    assertNotNull(list);

    int dabSetCount = 0;
    int dynamicBlockCount = 0;
    for (ArrivalAndDepartureInstance instance : list) {
      if (instance.getBlockInstance().getBlock() instanceof DynamicBlockConfigurationEntryImpl) {
        dynamicBlockCount++;
      }
      if (instance.getBlockLocation() != null) {
        if (instance.getBlockLocation().isDistanceAlongBlockSet())
          dabSetCount++;
      }
    }
    assertTrue(dabSetCount > 0);
    assertEquals(dynamicBlockCount, dabSetCount);
  }

}