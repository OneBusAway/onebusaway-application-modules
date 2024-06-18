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
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureFilterByRealtime;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRtBuilder;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Verify that real-time is accepted on 24+ hour trips -- this time using live data
 */
public class WMATAServiceDay4IntegrationTest extends AbstractGtfsRealtimeIntegrationTest {

  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_4";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  public void test() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("1");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);
    MonitoredResult testResult = new MonitoredResult();
    source.setMonitoredResult(testResult);

    String[] pbFilenames = {
            "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_4/23:45.wmata.pb",
            "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_4/23:54.wmata.pb",
            "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_4/00:04.wmata.pb",
            "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_4/00:09.wmata.pb"
    };
    String[] stops = {
            "1_15613",  //K6 Z8 Z6
            "1_13004",
            "1_13006",
            "1_10662",
            "1_10650", // Z2 Z6 Z8
            "1_27959"
    };

    int index = -1;
    for (String pbFilename : pbFilenames) {
      index++;
      ClassPathResource gtfsRtResource = new ClassPathResource(pbFilename);
      if (!gtfsRtResource.exists()) throw new RuntimeException(pbFilename + " not found in classpath!");
      GtfsRtBuilder builder = new GtfsRtBuilder();
      loadRealtime(pbFilename);
      source.refresh(); // launch

      TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
      StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString(stops[index]));
      assertNotNull(firstStop);
      long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();

      ArrivalsAndDeparturesBeanService service = getBundleLoader().getApplicationContext().getBean(ArrivalsAndDeparturesBeanService.class);
      ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
      query.setTime(firstStopTime);
      query.setMinutesBefore(5);
      query.setMinutesAfter(30);
      List<String> filter = new ArrayList<>();
      filter.add("1");
      query.getSystemFilterChain().add(new ArrivalAndDepartureFilterByRealtime(filter));
      List<ArrivalAndDepartureBean> arrivalsAndDeparturesByStopId = service.getArrivalsAndDeparturesByStopId(firstStop.getId(), query);
      assertNotNull(arrivalsAndDeparturesByStopId);
      assertTrue("missing arrival for stop " + stops[index], arrivalsAndDeparturesByStopId.size() > 0);
      listener.reset();
    }
  }

}
