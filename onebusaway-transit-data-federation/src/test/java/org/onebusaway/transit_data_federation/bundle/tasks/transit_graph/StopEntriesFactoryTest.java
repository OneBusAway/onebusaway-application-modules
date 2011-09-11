/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.StopEntriesFactory;
import org.onebusaway.transit_data_federation.impl.transit_graph.AgencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopEntriesFactoryTest {

  @Test
  public void test() {

    Stop stopA = new Stop();
    stopA.setId(new AgencyAndId("1", "stopA"));
    stopA.setCode("A");
    stopA.setDesc("Stop A Description");
    stopA.setLat(47.0);
    stopA.setLon(-122.0);
    stopA.setLocationType(0);
    stopA.setName("Stop A");

    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("1", "stopB"));
    stopB.setCode("B");
    stopB.setDesc("Stop B Description");
    stopB.setLat(47.1);
    stopB.setLon(-122.1);
    stopB.setLocationType(0);
    stopB.setName("Stop B");

    GtfsRelationalDao dao = Mockito.mock(GtfsRelationalDao.class);
    Mockito.when(dao.getAllStops()).thenReturn(Arrays.asList(stopA, stopB));

    StopEntriesFactory factory = new StopEntriesFactory();
    factory.setGtfsDao(dao);

    TransitGraphImpl graph = new TransitGraphImpl();

    AgencyEntryImpl agency = new AgencyEntryImpl();
    agency.setId("1");
    graph.putAgencyEntry(agency);
    graph.refreshAgencyMapping();

    factory.processStops(graph);

    StopEntryImpl stopEntryA = graph.getStopEntryForId(stopA.getId());

    assertEquals(stopA.getId(), stopEntryA.getId());
    assertEquals(stopA.getLat(), stopEntryA.getStopLat(), 0);
    assertEquals(stopA.getLon(), stopEntryA.getStopLon(), 0);

    StopEntryImpl stopEntryB = graph.getStopEntryForId(stopB.getId());

    assertEquals(stopB.getId(), stopEntryB.getId());
    assertEquals(stopB.getLat(), stopEntryB.getStopLat(), 0);
    assertEquals(stopB.getLon(), stopEntryB.getStopLon(), 0);

    List<StopEntry> stops = graph.getAllStops();
    assertEquals(2, stops.size());
    assertTrue(stops.contains(stopEntryA));
    assertTrue(stops.contains(stopEntryB));

    stops = agency.getStops();
    assertTrue(stops.contains(stopEntryA));
    assertTrue(stops.contains(stopEntryB));
  }
}
