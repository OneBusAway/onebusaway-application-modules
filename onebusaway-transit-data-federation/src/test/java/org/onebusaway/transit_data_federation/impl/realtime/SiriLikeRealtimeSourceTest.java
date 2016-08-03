/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.w3c.dom.Node;

public class SiriLikeRealtimeSourceTest {

  private SiriLikeRealtimeSource source;
  
  @Before
  public void setUp() throws Exception {
    String[] routes = {"2"};
    List<String>  routeList = Arrays.asList(routes);
    source = new SiriLikeRealtimeSource() {
      protected Integer calculateScheduleDeviation(VehicleLocationRecord vlr) {
        return -2;
      }
    };
    source.setAgency("UTA");
    source.setRefreshInterval(-1);
    source.setup();
    source.setRoutes(routeList);
  }
  
  
  @Test
  public void testParseEmpty() throws Exception {
    URL url = getClass().getResource("SiriLike_Empty.xml").toURI().toURL();
    List<Node> vehicles = source.parseVehicles(url);
    assertNotNull(vehicles);
    assertEquals(1, vehicles.size());
    VehicleLocationRecord vlr = source.parse(vehicles.get(0));
    assertNull(vlr);
  }

  @Test
  public void testParseRoute2() throws Exception {
    URL url = getClass().getResource("SiriLike_2.xml").toURI().toURL();
    List<Node> vehicles = source.parseVehicles(url);
    assertNotNull(vehicles);
    assertEquals(1, vehicles.size());
    // as we don't have predictions, there isn't much we can do with this 
    VehicleLocationRecord vlr = source.parse(vehicles.get(0));
    assertNotNull(vlr);
    assertEquals(1470227367368l, vlr.getTimeOfRecord());
    assertEquals(1470227367368l, vlr.getTimeOfLocationUpdate());
    assertEquals("UTA", vlr.getTripId().getAgencyId());
    assertEquals("2726094", vlr.getTripId().getId());
    assertNull(vlr.getBlockId());
    assertEquals(1470204000000l, vlr.getServiceDate());
    assertEquals("07011", vlr.getVehicleId().getId());
    assertEquals(40.76589, vlr.getCurrentLocationLat(), 0.0001);
    assertEquals(-111.90987, vlr.getCurrentLocationLon(), 0.0001);
    assertEquals(-2.0, vlr.getScheduleDeviation(), 0.01);
  }

  @Test
  public void testParseServiceDate() throws Exception {
    String s = "2016-08-03T00:00:00-06:00";
    Date d = source.parseServiceDate(s);
    assertEquals(1470204000000l, d.getTime());
  }
  
}
