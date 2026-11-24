/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.frequency;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.route;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.routeCollection;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopRouteDirectionScheduleBean;
import org.onebusaway.transit_data.model.StopRouteScheduleBean;
import org.onebusaway.transit_data_federation.impl.blocks.BlockStopTimeIndicesFactory;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class StopScheduleBeanServiceImplTest {

  private static final ServiceDate SERVICE_DATE = new ServiceDate(2026, 11, 2);

  private StopScheduleBeanServiceImpl _service;

  private NarrativeService _narrativeService;

  private BlockIndexService _blockIndexService;

  private StopEntryImpl _stopA;

  private RouteEntryImpl _route;

  @Before
  public void setup() {
    _stopA = stop("stopA");
    _route = route("route");
    // Sets the route's parent, which trips report as their route collection
    routeCollection("route", _route);

    TransitGraphDao graph = mock(TransitGraphDao.class);
    when(graph.getStopEntryForId(_stopA.getId())).thenReturn(_stopA);

    ExtendedCalendarService calendarService = mock(ExtendedCalendarService.class);
    when(calendarService.getServiceDatesForServiceIds(any(ServiceIdActivation.class))).thenReturn(
        Collections.singleton(SERVICE_DATE));

    RouteBeanService routeBeanService = mock(RouteBeanService.class);
    RouteBean.Builder routeBean = RouteBean.builder();
    routeBean.setId("1_route");
    when(routeBeanService.getRouteForId(_route.getId())).thenReturn(
        routeBean.create());

    _narrativeService = mock(NarrativeService.class);
    when(_narrativeService.getTripForId(any())).thenReturn(
        TripNarrative.builder().setTripHeadsign("Downtown").create());

    _blockIndexService = mock(BlockIndexService.class);

    _service = new StopScheduleBeanServiceImpl();
    _service.setTransitGraphDao(graph);
    _service.setCalendarService(calendarService);
    _service.setRouteBeanService(routeBeanService);
    _service.setNarrativeService(_narrativeService);
    _service.setBlockIndexService(_blockIndexService);
  }

  /**
   * The stop_headsign of each stop time must reach the schedule-for-stop
   * results, for both scheduled and frequency-based trips (issue #207).
   */
  @Test
  public void testStopHeadsign() {
    StopTimeEntryImpl stopTime = addTrip("blockA", "tripA", null);
    StopTimeEntryImpl frequencyStopTime = addTrip("blockB", "tripB",
        frequency(time(6, 0), time(9, 0), 600, 0));

    when(_narrativeService.getStopTimeForEntry(stopTime)).thenReturn(
        StopTimeNarrative.builder().setStopHeadsign("Downtown via 1st Ave").create());
    when(_narrativeService.getStopTimeForEntry(frequencyStopTime)).thenReturn(
        StopTimeNarrative.builder().setStopHeadsign("Downtown via 2nd Ave").create());

    StopRouteDirectionScheduleBean direction = getDirection();

    assertEquals(1, direction.getStopTimes().size());
    assertEquals("Downtown via 1st Ave",
        direction.getStopTimes().get(0).getStopHeadsign());
    assertEquals(1, direction.getFrequencies().size());
    assertEquals("Downtown via 2nd Ave",
        direction.getFrequencies().get(0).getStopHeadsign());
  }

  @Test
  public void testStopHeadsignWithoutNarrative() {
    addTrip("blockA", "tripA", null);
    addTrip("blockB", "tripB", frequency(time(6, 0), time(9, 0), 600, 0));

    StopRouteDirectionScheduleBean direction = getDirection();

    assertEquals(1, direction.getStopTimes().size());
    assertNull(direction.getStopTimes().get(0).getStopHeadsign());
    assertEquals(1, direction.getFrequencies().size());
    assertNull(direction.getFrequencies().get(0).getStopHeadsign());
  }

  /**
   * Adds a two-stop trip serving stop A first, and returns its stop time at
   * stop A. A non-null frequency makes it a frequency-based trip.
   */
  private StopTimeEntryImpl addTrip(String blockId, String tripId,
      FrequencyEntry frequency) {
    TripEntryImpl trip = trip(tripId, "serviceId");
    trip.setRoute(_route);
    trip.setDirectionId("0");

    StopTimeEntryImpl stopTime = stopTime(0, _stopA, trip, time(9, 0),
        time(9, 0), 0);
    stopTime(1, stop("stopB"), trip, time(9, 10), time(9, 10), 0);

    BlockEntryImpl block = block(blockId);
    List<BlockEntry> blocks = Arrays.<BlockEntry> asList(block);
    BlockStopTimeIndicesFactory factory = new BlockStopTimeIndicesFactory();

    if (frequency == null) {
      linkBlockTrips(block, trip);
      when(_blockIndexService.getStopTimeIndicesForStop(_stopA)).thenReturn(
          indicesForStopA(factory.createIndices(blocks)));
    } else {
      linkBlockTrips(block, Arrays.asList(frequency), trip);
      when(_blockIndexService.getFrequencyStopTimeIndicesForStop(_stopA)).thenReturn(
          indicesForStopA(factory.createFrequencyIndices(blocks)));
    }

    return stopTime;
  }

  private <T extends AbstractBlockStopTimeIndex> List<T> indicesForStopA(
      List<T> indices) {
    List<T> results = new ArrayList<T>();
    for (T index : indices) {
      if (index.getStop() == _stopA)
        results.add(index);
    }
    return results;
  }

  private StopRouteDirectionScheduleBean getDirection() {
    List<StopRouteScheduleBean> routes = _service.getScheduledArrivalsForStopAndDate(
        _stopA.getId(), SERVICE_DATE);
    assertEquals(1, routes.size());
    List<StopRouteDirectionScheduleBean> directions = routes.get(0).getDirections();
    assertEquals(1, directions.size());
    return directions.get(0);
  }
}
