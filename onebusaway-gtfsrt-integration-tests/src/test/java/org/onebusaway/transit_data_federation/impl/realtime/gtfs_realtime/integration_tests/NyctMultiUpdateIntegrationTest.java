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
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.trips.TimepointPredictionBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.onebusaway.util.AgencyAndIdLibrary;
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
            "nyct_subways_gtfs_rt.2023-05-29T14:14:29-04:00.pb"
    };

    int i = 1;
    for (String name : names) {
      String gtfsrtFilenameN = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/integration_tests/nyct_multi_trips/" + name;
      ClassPathResource gtfsRtResourceN = new ClassPathResource(gtfsrtFilenameN);
      source.setTripUpdatesUrl(gtfsRtResourceN.getURL());
      source.refresh();
      verifyBeans("beans run " + i, firstStop, source.getGtfsRealtimeTripLibrary().getCurrentTime());
      verifyRouteDirectionStops("MTASBWY_1");
      verifyRouteDirectionStops("MTASBWY_A");
      i++;
    }


  }

  private void verifyBeans(String message, StopEntry firstStop, long firstStopTime) {
    Map<AgencyAndId, Integer> tripCount = new HashMap<>();
    // search for duplicates in API
    ArrivalsAndDeparturesBeanService service = getBundleLoader().getApplicationContext().getBean(ArrivalsAndDeparturesBeanService.class);
    ArrivalsAndDeparturesQueryBean query = new ArrivalsAndDeparturesQueryBean();
    query.setTime(firstStopTime);
    query.setMinutesBefore(5);
    query.setMinutesAfter(65);
    List<String> filter = new ArrayList<>();
    filter.add("MTASBWY");
    query.getSystemFilterChain().add(new ArrivalAndDepartureFilterByRealtime(filter));
    List<ArrivalAndDepartureBean> arrivalsAndDeparturesByStopId = service.getArrivalsAndDeparturesByStopId(firstStop.getId(), query);

    _log.debug("found {} ADs at {}", arrivalsAndDeparturesByStopId.size(), new Date(firstStopTime));
    for (ArrivalAndDepartureBean bean : arrivalsAndDeparturesByStopId) {
      AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(bean.getTrip().getId());
      if (!tripCount.containsKey(tripId)) {
        tripCount.put(tripId, 0);
      }
      tripCount.put(tripId, tripCount.get(tripId) + 1);
      verifyPredictions(bean);
    }
    verifyTripRange(message, firstStop, firstStopTime);

  }

  private void verifyPredictions(ArrivalAndDepartureBean bean) {
    verifyTimepoints(bean.getTripStatus().getTimepointPredictions());
    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(bean.getTrip().getId());
    List<TimepointPredictionRecord> predictionRecordsForTrip = getBundleLoader().getApplicationContext().getBean(TransitDataService.class).getPredictionRecordsForTrip(tripId.getAgencyId(),
            bean.getTripStatus());
    verifyTimepointBeans(predictionRecordsForTrip);
  }

  private void verifyTimepointBeans(List<TimepointPredictionRecord> beans) {
    Set<String> check = new HashSet<>();
    for (TimepointPredictionRecord bean : beans) {
      String hash = bean.getTripId().toString() + ":" + bean.getTimepointId().toString();
      if (check.contains(hash)) {
        _log.error("whoops2!");
        fail(hash);
      }
      check.add(hash);
    }

  }

  private void verifyTimepoints(List<TimepointPredictionBean> tprs) {
    Set<String> check = new HashSet<>();
    for (TimepointPredictionBean tpr : tprs) {
      String hash = tpr.getTripId() + ":" + tpr.getTimepointId();
      if (check.contains(hash)) {
        _log.error("whoops!");
        fail(hash);
      }
      check.add(hash);
    }

  }


  private void verifyTripRange(String message, StopEntry firstStop, long firstStopTime) {
    Map<AgencyAndId, Integer> tripCount = new HashMap<>();
    ArrivalAndDepartureService arrivalAndDepartureService = getBundleLoader().getApplicationContext().getBean(ArrivalAndDepartureService.class);

    long window = 75 * 60 * 1000; // 75 minutes
    List<ArrivalAndDepartureInstance> list = arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(firstStop,
            new TargetTime(firstStopTime, firstStopTime), firstStopTime - window, firstStopTime + window);
    assertNotNull(list);

    for (ArrivalAndDepartureInstance ad : list) {
      AgencyAndId id = ad.getBlockTrip().getTrip().getId();
      if (!tripCount.containsKey(id)) {
        tripCount.put(id, 0);
      }
      tripCount.put(id, tripCount.get(id) + 1);
    }

    verifyTripCounts(message, tripCount);
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

  }

  private void verifyRouteDirectionStops(String routeId) {
    int count = 0;
    TransitDataService service = getBundleLoader().getApplicationContext().getBean(TransitDataService.class);
    StopsForRouteBean stopsForRoute = service.getStopsForRoute(routeId);
    for (StopGroupingBean stopGrouping : stopsForRoute.getStopGroupings()) {
      for (StopGroupBean stopGroup : stopGrouping.getStopGroups()) {
        _log.debug("found route grouping {}", stopGroup.getName().getName());
        count++;
        String lastStopId = null;
        for (String stopId : stopGroup.getStopIds()) {
          if (lastStopId == null) {
            lastStopId = stopId;
          } else {
            if (lastStopId.equals(stopId)) {
              fail("duplicate stop");
            }
          }
        }

      }

    }

    assertEquals(2, count);
  }

  private void verifyTripCounts(String message, Map<AgencyAndId, Integer> tripCount) {
    for (AgencyAndId tripId : tripCount.keySet()) {
      Integer count = tripCount.get(tripId);
      if (count > 1) {
        _log.error(message + "; duplicate trip {}", tripId);
        fail(message + " duplicate trip " + tripId);
      }
    }
  }

}