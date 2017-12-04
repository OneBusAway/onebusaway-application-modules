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
import org.onebusaway.transit_data_federation.impl.realtime.SiriLikeRealtimeSource.NodesAndTimestamp;
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
    NodesAndTimestamp vehicles = source.parseVehicles(url);
    assertNotNull(vehicles);
    
    assertEquals(0, vehicles.getNodes().size());
  }

//  @Test
  public void testParseRoute2() throws Exception {
    URL url = getClass().getResource("SiriLike_2.xml").toURI().toURL();
    NodesAndTimestamp vehicles = source.parseVehicles(url);
    assertNotNull(vehicles);
    assertEquals(1, vehicles.getNodes().size());
    // as we don't have predictions, there isn't much we can do with this 
    VehicleLocationRecord vlr = source.parse(vehicles.getNodes().get(0), 0);
    assertNotNull(vlr);
    assertEquals("UTA", vlr.getTripId().getAgencyId());
    assertEquals("2726094", vlr.getTripId().getId());
    assertNull(vlr.getBlockId());
    assertEquals(1470204000000l, vlr.getServiceDate());
    assertEquals("07011", vlr.getVehicleId().getId());
    assertEquals(40.76589, vlr.getCurrentLocationLat(), 0.0001);
    assertEquals(-111.90987, vlr.getCurrentLocationLon(), 0.0001);
    assertEquals(-2.0, vlr.getScheduleDeviation(), 0.01);
  }
  
//  @Test
  public void testParseRoute2Multi() throws Exception {
    URL url = getClass().getResource("SiriLike_2_multi.xml").toURI().toURL();
    NodesAndTimestamp vehicles = source.parseVehicles(url);
    assertNotNull(vehicles);
    assertEquals(4, vehicles.getNodes().size());
    // as we don't have predictions, there isn't much we can do with this 
    VehicleLocationRecord vlr = source.parse(vehicles.getNodes().get(0), 0);
    assertNotNull(vlr);
    assertEquals("UTA", vlr.getTripId().getAgencyId());
    assertEquals("2726063", vlr.getTripId().getId());
    assertNull(vlr.getBlockId());
    assertEquals(1470204000000l, vlr.getServiceDate());
    assertEquals("13034", vlr.getVehicleId().getId());
    assertEquals(40.76492, vlr.getCurrentLocationLat(), 0.0001);
    assertEquals(-111.8756, vlr.getCurrentLocationLon(), 0.0001);
    assertEquals(-2.0, vlr.getScheduleDeviation(), 0.01);
  }


//  @Test
  public void testParseServiceDate() throws Exception {
    String s = "2016-08-03T00:00:00-06:00";
    Date d = source.parseServiceDate(s);
    assertEquals(1470204000000l, d.getTime());
    
    //make sure we always return beginning of day
    s = "2016-08-03T13:13:13-06:00";
    d = source.parseServiceDate(s);
    assertEquals(1470204000000l, d.getTime());
    
  }
  
  @Test
  public void testParseDate() throws Exception {
    String s = "2016-08-03T06:39:48-06:00";
    Date d = source.parseShortDate(s);
    assertEquals("expected time of " + new Date(1470227988000l) +", got " + d,
        1470227988000l, d.getTime());
    
    s = "2016-08-03T08:54:36.2551919-06:00";
    d = source.parseDate(s);
    assertEquals("expected " + new Date(1470236076259l) + ", got " + d,
        1470236076259l, d.getTime());
    
    s = "2016-08-03T12:13:26.4430-06:00";
    d = source.parseDate(s);
    assertEquals("expected " + new Date(1470248006440l) + ", got " + d,
        1470248006440l, d.getTime());
  }
  
}
