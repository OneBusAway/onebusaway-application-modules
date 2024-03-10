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

import org.junit.Ignore;
import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeCancelServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.*;

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

    // setup cancelled service
    GtfsRealtimeCancelServiceImpl cancelService = getBundleLoader().getApplicationContext().getBean(GtfsRealtimeCancelServiceImpl.class);
    source.setGtfsRealtimeCancelService(cancelService);
    source.setRouteIdsToCancel(Arrays.asList("MTASBWY_1","MTASBWY_2","MTASBWY_3"));

    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_250N"));
    long firstStopTime = 1685384106000l;  //14:15 -- 2 minutes in future


    String[] names =  {
            "nyct_subways_gtfs_rt.2023-05-29T14:03:24-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:04:25-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:05:25-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:06:26-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:07:26-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:08:27-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:09:27-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:10:27-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:11:28-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:12:28-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:13:29-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T14:14:29-04:00.pb",
            "nyct_subways_gtfs_rt.2023-05-29T15:22:00-04:00.pb"
    };

    int i = 1;
    for (String name : names) {
      String gtfsrtFilenameN = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips/" + name;
      ClassPathResource gtfsRtResourceN = new ClassPathResource(gtfsrtFilenameN);
      source.setTripUpdatesUrl(gtfsRtResourceN.getURL());
      source.refresh();
      verifyBeans("beans run " + i, firstStop, source.getGtfsRealtimeTripLibrary().getCurrentTime());
      verifyRouteDirectionStops("MTASBWY_1", _exceptionRouteIds);
      verifyRouteDirectionStops("MTASBWY_A", _exceptionRouteIds);
      i++;
    }

    for (AgencyAndId stopId : stopHeadsigns.keySet()) {
      if (stopHeadsigns.get(stopId).size() > 1) {
        _log.error("bad result for stop {} with {}", stopId, stopHeadsigns.get(stopId));
        fail();
      } else {
        _log.error("result: stopId {} has {}", stopId, stopHeadsigns.get(stopId));
      }
    }

  }

}