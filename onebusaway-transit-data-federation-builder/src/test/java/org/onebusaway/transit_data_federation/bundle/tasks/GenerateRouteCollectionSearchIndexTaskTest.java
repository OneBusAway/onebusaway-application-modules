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
package org.onebusaway.transit_data_federation.bundle.tasks;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.routeCollection;

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
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.RouteCollectionSearchServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.RouteCollectionEntryImpl;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;

public class GenerateRouteCollectionSearchIndexTaskTest {

  private static final double MIN_SCORE = 0.4;

  private GenerateRouteCollectionSearchIndexTask _task;

  private TransitGraphDao _transitGraphDao;

  private NarrativeService _narrativeService;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Before
  public void setup() throws IOException {

    _task = new GenerateRouteCollectionSearchIndexTask();

    _bundle = Mockito.mock(FederatedTransitDataBundle.class);
    _task.setBundle(_bundle);

    File path = File.createTempFile(
        GenerateRouteCollectionSearchIndexTask.class.getName(), ".tmp");
    path.delete();
    path.deleteOnExit();
    Mockito.when(_bundle.getRouteSearchIndexPath()).thenReturn(path);

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

    RouteCollectionEntryImpl routeA = routeCollection("routeA");
    RouteCollectionEntryImpl routeB = routeCollection("routeB");
    RouteCollectionEntryImpl routeC = routeCollection("routeC");

    RouteCollectionNarrative.Builder routeNarrativeA = RouteCollectionNarrative.builder();
    routeNarrativeA.setShortName("10");
    routeNarrativeA.setLongName("El Diez");

    RouteCollectionNarrative.Builder routeNarrativeB = RouteCollectionNarrative.builder();
    routeNarrativeB.setShortName("11");
    routeNarrativeB.setLongName("El Once");

    RouteCollectionNarrative.Builder routeNarrativeC = RouteCollectionNarrative.builder();
    routeNarrativeC.setShortName("100");

    Mockito.when(_transitGraphDao.getAllRouteCollections()).thenReturn(
        Arrays.asList((RouteCollectionEntry) routeA, routeB, routeC));

    Mockito.when(_narrativeService.getRouteCollectionForId(routeA.getId())).thenReturn(
        routeNarrativeA.create());
    Mockito.when(_narrativeService.getRouteCollectionForId(routeB.getId())).thenReturn(
        routeNarrativeB.create());
    Mockito.when(_narrativeService.getRouteCollectionForId(routeC.getId())).thenReturn(
        routeNarrativeC.create());

    _task.run();

    Mockito.verify(_refreshService).refresh(
        RefreshableResources.ROUTE_COLLECTION_SEARCH_DATA);

    RouteCollectionSearchServiceImpl searchService = new RouteCollectionSearchServiceImpl();
    searchService.setBundle(_bundle);
    searchService.initialize();

    SearchResult<AgencyAndId> ids = searchService.searchForRoutesByName("10",
        10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(routeA.getId(), ids.getResult(0));

    ids = searchService.searchForRoutesByName("el diez", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(routeA.getId(), ids.getResult(0));

    ids = searchService.searchForRoutesByName("diez", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(routeA.getId(), ids.getResult(0));

    ids = searchService.searchForRoutesByName("11", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(routeB.getId(), ids.getResult(0));

    ids = searchService.searchForRoutesByName("100", 10, MIN_SCORE);
    assertEquals(1, ids.size());
    assertEquals(routeC.getId(), ids.getResult(0));

  }
}
