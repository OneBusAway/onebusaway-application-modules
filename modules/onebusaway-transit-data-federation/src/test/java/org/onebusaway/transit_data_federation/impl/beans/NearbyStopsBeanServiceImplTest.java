package org.onebusaway.transit_data_federation.impl.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.testing.DbUnitTestConfiguration;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DbUnitTestConfiguration(location = TransitDataFederationBaseTest.ISLAND_AND_PORT_DATABASE_EXTENDED)
public class NearbyStopsBeanServiceImplTest extends
    TransitDataFederationBaseTest {

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private NearbyStopsBeanService _nearbyStopsBeanService;

  @Test
  public void testA() {
    StopBean stopBean = _stopBeanService.getStopForId(new AgencyAndId("26",
        "66"));
    List<AgencyAndId> ids = _nearbyStopsBeanService.getNearbyStops(stopBean,
        100);
    assertEquals(1, ids.size());
    assertTrue(ids.contains(new AgencyAndId("26", "1034")));
  }

  @Test
  public void testB() {
    StopBean stopBean = _stopBeanService.getStopForId(new AgencyAndId("26",
        "1034"));
    List<AgencyAndId> ids = _nearbyStopsBeanService.getNearbyStops(stopBean,
        170);
    assertEquals(2, ids.size());
    assertTrue(ids.contains(new AgencyAndId("26", "62")));
    assertTrue(ids.contains(new AgencyAndId("26", "66")));
  }
}
