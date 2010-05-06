package org.onebusaway.transit_data_federation.impl.offline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.impl.StopSearchServiceImpl;
import org.onebusaway.transit_data_federation.impl.offline.GenerateStopSearchIndexTask;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.ISLAND_AND_PORT_DATABASE_EXTENDED)
public class GenerateStopSearchIndexTaskTest extends TransitDataFederationBaseTest {

  @Autowired
  private GtfsRelationalDao _gtfsDao;

  private GenerateStopSearchIndexTask _task;

  private File _path;

  @Before
  public void setup() throws IOException {

    _task = new GenerateStopSearchIndexTask();

    _path = File.createTempFile(GenerateStopSearchIndexTask.class.getName(), ".tmp");
    _path.delete();
    _path.mkdirs();

    _task.setOutputPath(_path);
    _task.setGtfsDao(_gtfsDao);
  }

  @After
  public void teardown() {
    deleteFile(_path);
  }

  @Test
  public void testGenerateStopSearchIndex() throws CorruptIndexException, IOException, ParseException {

    _task.run();
    StopSearchServiceImpl searchService = new StopSearchServiceImpl();
    searchService.setIndexPath(_path);
    searchService.initialize();
    SearchResult<AgencyAndId> ids = searchService.searchForStopsByCode("1001", 10);
    assertEquals(1, ids.size());
    assertEquals(new AgencyAndId("26", "1001"), ids.getResult(0));

    ids = searchService.searchForStopsByCode("1107", 10);
    assertEquals(2, ids.size());
    assertTrue(ids.getResults().contains(new AgencyAndId("26", "1107")));
    assertTrue(ids.getResults().contains(new AgencyAndId("29", "1107")));

    ids = searchService.searchForStopsByName("Stanwood", 10);
    assertEquals(4, ids.size());
    assertTrue(ids.getResults().contains(new AgencyAndId("26", "1006")));
    assertTrue(ids.getResults().contains(new AgencyAndId("26", "1007")));
    assertTrue(ids.getResults().contains(new AgencyAndId("26", "1010")));
    assertTrue(ids.getResults().contains(new AgencyAndId("26", "1024")));
  }
}
