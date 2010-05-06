package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.junit.Test;

import java.io.File;

public class StopSearchServiceImplTest extends TransitDataFederationBaseTest {

  @Test
  public void testSearchForStopsById() throws Exception {
    
    File path = getIslandAndPortStopSearchIndex();
    
    StopSearchServiceImpl searchService = new StopSearchServiceImpl();
    searchService.setIndexPath(path);
    searchService.initialize();
    
    SearchResult<AgencyAndId> result = searchService.searchForStopsByCode("1110", 10);
    assertEquals(2,result.size());
    assertTrue(result.getResults().contains(new AgencyAndId("26","1110")));
    assertTrue(result.getResults().contains(new AgencyAndId("29","1110")));
    
    SearchResult<AgencyAndId> result2 = searchService.searchForStopsByCode("2", 10);
    assertEquals(1,result2.size());
    assertTrue(result2.getResults().contains(new AgencyAndId("26","2")));    
  }
}
