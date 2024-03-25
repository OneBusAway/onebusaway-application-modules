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
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureFilterByRealtime;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicBlockIndexServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeCancelServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.core.io.ClassPathResource;

import java.util.*;

import static org.junit.Assert.*;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;

/**
 * Confirm that an ADDED trip that changes details is consumed and interpreted correctly.
 */
public class NyctChangingPatternTest extends AbstractGtfsRealtimeIntegrationTest {
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
    source.setRouteIdsToCancel(Arrays.asList("MTASBWY_A","MTASBWY_B","MTASBWY_C"));

    TransitGraphDao graph = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class);
    StopEntry firstStop = graph.getStopEntryForId(AgencyAndId.convertFromString("MTASBWY_A03N"));


    String[] reverseNames =  {
            "1707401927.ace.pb",
            "1707401866.ace.pb",
            "1707401806.ace.pb",
            "1707401745.ace.pb",
            "1707401685.ace.pb",
            "1707401625.ace.pb",
            "1707401564.ace.pb",
            "1707401504.ace.pb",
            "1707401443.ace.pb",
            "1707401383.ace.pb",
            "1707401323.ace.pb",
            "1707401262.ace.pb",
            "1707401202.ace.pb",
            "1707401142.ace.pb",
            "1707401081.ace.pb",
            "1707401021.ace.pb",
            "1707400960.ace.pb",
            "1707400900.ace.pb",
            "1707400840.ace.pb",
            "1707400779.ace.pb",
            "1707400719.ace.pb",
            "1707400659.ace.pb",
            "1707400598.ace.pb",
            "1707400538.ace.pb",
            "1707400478.ace.pb",
            "1707400417.ace.pb",
            "1707400357.ace.pb",
            "1707400297.ace.pb",
            "1707400236.ace.pb",
            "1707400176.ace.pb",
            "1707400115.ace.pb",
            "1707400055.ace.pb",
            "1707399995.ace.pb",
            "1707399934.ace.pb",
            "1707399874.ace.pb",
            "1707399814.ace.pb",
            "1707399753.ace.pb",
            "1707399693.ace.pb",
            "1707399632.ace.pb",
            "1707399572.ace.pb",
            "1707399512.ace.pb",
            "1707399452.ace.pb",
            "1707399391.ace.pb",
            "1707399331.ace.pb",
            "1707399270.ace.pb",
            "1707399210.ace.pb",
            "1707399150.ace.pb",
            "1707399090.ace.pb",
            "1707399029.ace.pb",
            "1707398969.ace.pb",
            "1707398908.ace.pb",
            "1707398848.ace.pb",
            "1707398788.ace.pb",
            "1707398727.ace.pb",
            "1707398667.ace.pb",
            "1707398607.ace.pb",
            "1707398546.ace.pb",
            "1707398486.ace.pb",
            "1707398426.ace.pb",
            "1707398365.ace.pb",
            "1707398305.ace.pb",
            "1707398244.ace.pb",
            "1707398184.ace.pb",
            "1707398123.ace.pb",
            "1707398063.ace.pb",
            "1707398002.ace.pb",
            "1707397942.ace.pb",
            "1707397881.ace.pb",
            "1707397821.ace.pb",
            "1707397760.ace.pb",
            "1707397700.ace.pb",
            "1707397640.ace.pb",
            "1707397579.ace.pb",
            "1707397519.ace.pb",
            "1707397458.ace.pb",
            "1707397398.ace.pb",
            "1707397338.ace.pb",
            "1707397277.ace.pb",
            "1707397217.ace.pb",
            "1707397157.ace.pb"

    };
    List<String> names = Arrays.asList(reverseNames);
    List<String> exceptionTrips = null;
    Collections.reverse(names);
    int i = 1;
    for (String name : names) {
      long iterationTime = source.getGtfsRealtimeTripLibrary().getCurrentTime();
      String gtfsrtFilenameN = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips/" + name;
      ClassPathResource gtfsRtResourceN = new ClassPathResource(gtfsrtFilenameN);
      source.setTripUpdatesUrl(gtfsRtResourceN.getURL());
      source.refresh();
      verifyBeans("beans run " + i + ", name=" + name, firstStop, iterationTime, exceptionTrips);
      verifyRouteDirectionStops("MTASBWY_A", _exceptionRouteIds);
      if (iterationTime == 0l) continue;
      long expectedTime = timeMillis(iterationTime, 8, 52, 0);
      if (iterationTime >=  expectedTime) {
        debugStop("run " + i + " for stop " + firstStop.getId() + " using file " + name,
                firstStop, iterationTime, 22);
      }
      i++;
    }

    DynamicBlockIndexServiceImpl dynamicBlockIndexService = getBundleLoader().getApplicationContext().getBean(DynamicBlockIndexServiceImpl.class);
    List<BlockStopTimeIndex> stopTimeIndicesForStop = dynamicBlockIndexService.getStopTimeIndicesForStop(firstStop);
    _log.debug("stopTimeIndicesForStop");

    for (AgencyAndId stopId : stopHeadsigns.keySet()) {
      if (stopHeadsigns.get(stopId).size() > 1) {
        _log.error("bad result for stop {} with {}", stopId, stopHeadsigns.get(stopId));
        fail();
      } else {
        _log.debug("result: stopId {} has {}", stopId, stopHeadsigns.get(stopId));
      }
    }

  }

  private long timeMillis(long serviceDate, int hour, int minute, int seconds) {
    return new ServiceDate(new Date(serviceDate)).getAsDate().getTime() + time(hour, minute, seconds) * 1000;
  }

  private void debugStop(String msg, StopEntry stop, long firstStopTime, int headwayMinutes) {
    ArrivalsAndDeparturesBeanService service = getBundleLoader().getApplicationContext().getBean(ArrivalsAndDeparturesBeanService.class);
    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    query.setTime(firstStopTime);
    query.setMinutesBefore(1);
    query.setMinutesAfter(headwayMinutes); // 5 minute headway
    List<String> filter = new ArrayList<>();
    filter.add("MTASBWY");
    query.getSystemFilterChain().add(new ArrivalAndDepartureFilterByRealtime(filter));
    List<ArrivalAndDepartureBean> arrivalsAndDeparturesByStopId = service.getArrivalsAndDeparturesByStopId(stop.getId(), query);
    // confirm there is an arrival in headway minutes
    assertFalse(msg, arrivalsAndDeparturesByStopId.isEmpty());
  }

}
