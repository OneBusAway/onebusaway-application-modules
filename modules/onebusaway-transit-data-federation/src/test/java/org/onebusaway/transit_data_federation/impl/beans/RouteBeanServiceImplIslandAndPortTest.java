package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.ISLAND_AND_PORT_DATABASE_EXTENDED)
public class RouteBeanServiceImplIslandAndPortTest extends TransitDataFederationBaseTest {

  @Autowired
  private RouteBeanService _routeBeanService;

  @Test
  public void testGetRouteForId() {

    RouteBean route = _routeBeanService.getRouteForId(new AgencyAndId("26", "1"));
    assertEquals("1", route.getShortName());
    assertEquals("Clinton to Oak Harbor", route.getLongName());

    AgencyBean agency = route.getAgency();
    assertEquals("Island Transit", agency.getName());
  }

  @Test
  public void testGetStopsForRoute() {

    StopsForRouteBean stopsForRoute = _routeBeanService.getStopsForRoute(new AgencyAndId("26", "1"));
    List<StopBean> stops = stopsForRoute.getStops();
    assertEquals(121, stops.size());
  }
}
