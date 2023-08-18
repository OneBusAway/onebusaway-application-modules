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
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test G stop replacement of wrong way concurrencies.
 */
public class NyctWrongWayGIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {
  @Override
  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way";
  }

  @Override
  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void testWrongWayConcurrenciesOnG() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("MTASBWY");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();

    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_wrong_way/g.pb";
    ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
    source.setTripUpdatesUrl(gtfsRtResource.getURL());
    source.refresh(); // launch

    /*
    route_id,direction_id,from_stop_id,to_stop_id
    G,0,A42N,A42S
    G,1,A42S,A42N
     */
    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);
    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry southStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_A42S"));
    StopEntry northStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_A42N"));
    long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();  //Fri Aug 11 15:04:26 EDT 2023
    long window = 75 * 60 * 1000; // 75 minutes
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(southStop,
            new TargetTime(firstStopTime, firstStopTime), firstStopTime, firstStopTime + window);
    assertNotNull(list);
    for (ArrivalAndDepartureInstance ad : list) {
      // route G, direction = 0
      // G36S
      // A42S -> A42N
      // F20S
      //route_id,direction_id,from_stop_id,to_stop_id
      //G,0,A42N,A42S -> A42S should be 0 direction
      //G,1,A42S,A42N
      if ("G".equals(ad.getBlockTrip().getTrip().getRoute().getId().getId())) {
        if (!"0".equals(ad.getBlockTrip().getTrip().getDirectionId())) {
          _log.error("bad A/D={}", ad);
          assertEquals("0", ad.getBlockTrip().getTrip().getDirectionId());
        }
      }
    }
    list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(northStop,
            new TargetTime(firstStopTime, firstStopTime), firstStopTime, firstStopTime + window);
    assertNotNull(list);
    for (ArrivalAndDepartureInstance ad : list) {
      if ("G".equals(ad.getBlockTrip().getTrip().getRoute().getId().getId())) {
        if (!"1".equals(ad.getBlockTrip().getTrip().getDirectionId())) {
          _log.info("bad A/D={}", ad);
          assertEquals("1", ad.getBlockTrip().getTrip().getDirectionId());
        }
      }

    }
    verifyBeans("northStop", northStop, firstStopTime);
    verifyBeans("southStop", southStop, firstStopTime);
  }
}
