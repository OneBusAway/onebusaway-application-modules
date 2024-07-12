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
import org.junit.Ignore;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureFilterByRealtime;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRtBuilder;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class WMATAServiceDay2IntegrationTest extends AbstractGtfsRealtimeJsonIntegrationTest {
  protected String getIntegrationTestPath() {
    return "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_2";
  }

  protected String[] getPaths() {
    String[] paths = {"test-data-sources.xml"};
    return paths;
  }

  @Test
  @Ignore("Broken upstream in CamSys repo; probably since about Nov 16 2023 - https://github.com/camsys/onebusaway-application-modules/commits/unified/?after=87a68db9060d67121fcf912359d18f1e4498bb0d+209")
  public void test() throws Exception {
    GtfsRealtimeSource source = getBundleLoader().getSource();
    source.setAgencyId("1");

    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);
    MonitoredResult testResult = new MonitoredResult();
    source.setMonitoredResult(testResult);

    String[] jsonFilenames = {
            "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/wmata_service_day_2/1020.json"
    };
    String[] stops = {
            "1_4570"
    };

    int index = -1;
    for (String jsonFilename : jsonFilenames) {
      index++;
      // example is in json, convert to protocol buffer
      ClassPathResource gtfsRtResource = new ClassPathResource(jsonFilename);
      if (!gtfsRtResource.exists()) throw new RuntimeException(jsonFilename + " not found in classpath!");
      GtfsRtBuilder builder = new GtfsRtBuilder();
      GtfsRealtime.FeedMessage feed = builder.readJson(gtfsRtResource.getURL());
      URL tmpFeedLocation = createFeedLocation();
      writeFeed(feed, tmpFeedLocation);
      source.setTripUpdatesUrl(tmpFeedLocation);
      source.refresh(); // launch

      assertEquals("expected 1 update in file " + jsonFilename,
              1, listener.getRecords().size());

      TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
      StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString(stops[index]));
      assertNotNull(firstStop);
      long firstStopTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();

      ArrivalsAndDeparturesBeanService service = getBundleLoader().getApplicationContext().getBean(ArrivalsAndDeparturesBeanService.class);
      ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
      query.setTime(firstStopTime);
      query.setMinutesBefore(5);
      query.setMinutesAfter(65);
      List<String> filter = new ArrayList<>();
      filter.add("1");
      query.getSystemFilterChain().add(new ArrivalAndDepartureFilterByRealtime(filter));
      List<ArrivalAndDepartureBean> arrivalsAndDeparturesByStopId = service.getArrivalsAndDeparturesByStopId(firstStop.getId(), query);
      assertNotNull(arrivalsAndDeparturesByStopId);
      _log.error("beans={}", arrivalsAndDeparturesByStopId);
      assertTrue("missing arrival for stop " + stops[index], arrivalsAndDeparturesByStopId.size() > 0);
      listener.reset();
    }
  }
}