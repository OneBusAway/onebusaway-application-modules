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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.junit.After;
import org.junit.Before;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.trips.TimepointPredictionBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockConfigurationEntryImpl;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

/**
 * Build and load a bundle to match to gtfs-rt.  Then run comparisons against
 * the reults of the gtfs-rt.
 */
public abstract class AbstractGtfsRealtimeIntegrationTest {

  protected static Logger _log = LoggerFactory.getLogger(AbstractGtfsRealtimeIntegrationTest.class);

  private BundleContext _bundleContext;
  protected BundleContext getBundleContext() {
    return _bundleContext;
  }
  private BundleLoader _bundleLoader;
  public BundleLoader getBundleLoader() {
    return _bundleLoader;
  }

  private BundleBuilder _bundleBuilder;
  public BundleBuilder getBundleBuilder() {
    return _bundleBuilder;
  }

  protected Map<AgencyAndId, Set<String>> stopHeadsigns = new HashMap<>();
  protected abstract String getIntegrationTestPath();

  @Before
  public void setup() throws Exception {
    _bundleBuilder = new BundleBuilder();
    _bundleBuilder.setup(getIntegrationTestPath());
    _bundleContext = _bundleBuilder.getBundleContext();

    _bundleLoader = new BundleLoader(_bundleContext);
    _bundleLoader.create(getPaths());
    _bundleLoader.load();

  }

  protected abstract String[] getPaths();

  @After
  public void cleanup() {
    // if the bundle loader failed prevent further exceptions
    if (_bundleLoader != null)
      _bundleLoader.close();
  }

  protected void verifyBeans(String message, StopEntry firstStop, long firstStopTime) {
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

    assertTrue("no A/Ds for stop " + firstStop.getId(), !arrivalsAndDeparturesByStopId.isEmpty());
    _log.debug("found {} ADs at {}", arrivalsAndDeparturesByStopId.size(), new Date(firstStopTime));
    for (ArrivalAndDepartureBean bean : arrivalsAndDeparturesByStopId) {
      AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(bean.getTrip().getId());
      if (!tripCount.containsKey(tripId)) {
        tripCount.put(tripId, 0);
      }
      tripCount.put(tripId, tripCount.get(tripId) + 1);
      verifyPredictions(bean);
      verifyNarrative(bean);
    }
    verifyTripRange(message, firstStop, firstStopTime);

  }

  protected void verifyNarrative(ArrivalAndDepartureBean bean) {

    TripEntry trip = getBundleLoader().getApplicationContext().getBean(TransitGraphDao.class)
            .getTripEntryForId(AgencyAndIdLibrary.convertFromString(bean.getTrip().getId()));
    String tripId = trip.getId().toString();

    assertNotNull(bean.getTrip().getShapeId()); // trip must have a shape
    AgencyAndId shapeId = AgencyAndId.convertFromString(bean.getTrip().getShapeId());

    NarrativeService narrativeService = getBundleLoader().getApplicationContext().getBean(NarrativeService.class);
    for (StopTimeEntry stopTimeEntry : trip.getStopTimes()) {
      AgencyAndId stopId = stopTimeEntry.getStop().getId();
      StopTimeNarrative stopTimeNarrative = narrativeService.getStopTimeForEntry(stopTimeEntry);
      if (stopTimeNarrative == null) {
        stopTimeNarrative = narrativeService.getStopTimeNarrativeForPattern(null, stopId, trip.getDirectionId());
        if (stopTimeNarrative == null) {
          _log.error("missing narrative for stopId {} and direction {} for stopTime {} ", stopId, trip.getDirectionId(), stopTimeEntry);
          fail();
        }
      } else if (stopTimeNarrative.getStopHeadsign() != null) {
        validateHeadsign(stopTimeEntry, stopTimeNarrative);
        if (!stopHeadsigns.containsKey(stopId)) {
          stopHeadsigns.put(stopId, new HashSet<>());
        }
        stopHeadsigns.get(stopId).add(bean.getTrip().getRoute().getId() + ":" + stopTimeEntry.getStop().getId() + ":" + stopTimeNarrative.getStopHeadsign());
      }
    }
    // verify the shape exists
    boolean foundShape = narrativeService.getShapePointsForId(shapeId) != null;
    if (!foundShape) {
      _log.error("no shape for trip {}", tripId);
      fail();
    }

  }

  public void validateHeadsign(StopTimeEntry stopTimeEntry, StopTimeNarrative stopTimeNarrative) {
    // no-op here -- optionally implemented by subclasses
  }

  protected void verifyPredictions(ArrivalAndDepartureBean bean) {
    verifyTimepoints(bean.getTripStatus().getTimepointPredictions());
    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(bean.getTrip().getId());
    List<TimepointPredictionRecord> predictionRecordsForTrip = getBundleLoader().getApplicationContext().getBean(TransitDataService.class).getPredictionRecordsForTrip(tripId.getAgencyId(),
            bean.getTripStatus());
    verifyTimepointBeans(predictionRecordsForTrip);
  }

  protected void verifyTimepointBeans(List<TimepointPredictionRecord> beans) {
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

  protected void verifyTimepoints(List<TimepointPredictionBean> tprs) {
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


  protected  Map<AgencyAndId, Integer> verifyTripRange(String message, StopEntry firstStop, long firstStopTime) {
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
    return tripCount;
  }

  protected void verifyRouteDirectionStops(String routeId) {
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

  protected void verifyTripCounts(String message, Map<AgencyAndId, Integer> tripCount) {
    for (AgencyAndId tripId : tripCount.keySet()) {
      Integer count = tripCount.get(tripId);
      if (count > 1) {
        _log.error(message + "; duplicate trip {}", tripId);
        fail(message + " duplicate trip " + tripId);
      }
    }
  }


}
