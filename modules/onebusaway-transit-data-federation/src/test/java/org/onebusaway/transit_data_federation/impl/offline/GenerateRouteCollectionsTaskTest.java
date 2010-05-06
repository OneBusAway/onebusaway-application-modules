package org.onebusaway.transit_data_federation.impl.offline;

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.impl.offline.GenerateRouteCollectionsTask;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.TransitDataFederationMutableDao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.ISLAND_AND_PORT_DATABASE)
public class GenerateRouteCollectionsTaskTest extends
    TransitDataFederationBaseTest {

  @Autowired
  private GtfsRelationalDao _gtfsDao;

  @Autowired
  private TransitDataFederationMutableDao _whereMutableDao;

  @Test
  public void go() {

    GenerateRouteCollectionsTask task = new GenerateRouteCollectionsTask();
    task.setGtfsDao(_gtfsDao);
    task.setWhereMutableDao(_whereMutableDao);
    task.run();

    List<RouteCollection> routeCollections = _whereMutableDao.getAllRouteCollections();
    assertEquals(27, routeCollections.size());

    RouteCollection route = _whereMutableDao.getRouteCollectionForId(new AgencyAndId(
        "29", "6"));
    assertEquals("6", route.getShortName());
    assertEquals("Tri-Area Loop A", route.getLongName());
  }
}
