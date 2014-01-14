/**
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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.tasks.UniqueServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;

public class AgencyEntriesFactoryTest {

  @Test
  public void testProcessAgencies() {

    GtfsRelationalDao gtfsDao = Mockito.mock(GtfsRelationalDao.class);

    Agency agencyA = new Agency();
    agencyA.setId("A");

    Agency agencyB = new Agency();
    agencyB.setId("B");

    Mockito.when(gtfsDao.getAllAgencies()).thenReturn(
        Arrays.asList(agencyA, agencyB));

    TransitGraphImpl graph = new TransitGraphImpl();

    AgencyEntriesFactory factory = new AgencyEntriesFactory();
    factory.setGtfsDao(gtfsDao);
    factory.setUniqueService(new UniqueServiceImpl());
    factory.processAgencies(graph);

    AgencyEntry agencyEntryA = graph.getAgencyForId("A");
    assertEquals("A", agencyEntryA.getId());

    AgencyEntry agencyEntryB = graph.getAgencyForId("B");
    assertEquals("B", agencyEntryB.getId());

    List<AgencyEntry> agencies = graph.getAllAgencies();
    assertEquals(2, agencies.size());
    assertTrue(agencies.contains(agencyEntryA));
    assertTrue(agencies.contains(agencyEntryB));
  }

}
