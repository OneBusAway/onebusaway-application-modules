package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.model.SearchResult;

import org.junit.Test;

import java.io.File;

public class RouteSearchServiceImplTest extends TransitDataFederationBaseTest {

  @Test
  public void testSearchForRoutesByName() throws Exception {

    File path = getIslandAndPortRouteSearchIndex();

    RouteCollectionSearchServiceImpl searchService = new RouteCollectionSearchServiceImpl();
    searchService.setIndexPath(path);
    searchService.initialize();

    SearchResult<AgencyAndId> result1 = searchService.searchForRoutesByShortName("6",10);
    assertEquals(2, result1.size());
    assertTrue(result1.getResults().contains(new AgencyAndId("29", "6")));
    assertTrue(result1.getResults().contains(new AgencyAndId("26", "6")));

    SearchResult<AgencyAndId> result2 = searchService.searchForRoutesByShortName("1",10);
    assertEquals(2, result2.size());
    assertTrue(result2.getResults().contains(new AgencyAndId("29", "1")));
    assertTrue(result2.getResults().contains(new AgencyAndId("26", "1")));

    SearchResult<AgencyAndId> result3 = searchService.searchForRoutesByShortName("411C",10);
    assertEquals(1, result3.size());
    assertTrue(result3.getResults().contains(new AgencyAndId("26", "411C")));
  }
}
