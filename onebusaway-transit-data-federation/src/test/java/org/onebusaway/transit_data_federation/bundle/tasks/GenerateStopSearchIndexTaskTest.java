/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.model.SearchResult;

public class GenerateStopSearchIndexTaskTest {

  private static final double MIN_SCORE = 1.0;

  private GtfsRelationalDao _gtfsDao;

  private GenerateStopSearchIndexTask _task;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  @Before
  public void setup() throws IOException {

    _task = new GenerateStopSearchIndexTask();

    File path = File.createTempFile(
        GenerateStopSearchIndexTask.class.getName(), ".tmp");
    path.delete();
    path.mkdirs();

    _bundle = new FederatedTransitDataBundle(path);
    _task.setBundle(_bundle);

    _gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    _task.setGtfsDao(_gtfsDao);
    
    _refreshService = Mockito.mock(RefreshService.class);
    _task.setRefreshService(_refreshService);
  }

  @After
  public void teardown() {
    deleteFile(_bundle.getPath());
  }

  @Test
  public void testGenerateStopSearchIndex() throws CorruptIndexException,
      IOException, ParseException {

    Stop stopA = new Stop();
    stopA.setCode("111");
    stopA.setId(new AgencyAndId("1", "111"));
    stopA.setName("AAA Station");

    Stop stopB = new Stop();
    stopB.setId(new AgencyAndId("1", "222"));
    stopB.setName("BBB Station");

    Stop stopC = new Stop();
    stopC.setCode("444");
    stopC.setId(new AgencyAndId("1", "333"));
    stopC.setName("CCC Station");

    Mockito.when(_gtfsDao.getAllStops()).thenReturn(
        Arrays.asList(stopA, stopB, stopC));

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

  private void deleteFile(File file) {
    if (!file.exists())
      return;
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children)
          deleteFile(child);
      }
    }
    file.delete();
  }
}
