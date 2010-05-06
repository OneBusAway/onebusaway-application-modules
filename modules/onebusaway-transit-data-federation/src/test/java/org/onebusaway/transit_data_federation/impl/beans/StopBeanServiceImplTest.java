package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.ISLAND_AND_PORT_DATABASE_EXTENDED)
public class StopBeanServiceImplTest extends TransitDataFederationBaseTest {

  @Autowired
  private StopBeanService _stopBeanService;

  @Test
  public void testGetStopForId() {
    StopBean stopBean = _stopBeanService.getStopForId(new AgencyAndId("29", "1109"));
    assertNotNull(stopBean);
    assertEquals("Port Townsend Library", stopBean.getName());
    assertEquals(48.116228, stopBean.getLat(), 0.0);
    assertEquals(-122.763580, stopBean.getLon(), 0.0);
    List<RouteBean> routes = stopBean.getRoutes();
    assertEquals(2, routes.size());

    RouteBean first = routes.get(0);
    assertEquals("29_11", first.getId());
    assertEquals("11", first.getShortName());
    assertEquals("Port Townsend Downtown Shuttle", first.getLongName());

    RouteBean second = routes.get(1);
    assertEquals("29_12", second.getId());
    assertEquals("12", second.getShortName());
    assertEquals("Fort Worden/Peninsula College/Centrum", second.getLongName());
  }
}
