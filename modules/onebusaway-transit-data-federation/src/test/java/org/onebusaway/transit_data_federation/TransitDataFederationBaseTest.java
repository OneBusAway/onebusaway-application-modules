package org.onebusaway.transit_data_federation;

import org.onebusaway.testing.OneBusAwayBaseTest;
import org.onebusaway.testing.TestData;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.springframework.test.context.ContextConfiguration;

import java.io.File;

@ContextConfiguration(locations = {
    "/data-sources-test.xml",
    "/org/onebusaway/transit_data_federation/TransitDataFederationBaseTestContext.xml"})
public abstract class TransitDataFederationBaseTest extends OneBusAwayBaseTest {
  public static final String CALTRAIN_DATABASE = "/org/onebusaway/transit_data_federation/CaltrainDatabase.xml.gz";
  public static final String ISLAND_AND_PORT_DATABASE = "/org/onebusaway/transit_data_federation/IslandAndPortDatabase.xml.gz";
  public static final String ISLAND_AND_PORT_DATABASE_EXTENDED = "/org/onebusaway/transit_data_federation/IslandAndPortDatabaseExtended.xml.gz";

  protected File getIslandAndPortStopSearchIndex() {
    return TestData.getResourceAsFile(_context,
        "/org/onebusaway/transit_data_federation/IslandAndPort-StopSearchIndex");
  }

  protected File getIslandAndPortRouteSearchIndex() {
    return TestData.getResourceAsFile(
        _context,
        "/org/onebusaway/transit_data_federation/IslandAndPort-RouteCollectionSearchIndex");
  }

  public static Cache createCache() {
    CacheManager manager = new CacheManager(
        TransitDataFederationBaseTest.class.getResourceAsStream("ehcache.xml"));
    manager.addCache("cache");
    return manager.getCache("cache");
  }
}
