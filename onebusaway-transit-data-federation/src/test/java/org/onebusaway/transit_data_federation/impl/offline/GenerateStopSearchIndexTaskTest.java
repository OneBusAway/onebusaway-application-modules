package org.onebusaway.transit_data_federation.impl.offline;

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
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.model.SearchResult;

public class GenerateStopSearchIndexTaskTest {
  
  private static final double MIN_SCORE = 1.0;

  private GtfsRelationalDao _gtfsDao;

  private GenerateStopSearchIndexTask _task;

  private File _path;

  @Before
  public void setup() throws IOException {

    _task = new GenerateStopSearchIndexTask();

    _path = File.createTempFile(GenerateStopSearchIndexTask.class.getName(),
        ".tmp");
    _path.delete();
    _path.mkdirs();

    _task.setOutputPath(_path);

    _gtfsDao = Mockito.mock(GtfsRelationalDao.class);
    _task.setGtfsDao(_gtfsDao);
  }

  @After
  public void teardown() {
    deleteFile(_path);
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
    searchService.setIndexPath(_path);
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
