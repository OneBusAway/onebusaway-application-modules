package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.CALTRAIN_DATABASE)
public class RouteBeanServiceImplCaltrainTest extends TransitDataFederationBaseTest {

  @Autowired
  private RouteBeanService _routeBeanService;

  @Test
  public void testGetRouteForId() {

    RouteBean route = _routeBeanService.getRouteForId(new AgencyAndId("Caltrain", "Bullet"));
    assertNull(route.getShortName());
    assertEquals("Bullet", route.getLongName());

    AgencyBean agency = route.getAgency();
    assertEquals("Caltrain", agency.getName());
  }

  @Test
  public void testGetStopsForRoute() {

    StopsForRouteBean stopsForRoute = _routeBeanService.getStopsForRoute(new AgencyAndId("Caltrain", "Bullet"));
    List<StopBean> stops = stopsForRoute.getStops();
    assertEquals(12, stops.size());

    List<EncodedPolylineBean> polylines = stopsForRoute.getPolylines();
    assertEquals(2, polylines.size());
    
    List<StopGroupingBean> groupings = stopsForRoute.getStopGroupings();
    assertEquals(1, groupings.size());
    StopGroupingBean grouping = groupings.get(0);
    assertEquals("direction", grouping.getType());

    List<StopGroupBean> groups = grouping.getStopGroups();
    assertEquals(2, groups.size());

    StopGroupBean groupA = groups.get(0);
    StopGroupBean groupB = groups.get(1);
    
    if( ! groupA.getName().getName().equals("San Jose to San Francisco")) {
      StopGroupBean tmp = groupA;
      groupA = groupB;
      groupB = tmp;
    }
    
    NameBean nameA = groupA.getName();
    assertEquals("destination", nameA.getType());
    assertEquals("San Jose to San Francisco", nameA.getName());

    List<String> stopIdsA = groupA.getStopIds();
    assertEquals(12, stopIdsA.size());
    assertEquals(ids("Caltrain",
        "Tamien Caltrain",
        "San Jose Caltrain",
        "Sunnyvale Caltrain",
        "Mountain View Caltrain",
        "Palo Alto Caltrain",
        "Menlo Park Caltrain",
        "Redwood City Caltrain",
        "Hillsdale Caltrain",
        "San Mateo Caltrain",
        "Millbrae Caltrain",
        "22nd Street Caltrain",        
        "San Francisco Caltrain"
    ), stopIdsA);
    
    NameBean nameB = groupB.getName();
    assertEquals("destination", nameB.getType());
    assertEquals("San Francisco to San Jose", nameB.getName());

    List<String> stopIdsB = groupB.getStopIds();
    assertEquals(12, stopIdsB.size());
    assertEquals(ids("Caltrain",
        "San Francisco Caltrain",
        "22nd Street Caltrain",        
        "Millbrae Caltrain",
        "San Mateo Caltrain",
        "Hillsdale Caltrain",
        "Redwood City Caltrain",
        "Menlo Park Caltrain",
        "Palo Alto Caltrain",
        "Mountain View Caltrain",
        "Sunnyvale Caltrain",
        "San Jose Caltrain",
        "Tamien Caltrain"    
    ), stopIdsB);

  }

  private static List<String> ids(String agencyId, String... ids) {
    List<String> allIds = new ArrayList<String>();
    for (String id : ids)
      allIds.add(agencyId + "_" + id);
    return allIds;
  }
}
