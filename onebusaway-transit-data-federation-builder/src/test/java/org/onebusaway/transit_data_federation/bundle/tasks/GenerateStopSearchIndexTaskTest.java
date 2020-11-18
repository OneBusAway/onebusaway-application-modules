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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class GenerateStopSearchIndexTaskTest {

  private static final double MIN_SCORE = 1.0;

  private GenerateStopSearchIndexTask _task;

  private TransitGraphDao _transitGraphDao;

  private NarrativeService _narrativeService;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Before
  public void setup() throws IOException {

    _task = new GenerateStopSearchIndexTask();

    _bundle = Mockito.mock(FederatedTransitDataBundle.class);
    _task.setBundle(_bundle);

    File path = File.createTempFile(
        GenerateStopSearchIndexTask.class.getName(), ".tmp");
    path.delete();
    path.deleteOnExit();
    Mockito.when(_bundle.getStopSearchIndexPath()).thenReturn(path);

    _transitGraphDao = Mockito.mock(TransitGraphDao.class);
    _task.setTransitGraphDao(_transitGraphDao);

    _narrativeService = Mockito.mock(NarrativeService.class);
    _task.setNarrativeService(_narrativeService);

    _refreshService = Mockito.mock(RefreshService.class);
    _task.setRefreshService(_refreshService);
  }

  @Test
  public void testGenerateStopSearchIndex() throws CorruptIndexException,
      IOException, ParseException {

    StopEntryImpl stopA = stop("111", 0, 0);
    StopEntryImpl stopB = stop("222", 0, 0);
    StopEntryImpl stopC = stop("333", 0, 0);

    StopNarrative.Builder stopNarrativeA = StopNarrative.builder();
    stopNarrativeA.setCode("111");
    stopNarrativeA.setName("AAA Station");

    StopNarrative.Builder stopNarrativeB = StopNarrative.builder();
    stopNarrativeB.setName("BBB Station");

    StopNarrative.Builder stopNarrativeC = StopNarrative.builder();
    stopNarrativeC.setCode("444");
    stopNarrativeC.setName("CCC Station");

    Mockito.when(_transitGraphDao.getAllStops()).thenReturn(
        Arrays.asList((StopEntry) stopA, stopB, stopC));

    Mockito.when(_narrativeService.getStopForId(stopA.getId())).thenReturn(
        stopNarrativeA.create());
    Mockito.when(_narrativeService.getStopForId(stopB.getId())).thenReturn(
        stopNarrativeB.create());
    Mockito.when(_narrativeService.getStopForId(stopC.getId())).thenReturn(
        stopNarrativeC.create());

    _task.run();

    StopSearchServiceImpl searchService = new StopSearchServiceImpl();
    searchService.setBundle(_bundle);
    searchService.initialize();
    SearchResult<AgencyAndId> ids = searchService.searchForStopsByCode("111",
        10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(new AgencyAndId("1", "111"), ids.getResult(0));

    ids = searchService.searchForStopsByCode("222", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertTrue(ids.getResults().contains(new AgencyAndId("1", "222")));

    ids = searchService.searchForStopsByCode("333", 10, MIN_SCORE);
    assertEquals(0, ids.size());

    ids = searchService.searchForStopsByCode("444", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertTrue(ids.getResults().contains(new AgencyAndId("1", "333")));
  }
}
